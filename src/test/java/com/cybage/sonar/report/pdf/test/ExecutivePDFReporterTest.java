package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.ExecutivePDFReporter;
import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import com.cybage.sonar.report.pdf.util.Credentials;
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
    // Reflection helper
    // -------------------------------------------------------------------------

    private Object invokeProtected(String methodName) throws Exception {
        Method m = ExecutivePDFReporter.class.getDeclaredMethod(methodName);
        m.setAccessible(true);
        return m.invoke(reporter);
    }
}
