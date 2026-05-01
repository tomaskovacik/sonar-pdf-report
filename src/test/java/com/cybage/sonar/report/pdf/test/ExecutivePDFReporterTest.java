package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.ExecutivePDFReporter;
import com.cybage.sonar.report.pdf.builder.ConditionBuilder;
import com.cybage.sonar.report.pdf.builder.QualityProfileEntityBuilder;
import com.cybage.sonar.report.pdf.builder.StatusPeriodBuilder;
import com.cybage.sonar.report.pdf.entity.*;
import com.cybage.sonar.report.pdf.util.Credentials;
import com.itextpdf.text.ChapterAutoNumber;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Section;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Tests for {@link ExecutivePDFReporter} covering the accessor methods,
 * constants, and {@link ExecutivePDFReporter#getReportType()}.
 */
@Test(groups = {"report"})
public class ExecutivePDFReporterTest {

    private ExecutivePDFReporter reporter;
    private Properties langProperties;
    private Properties configProperties;
    private LeakPeriodConfiguration leakPeriod;
    private List<String> sonarLanguage;
    private Set<String> otherMetrics;
    private Set<String> typesOfIssue;

    @BeforeMethod
    public void setUp() {
        langProperties = new Properties();
        configProperties = new Properties();
        leakPeriod = new LeakPeriodConfiguration();
        sonarLanguage = Arrays.asList("java", "xml");
        otherMetrics = new HashSet<>(Collections.singletonList("custom_metric"));
        typesOfIssue = new HashSet<>(Collections.singletonList("BUG"));

        reporter = new ExecutivePDFReporter(
                new Credentials("http://localhost:9000", "token"),
                null,
                "com.example:test",
                "2.0",
                sonarLanguage,
                otherMetrics,
                typesOfIssue,
                leakPeriod,
                configProperties,
                langProperties,
                null);
    }

    // -------------------------------------------------------------------------
    // Public constant
    // -------------------------------------------------------------------------

    @Test
    public void testLogoPropsConstant() {
        Assert.assertEquals(ExecutivePDFReporter.LOGO_PROPS, "front.page.logo");
    }

    // -------------------------------------------------------------------------
    // getReportType
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportType() {
        Assert.assertEquals(reporter.getReportType(), "pdf");
    }

    // -------------------------------------------------------------------------
    // getProjectVersion (public)
    // -------------------------------------------------------------------------

    @Test
    public void testGetProjectVersion() {
        Assert.assertEquals(reporter.getProjectVersion(), "2.0");
    }

    // -------------------------------------------------------------------------
    // Protected / private accessors via reflection
    // -------------------------------------------------------------------------

    @Test
    public void testGetProjectKey() throws Exception {
        Assert.assertEquals(invokeProtected("getProjectKey"), "com.example:test");
    }

    @Test
    public void testGetLogo() throws Exception {
        Assert.assertNull(invokeProtected("getLogo"));
    }

    @Test
    public void testGetSonarLanguage() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokeProtected("getSonarLanguage");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains("java"));
    }

    @Test
    public void testGetOtherMetrics() throws Exception {
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) invokeProtected("getOtherMetrics");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("custom_metric"));
    }

    @Test
    public void testGetTypesOfIssue() throws Exception {
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) invokeProtected("getTypesOfIssue");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("BUG"));
    }

    @Test
    public void testGetLeakPeriod() throws Exception {
        LeakPeriodConfiguration result = (LeakPeriodConfiguration) invokeProtected("getLeakPeriod");
        Assert.assertNotNull(result);
        Assert.assertSame(result, leakPeriod);
    }

    @Test
    public void testGetLangProperties() throws Exception {
        Properties result = (Properties) invokeProtected("getLangProperties");
        Assert.assertNotNull(result);
        Assert.assertSame(result, langProperties);
    }

    @Test
    public void testGetReportProperties() throws Exception {
        Properties result = (Properties) invokeProtected("getReportProperties");
        Assert.assertNotNull(result);
        Assert.assertSame(result, configProperties);
    }

    // -------------------------------------------------------------------------
    // getTextProperty (inherited from PDFReporter)
    // -------------------------------------------------------------------------

    @Test
    public void testGetTextPropertyReturnsValue() {
        langProperties.setProperty("some.key", "some value");
        Assert.assertEquals(reporter.getTextProperty("some.key"), "some value");
    }

    @Test
    public void testGetTextPropertyReturnsMissingPrefixWhenAbsent() {
        String result = reporter.getTextProperty("nonexistent.key");
        Assert.assertTrue(result.startsWith("missing!"), "should prefix missing keys with 'missing!'");
    }

    // -------------------------------------------------------------------------
    // getConfigProperty (inherited from PDFReporter)
    // -------------------------------------------------------------------------

    @Test
    public void testGetConfigProperty() {
        configProperties.setProperty("config.key", "config value");
        Assert.assertEquals(reporter.getConfigProperty("config.key"), "config value");
    }

    @Test
    public void testGetConfigPropertyAbsentReturnsNull() {
        Assert.assertNull(reporter.getConfigProperty("absent.key"));
    }

    // -------------------------------------------------------------------------
    // Constructor with null logo / empty collections
    // -------------------------------------------------------------------------

    @Test
    public void testConstructorWithEmptyCollections() {
        ExecutivePDFReporter empty = new ExecutivePDFReporter(
                new Credentials("http://localhost:9000", "token"),
                null,
                "my:project",
                "1.0",
                Collections.emptyList(),
                Collections.emptySet(),
                Collections.emptySet(),
                new LeakPeriodConfiguration(),
                new Properties(),
                new Properties(),
                null);

        Assert.assertEquals(empty.getProjectVersion(), "1.0");
        Assert.assertEquals(empty.getReportType(), "pdf");
    }

    // -------------------------------------------------------------------------
    // Protected print methods — smoke tests using real iText objects
    // -------------------------------------------------------------------------

    @Test
    public void testPrintQualityProfileInfoWithNoLanguages() throws Exception {
        Project project = minimalProject();
        project.setLanguages(null);
        project.setQualityProfiles(Collections.emptyList());

        invokePrint("printQualityProfileInfo", project, createSection());
        // passes if no exception
    }

    @Test
    public void testPrintQualityProfileInfoWithLanguagesAndProfiles() throws Exception {
        QualityProfile qp = new QualityProfileEntityBuilder()
                .setKey("k1").setName("Sonar way").setLanguage("java").setLanguageName("Java")
                .setIsInherited(false).setIsDefault(true).setActiveRuleCount(100L)
                .setRulesUpdatedAt("2024-01-01").setProjectCount(5L).createQualityProfile();

        Project project = minimalProject();
        project.setLanguages(Collections.singletonList("java"));
        project.setQualityProfiles(Collections.singletonList(qp));

        invokePrint("printQualityProfileInfo", project, createSection());
    }

    @Test
    public void testPrintQualityGateInfoStatusOk() throws Exception {
        Project project = minimalProject();
        project.setProjectStatus(new ProjectStatus("OK", Collections.emptyList(), Collections.emptyList()));

        invokePrint("printQualityGateInfo", project, createSection());
    }

    @Test
    public void testPrintQualityGateInfoStatusError() throws Exception {
        Condition c = new ConditionBuilder()
                .setStatus("ERROR").setMetricKey("coverage").setComparator("LT")
                .setErrorThreshold("80").setActualValue("70").createCondition();
        StatusPeriod sp = new StatusPeriodBuilder()
                .setIndex(1).setMode("previous_version").setDate("2024-01-01")
                .setParameter("1.0").createStatusPeriod();

        Project project = minimalProject();
        project.setProjectStatus(new ProjectStatus("ERROR",
                Collections.singletonList(c),
                Collections.singletonList(sp)));

        invokePrint("printQualityGateInfo", project, createSection());
    }

    @Test
    public void testPrintMostViolatedRulesEmptyList() throws Exception {
        Project project = minimalProject();
        project.setMostViolatedRules(Collections.emptyList());

        invokePrint("printMostViolatedRules", project, createSection());
    }

    @Test
    public void testPrintMostViolatedFilesEmptyList() throws Exception {
        Project project = minimalProject();
        project.setMostViolatedFiles(Collections.emptyList());

        invokePrint("printMostViolatedFiles", project, createSection());
    }

    @Test
    public void testPrintMostComplexFilesEmptyList() throws Exception {
        Project project = minimalProject();
        project.setMostComplexFiles(Collections.emptyList());

        invokePrint("printMostComplexFiles", project, createSection());
    }

    @Test
    public void testPrintMostDuplicatedFilesEmptyList() throws Exception {
        Project project = minimalProject();
        project.setMostDuplicatedFiles(Collections.emptyList());

        invokePrint("printMostDuplicatedFiles", project, createSection());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Section createSection() {
        ChapterAutoNumber chapter = new ChapterAutoNumber(new Paragraph("Chapter"));
        return chapter.addSection(new Paragraph("Section"));
    }

    private Project minimalProject() {
        Project p = new Project("my:key", "1.0", Collections.singletonList("java"));
        p.setName("Test Project");
        p.setProjectStatus(new ProjectStatus("OK", Collections.emptyList(), Collections.emptyList()));
        p.setQualityProfiles(Collections.emptyList());
        p.setMostViolatedRules(Collections.emptyList());
        p.setMostComplexFiles(Collections.emptyList());
        p.setMostDuplicatedFiles(Collections.emptyList());
        p.setMostViolatedFiles(Collections.emptyList());
        Measures measures = new Measures();
        measures.setPeriods(Collections.emptyList());
        p.setMeasures(measures);
        return p;
    }

    private void invokePrint(String methodName, Project project, Section section) throws Exception {
        Method m = ExecutivePDFReporter.class.getDeclaredMethod(methodName, Project.class, Section.class);
        m.setAccessible(true);
        m.invoke(reporter, project, section);
    }

    // -------------------------------------------------------------------------
    // Reflection helper
    // -------------------------------------------------------------------------

    private Object invokeProtected(String methodName) throws Exception {
        Method m = ExecutivePDFReporter.class.getDeclaredMethod(methodName);
        m.setAccessible(true);
        return m.invoke(reporter);
    }
}
