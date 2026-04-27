package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.batch.PDFPostJob;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.config.Configuration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.*;

@Test(groups = {"metrics"})
public class PDFPostJobTest {

    private Path tempWorkDir;
    private FileSystem mockFs;
    private Configuration mockConfig;
    private PDFPostJob job;

    @BeforeMethod
    public void setUp() throws Exception {
        tempWorkDir = Files.createTempDirectory("sonar-postjob-test");
        mockFs = mock(FileSystem.class);
        when(mockFs.workDir()).thenReturn(tempWorkDir.toFile());
        mockConfig = mock(Configuration.class);
        job = new PDFPostJob(mockConfig, mockFs);
    }

    @AfterMethod
    public void tearDown() {
        deleteDirectory(tempWorkDir.toFile());
    }

    // ---- constants ----

    @Test
    public void testSkipPdfKey() {
        Assert.assertEquals(PDFPostJob.SKIP_PDF_KEY, "sonar.pdf.skip");
    }

    @Test
    public void testSkipPdfDefaultValue() {
        Assert.assertFalse(PDFPostJob.SKIP_PDF_DEFAULT_VALUE);
    }

    @Test
    public void testReportTypeKey() {
        Assert.assertEquals(PDFPostJob.REPORT_TYPE, "report.type");
    }

    @Test
    public void testReportTypeDefault() {
        Assert.assertEquals(PDFPostJob.REPORT_TYPE_DEFAULT_VALUE, "pdf");
    }

    @Test
    public void testSonarUserTokenEnv() {
        Assert.assertEquals(PDFPostJob.SONAR_USER_TOKEN_ENV, "SONAR_USER_TOKEN");
    }

    @Test
    public void testSonarHostUrlKey() {
        Assert.assertEquals(PDFPostJob.SONAR_HOST_URL, "sonar.host.url");
    }

    @Test
    public void testSonarHostUrlDefault() {
        Assert.assertEquals(PDFPostJob.SONAR_HOST_URL_DEFAULT_VALUE, "http://localhost:9000");
    }

    @Test
    public void testSonarProjectVersionKey() {
        Assert.assertEquals(PDFPostJob.SONAR_PROJECT_VERSION, "sonar.projectVersion");
    }

    @Test
    public void testSonarProjectVersionDefault() {
        Assert.assertEquals(PDFPostJob.SONAR_PROJECT_VERSION_DEFAULT_VALUE, "1.0");
    }

    @Test
    public void testLeakPeriodKey() {
        Assert.assertEquals(PDFPostJob.LEAK_PERIOD, "sonar.leak.period");
    }

    @Test
    public void testOtherMetricsKey() {
        Assert.assertEquals(PDFPostJob.OTHER_METRICS, "sonar.pdf.other.metrics");
    }

    @Test
    public void testTypesOfIssueKey() {
        Assert.assertEquals(PDFPostJob.TYPES_OF_ISSUE, "sonar.pdf.issue.details");
    }

    // ---- describe() ----

    @Test
    public void testDescribeIsNoOp() {
        PostJobDescriptor descriptor = mock(PostJobDescriptor.class);
        // Should not throw
        job.describe(descriptor);
        verifyNoMoreInteractions(descriptor);
    }

    // ---- execute() with skip flag ----

    @Test
    public void testExecuteSkipsWhenSkipFlagIsTrue() {
        PostJobContext ctx = mock(PostJobContext.class);
        Configuration cfg = mock(Configuration.class);
        when(ctx.config()).thenReturn(cfg);
        when(cfg.hasKey(PDFPostJob.SKIP_PDF_KEY)).thenReturn(true);
        when(cfg.getBoolean(PDFPostJob.SKIP_PDF_KEY)).thenReturn(Optional.of(Boolean.TRUE));

        // Should return early without calling get("sonar.projectKey")
        job.execute(ctx);

        verify(cfg, never()).get("sonar.projectKey");
    }

    @Test
    public void testExecuteDoesNotSkipWhenSkipFlagIsFalse() {
        PostJobContext ctx = mock(PostJobContext.class);
        Configuration cfg = mock(Configuration.class);
        when(ctx.config()).thenReturn(cfg);
        when(cfg.hasKey(PDFPostJob.SKIP_PDF_KEY)).thenReturn(true);
        when(cfg.getBoolean(PDFPostJob.SKIP_PDF_KEY)).thenReturn(Optional.of(Boolean.FALSE));
        when(cfg.hasKey(PDFPostJob.SONAR_HOST_URL)).thenReturn(false);
        when(cfg.get("sonar.projectKey")).thenReturn(Optional.of("test:project"));

        // SONAR_USER_TOKEN is not set in this env, so execute() returns early after the env check
        // This still exercises the skip-flag branch returning false
        job.execute(ctx);

        verify(cfg, atLeastOnce()).get("sonar.projectKey");
    }

    @Test
    public void testExecuteSkipsWhenSkipKeyAbsent() {
        PostJobContext ctx = mock(PostJobContext.class);
        Configuration cfg = mock(Configuration.class);
        when(ctx.config()).thenReturn(cfg);
        when(cfg.hasKey(PDFPostJob.SKIP_PDF_KEY)).thenReturn(false);
        when(cfg.hasKey(PDFPostJob.SONAR_HOST_URL)).thenReturn(false);
        when(cfg.get("sonar.projectKey")).thenReturn(Optional.of("test:project"));

        // No SONAR_USER_TOKEN in this env → logs warning and returns without throwing
        job.execute(ctx);

        verify(cfg, atLeastOnce()).get("sonar.projectKey");
    }

    // ---- readCeTaskId() via reflection ----

    @Test
    public void testReadCeTaskIdReturnsNullWhenFileAbsent() throws Exception {
        String result = invokeReadCeTaskId();
        Assert.assertNull(result, "should return null when report-task.txt does not exist");
    }

    @Test
    public void testReadCeTaskIdReturnsIdFromFile() throws Exception {
        File reportTaskFile = new File(tempWorkDir.toFile(), "report-task.txt");
        try (FileWriter fw = new FileWriter(reportTaskFile)) {
            fw.write("ceTaskId=abc-123-xyz\n");
        }

        String result = invokeReadCeTaskId();

        Assert.assertEquals(result, "abc-123-xyz");
    }

    @Test
    public void testReadCeTaskIdReturnsNullWhenKeyMissing() throws Exception {
        File reportTaskFile = new File(tempWorkDir.toFile(), "report-task.txt");
        try (FileWriter fw = new FileWriter(reportTaskFile)) {
            fw.write("serverUrl=http://localhost:9000\n");
        }

        String result = invokeReadCeTaskId();

        Assert.assertNull(result, "should return null when ceTaskId key is absent");
    }

    @Test
    public void testReadCeTaskIdReturnsNullWhenValueBlank() throws Exception {
        File reportTaskFile = new File(tempWorkDir.toFile(), "report-task.txt");
        try (FileWriter fw = new FileWriter(reportTaskFile)) {
            fw.write("ceTaskId=\n");
        }

        String result = invokeReadCeTaskId();

        Assert.assertNull(result, "should return null when ceTaskId value is blank");
    }

    // ---- helper ----

    private String invokeReadCeTaskId() throws Exception {
        Method m = PDFPostJob.class.getDeclaredMethod("readCeTaskId");
        m.setAccessible(true);
        try {
            return (String) m.invoke(job);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
