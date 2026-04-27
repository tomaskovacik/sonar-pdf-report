package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.batch.PDFGenerator;
import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import org.sonar.api.batch.fs.FileSystem;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class PDFGeneratorTest {

    private FileSystem mockFs;
    private LeakPeriodConfiguration leakPeriod;
    private List<String> languages;
    private Set<String> otherMetrics;
    private Set<String> typesOfIssue;

    @BeforeMethod
    public void setUp() {
        mockFs = mock(FileSystem.class);
        when(mockFs.workDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));
        leakPeriod = new LeakPeriodConfiguration();
        languages = Arrays.asList("java");
        otherMetrics = new HashSet<>();
        typesOfIssue = new HashSet<>();
    }

    private PDFGenerator createGenerator(String reportType) {
        return new PDFGenerator(
                "test:project",
                "1.0",
                languages,
                otherMetrics,
                typesOfIssue,
                leakPeriod,
                mockFs,
                "http://localhost:9000",
                "token123",
                reportType);
    }

    // ---- constants ----

    @Test
    public void testSonarBaseUrlConstant() {
        Assert.assertEquals(PDFGenerator.SONAR_BASE_URL, "sonar.base.url");
    }

    @Test
    public void testFrontPageLogoConstant() {
        Assert.assertEquals(PDFGenerator.FRONT_PAGE_LOGO, "front.page.logo");
    }

    @Test
    public void testDatePatternConstant() {
        Assert.assertEquals(PDFGenerator.DATE_PATTERN, "yyyy.MM.dd.HH.mm.ss");
    }

    // ---- isHtmlReport() via reflection ----

    @Test
    public void testIsHtmlReportReturnsTrueForHtml() throws Exception {
        PDFGenerator gen = createGenerator("html");
        Assert.assertTrue(invokeIsHtmlReport(gen));
    }

    @Test
    public void testIsHtmlReportReturnsTrueForHtmlCaseInsensitive() throws Exception {
        PDFGenerator gen = createGenerator("HTML");
        Assert.assertTrue(invokeIsHtmlReport(gen));
    }

    @DataProvider(name = "nonHtmlReportTypes")
    public Object[][] nonHtmlReportTypes() {
        return new Object[][] {
            { "pdf" },
            { null },
            { "word" }
        };
    }

    @Test(dataProvider = "nonHtmlReportTypes")
    public void testIsHtmlReportReturnsFalseForNonHtml(String reportType) throws Exception {
        PDFGenerator gen = createGenerator(reportType);
        Assert.assertFalse(invokeIsHtmlReport(gen));
    }

    // ---- computeReportPath() via reflection ----

    @Test
    public void testComputeReportPathReturnsPdfExtensionForPdf() throws Exception {
        PDFGenerator gen = createGenerator("pdf");
        SimpleDateFormat sdf = new SimpleDateFormat(PDFGenerator.DATE_PATTERN);
        String path = invokeComputeReportPath(gen, "test:project", sdf);
        Assert.assertTrue(path.endsWith(".pdf"), "path should end with .pdf but was: " + path);
    }

    @Test
    public void testComputeReportPathReturnsHtmlExtensionForHtml() throws Exception {
        PDFGenerator gen = createGenerator("html");
        SimpleDateFormat sdf = new SimpleDateFormat(PDFGenerator.DATE_PATTERN);
        String path = invokeComputeReportPath(gen, "test:project", sdf);
        Assert.assertTrue(path.endsWith(".html"), "path should end with .html but was: " + path);
    }

    @Test
    public void testComputeReportPathContainsProjectKey() throws Exception {
        PDFGenerator gen = createGenerator("pdf");
        SimpleDateFormat sdf = new SimpleDateFormat(PDFGenerator.DATE_PATTERN);
        String path = invokeComputeReportPath(gen, "my:project", sdf);
        Assert.assertTrue(path.contains("my-project"), "path should contain sanitized project key");
    }

    @Test
    public void testComputeReportPathContainsWorkDir() throws Exception {
        PDFGenerator gen = createGenerator("pdf");
        SimpleDateFormat sdf = new SimpleDateFormat(PDFGenerator.DATE_PATTERN);
        String path = invokeComputeReportPath(gen, "test:project", sdf);
        Assert.assertTrue(path.startsWith(mockFs.workDir().getAbsolutePath()),
                "path should start with workDir");
    }

    // ---- reflection helpers ----

    private boolean invokeIsHtmlReport(PDFGenerator gen) throws Exception {
        Method m = PDFGenerator.class.getDeclaredMethod("isHtmlReport");
        m.setAccessible(true);
        try {
            return (Boolean) m.invoke(gen);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private String invokeComputeReportPath(PDFGenerator gen, String projectId, SimpleDateFormat sdf) throws Exception {
        Method m = PDFGenerator.class.getDeclaredMethod("computeReportPath", String.class, SimpleDateFormat.class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(gen, projectId, sdf);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }
}
