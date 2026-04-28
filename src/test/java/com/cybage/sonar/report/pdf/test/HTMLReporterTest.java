package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.HTMLReporter;
import com.cybage.sonar.report.pdf.Toc;
import com.cybage.sonar.report.pdf.entity.*;
import com.cybage.sonar.report.pdf.util.Credentials;
import com.cybage.sonar.report.pdf.util.MetricKeys;
import com.cybage.sonar.report.pdf.util.ProjectStatusKeys;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Tests for {@link HTMLReporter}.
 *
 * <p>All tests use a concrete anonymous subclass of {@code HTMLReporter} that overrides
 * {@link HTMLReporter#getProject()} to return a fully-populated in-memory {@link Project},
 * avoiding any network calls to SonarQube.</p>
 */
@Test(groups = {"report"})
public class HTMLReporterTest {

    private Properties langProperties;
    private Properties configProperties;
    private LeakPeriodConfiguration leakPeriod;

    @BeforeMethod
    public void setUp() {
        langProperties = buildLangProperties();
        configProperties = new Properties();
        leakPeriod = new LeakPeriodConfiguration();
    }

    // -------------------------------------------------------------------------
    // getReportType
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportType() {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(reporter.getReportType(), "html");
    }

    // -------------------------------------------------------------------------
    // getReport() – quality gate OK, no typesOfIssue, no description
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportProducesHtmlForOkGate() throws Exception {
        Project project = buildOkProject();
        project.setDescription(null); // no description branch
        HTMLReporter reporter = createReporter(Collections.emptySet(), project);

        ByteArrayOutputStream baos = reporter.getReport();
        Assert.assertNotNull(baos);
        String html = baos.toString("UTF-8");
        Assert.assertTrue(html.contains("<!DOCTYPE html>"), "should contain DOCTYPE");
        Assert.assertTrue(html.contains("</html>"), "should close html tag");
        Assert.assertTrue(html.contains("Test Project"), "should contain project name");
        Assert.assertTrue(html.contains("Passed"), "OK gate should show Passed badge");
    }

    // -------------------------------------------------------------------------
    // getReport() – project with description
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportIncludesDescriptionWhenPresent() throws Exception {
        Project project = buildOkProject();
        project.setDescription("My project description");
        HTMLReporter reporter = createReporter(Collections.emptySet(), project);

        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("My project description"),
                "report should contain the project description");
    }

    // -------------------------------------------------------------------------
    // getReport() – quality gate ERROR with failed conditions
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportShowsFailedConditionsForErrorGate() throws Exception {
        Project project = buildErrorGateProject();
        HTMLReporter reporter = createReporter(Collections.emptySet(), project);

        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("Failed"), "ERROR gate should show Failed badge");
        Assert.assertTrue(html.contains("badge-error"), "should use badge-error CSS class");
    }

    // -------------------------------------------------------------------------
    // getReport() – typesOfIssue non-empty (appendIssueDetails branch)
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportWithTypesOfIssueIncludesIssueSection() throws Exception {
        Project project = buildOkProject();
        // Add a BUG issue to the project
        Issue bug = new Issue("MyFile.java", "src/MyFile.java", "MAJOR", 42, "OPEN", "Null pointer", "BUG", "5min");
        project.setIssues(Collections.singletonList(bug));

        HTMLReporter reporter = createReporter(Collections.singleton("bug"), project);

        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("Null pointer"), "issue message should appear in report");
    }

    // -------------------------------------------------------------------------
    // getReport() – issue with null line (N/A branch)
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportIssueWithNullLineShowsNA() throws Exception {
        Project project = buildOkProject();
        Issue issue = new Issue("File.java", "src/File.java", "MINOR", null, "OPEN", "Some message", "CODE_SMELL", "1min");
        project.setIssues(Collections.singletonList(issue));

        HTMLReporter reporter = createReporter(Collections.singleton("code_smell"), project);

        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("N/A"), "null line should render as N/A");
    }

    // -------------------------------------------------------------------------
    // getReport() – issue with line == 0 (N/A branch)
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportIssueWithZeroLineShowsNA() throws Exception {
        Project project = buildOkProject();
        Issue issue = new Issue("File.java", "src/File.java", "MINOR", 0, "OPEN", "Zero line message", "BUG", "2min");
        project.setIssues(Collections.singletonList(issue));

        HTMLReporter reporter = createReporter(Collections.singleton("bug"), project);

        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("N/A"), "zero line should render as N/A");
    }

    // -------------------------------------------------------------------------
    // getReport() – coverage section included when COVERAGE measure present
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportIncludesCoverageSectionWhenMeasurePresent() throws Exception {
        Project project = buildOkProject();
        // Coverage measures are already in buildOkProject – verify they render
        HTMLReporter reporter = createReporter(Collections.emptySet(), project);

        String html = reporter.getReport().toString("UTF-8");
        // coverage section header comes from metrics.coverage property
        Assert.assertTrue(html.contains("coverage"), "coverage section should appear");
    }

    // -------------------------------------------------------------------------
    // getReport() – violated files / complex files / duplicated files non-empty
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportMostViolatedFilesRendersTable() throws Exception {
        Project project = buildOkProject();
        FileInfo fi = new FileInfo();
        fi.setName("ViolatedFile.java");
        fi.setPath("src/ViolatedFile.java");
        fi.setViolations("5");
        fi.setComplexity("0");
        fi.setDuplicatedLines("0");
        project.setMostViolatedFiles(Collections.singletonList(fi));

        HTMLReporter reporter = createReporter(Collections.emptySet(), project);
        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("ViolatedFile.java"), "violated file name should appear");
    }

    @Test
    public void testGetReportMostComplexFilesRendersTable() throws Exception {
        Project project = buildOkProject();
        FileInfo fi = new FileInfo();
        fi.setName("ComplexFile.java");
        fi.setPath("src/ComplexFile.java");
        fi.setViolations("0");
        fi.setComplexity("50");
        fi.setDuplicatedLines("0");
        project.setMostComplexFiles(Collections.singletonList(fi));

        HTMLReporter reporter = createReporter(Collections.emptySet(), project);
        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("ComplexFile.java"), "complex file name should appear");
    }

    @Test
    public void testGetReportMostDuplicatedFilesRendersTable() throws Exception {
        Project project = buildOkProject();
        FileInfo fi = new FileInfo();
        fi.setName("DupFile.java");
        fi.setPath("src/DupFile.java");
        fi.setViolations("0");
        fi.setComplexity("0");
        fi.setDuplicatedLines("20");
        project.setMostDuplicatedFiles(Collections.singletonList(fi));

        HTMLReporter reporter = createReporter(Collections.emptySet(), project);
        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("DupFile.java"), "duplicated file name should appear");
    }

    // -------------------------------------------------------------------------
    // getReport() – most violated rules with severity rows
    // -------------------------------------------------------------------------

    @Test
    public void testGetReportMostViolatedRulesRendersTable() throws Exception {
        Project project = buildOkProject();
        Rule rule = new Rule("squid:S1234", "Use proper naming", 7L, "Java", "MAJOR");
        project.setMostViolatedRules(Collections.singletonList(rule));

        HTMLReporter reporter = createReporter(Collections.emptySet(), project);
        String html = reporter.getReport().toString("UTF-8");
        Assert.assertTrue(html.contains("Use proper naming"), "rule name should appear");
    }

    // -------------------------------------------------------------------------
    // escape() via reflection – corner cases
    // -------------------------------------------------------------------------

    @Test
    public void testEscapeNullReturnsEmptyString() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(invokeEscape(reporter, null), "");
    }

    @Test
    public void testEscapeAmpersand() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(invokeEscape(reporter, "a & b"), "a &amp; b");
    }

    @Test
    public void testEscapeLessThan() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(invokeEscape(reporter, "a < b"), "a &lt; b");
    }

    @Test
    public void testEscapeGreaterThan() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(invokeEscape(reporter, "a > b"), "a &gt; b");
    }

    @Test
    public void testEscapeDoubleQuote() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(invokeEscape(reporter, "say \"hi\""), "say &quot;hi&quot;");
    }

    @Test
    public void testEscapePlainTextUnchanged() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(invokeEscape(reporter, "hello world"), "hello world");
    }

    // -------------------------------------------------------------------------
    // appendMetricRow() via reflection
    // -------------------------------------------------------------------------

    @Test
    public void testAppendMetricRowWithNullValue() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        StringBuilder sb = new StringBuilder();
        invokeAppendMetricRow(reporter, sb, "Label", null);
        String result = sb.toString();
        Assert.assertTrue(result.contains("Label"), "label should appear");
        Assert.assertTrue(result.contains("–"), "null value should render as dash");
    }

    @Test
    public void testAppendMetricRowWithNullLabel() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        StringBuilder sb = new StringBuilder();
        invokeAppendMetricRow(reporter, sb, null, "42");
        String result = sb.toString();
        Assert.assertTrue(result.contains("42"), "value should appear");
    }

    // -------------------------------------------------------------------------
    // appendMetricCard() via reflection
    // -------------------------------------------------------------------------

    @Test
    public void testAppendMetricCardNewCode() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        StringBuilder sb = new StringBuilder();
        invokeAppendMetricCard(reporter, sb, "5", "New Bugs", true);
        Assert.assertTrue(sb.toString().contains("new-code"), "new-code CSS class should be added for new code");
    }

    @Test
    public void testAppendMetricCardNotNewCode() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        StringBuilder sb = new StringBuilder();
        invokeAppendMetricCard(reporter, sb, "10", "Bugs", false);
        Assert.assertFalse(sb.toString().contains("new-code"), "new-code CSS class should not be present");
    }

    @Test
    public void testAppendMetricCardNullValue() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        StringBuilder sb = new StringBuilder();
        invokeAppendMetricCard(reporter, sb, null, "Metric", false);
        Assert.assertTrue(sb.toString().contains("–"), "null value should render as dash");
    }

    // -------------------------------------------------------------------------
    // appendRatingCard() via reflection
    // -------------------------------------------------------------------------

    @Test
    public void testAppendRatingCardWithRating() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        StringBuilder sb = new StringBuilder();
        invokeAppendRatingCard(reporter, sb, "A", "Reliability Rating");
        String result = sb.toString();
        Assert.assertTrue(result.contains("rating-a"), "rating CSS class should be added");
        Assert.assertTrue(result.contains("Reliability Rating"), "label should appear");
    }

    @Test
    public void testAppendRatingCardNullRating() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        StringBuilder sb = new StringBuilder();
        invokeAppendRatingCard(reporter, sb, null, "Rating");
        String result = sb.toString();
        Assert.assertTrue(result.contains("–"), "null rating should render as dash");
    }

    // -------------------------------------------------------------------------
    // Abstract method stubs (should not throw) – called via reflection (protected)
    // -------------------------------------------------------------------------

    @Test
    public void testPrintFrontPageStubDoesNotThrow() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Method m = HTMLReporter.class.getDeclaredMethod("printFrontPage", Document.class, PdfWriter.class);
        m.setAccessible(true);
        m.invoke(reporter, (Object) null, (Object) null);
    }

    @Test
    public void testPrintTocTitleStubDoesNotThrow() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Method m = HTMLReporter.class.getDeclaredMethod("printTocTitle", Toc.class);
        m.setAccessible(true);
        m.invoke(reporter, new Toc());
    }

    @Test
    public void testPrintPdfBodyStubDoesNotThrow() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Method m = HTMLReporter.class.getDeclaredMethod("printPdfBody", Document.class);
        m.setAccessible(true);
        m.invoke(reporter, (Object) null);
    }

    // -------------------------------------------------------------------------
    // Accessor stubs
    // -------------------------------------------------------------------------

    @Test
    public void testGetProjectKey() {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(invokeGetProjectKey(reporter), "com.example:test");
    }

    @Test
    public void testGetProjectVersion() {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Assert.assertEquals(reporter.getProjectVersion(), "1.0");
    }

    @Test
    public void testGetLogo() throws Exception {
        HTMLReporter reporter = createReporter(Collections.emptySet(), buildOkProject());
        Method m = HTMLReporter.class.getDeclaredMethod("getLogo");
        m.setAccessible(true);
        Assert.assertNull(m.invoke(reporter));
    }

    // -------------------------------------------------------------------------
    // Factory / builder helpers
    // -------------------------------------------------------------------------

    /**
     * Creates an {@link HTMLReporter} with {@code project} pre-injected into the
     * {@code PDFReporter.project} field via reflection, so that
     * {@code super.getProject()} inside {@link HTMLReporter#getReport()} returns the
     * supplied object without making any network calls.
     */
    private HTMLReporter createReporter(Set<String> typesOfIssue, Project project) {
        Credentials creds = new Credentials("http://localhost:9000", "token");
        HTMLReporter reporter = new HTMLReporter(
                creds,
                null,                  // logo URL
                "com.example:test",
                "1.0",
                Collections.singletonList("java"),
                Collections.emptySet(), // otherMetrics
                typesOfIssue,
                leakPeriod,
                configProperties,
                langProperties,
                null);
        injectProject(reporter, project);
        return reporter;
    }

    /**
     * Injects {@code project} into the private {@code project} field of
     * {@link com.cybage.sonar.report.pdf.PDFReporter} so that
     * {@code PDFReporter.getProject()} returns it without a network call.
     */
    private static void injectProject(Object reporter, Project project) {
        try {
            java.lang.reflect.Field f =
                    com.cybage.sonar.report.pdf.PDFReporter.class.getDeclaredField("project");
            f.setAccessible(true);
            f.set(reporter, project);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject project", e);
        }
    }

    /** Builds a fully-populated {@link Project} with quality gate = OK. */
    private Project buildOkProject() {
        Project project = new Project("com.example:test", "1.0", Collections.singletonList("java"));
        project.setName("Test Project");
        project.setDescription("Test description");

        // Measures
        Measures measures = new Measures();
        addMeasure(measures, MetricKeys.BUGS, "3");
        addMeasure(measures, MetricKeys.NEW_BUGS, "1");
        addMeasure(measures, MetricKeys.RELIABILITY_RATING, "1.0");
        addMeasure(measures, MetricKeys.RELIABILITY_REMEDIATION_EFFORT, "30");
        addMeasure(measures, MetricKeys.NEW_RELIABILITY_REMEDIATION_EFFORT, "10");

        addMeasure(measures, MetricKeys.VULNERABILITIES, "2");
        addMeasure(measures, MetricKeys.NEW_VULNERABILITIES, "0");
        addMeasure(measures, MetricKeys.SECURITY_RATING, "1.0");
        addMeasure(measures, MetricKeys.SECURITY_REMEDIATION_EFFORT, "60");
        addMeasure(measures, MetricKeys.NEW_SECURITY_REMEDIATION_EFFORT, "0");

        addMeasure(measures, MetricKeys.CODE_SMELLS, "10");
        addMeasure(measures, MetricKeys.NEW_CODE_SMELLS, "2");
        addMeasure(measures, MetricKeys.SQALE_RATING, "1.0");
        addMeasure(measures, MetricKeys.SQALE_INDEX, "120");
        addMeasure(measures, MetricKeys.SQALE_DEBT_RATIO, "0.5");
        addMeasure(measures, MetricKeys.NEW_TECHNICAL_DEBT, "30");
        addMeasure(measures, MetricKeys.EFFORT_TO_REACH_MAINTAINABILITY_RATING_A, "60");

        addMeasure(measures, MetricKeys.COVERAGE, "85.0");
        addMeasure(measures, MetricKeys.LINE_COVERAGE, "87.0");
        addMeasure(measures, MetricKeys.BRANCH_COVERAGE, "80.0");
        addMeasure(measures, MetricKeys.UNCOVERED_LINES, "30");
        addMeasure(measures, MetricKeys.UNCOVERED_CONDITIONS, "10");
        addMeasure(measures, MetricKeys.LINES_TO_COVER, "200");

        addMeasure(measures, MetricKeys.DUPLICATED_LINES_DENSITY, "2.5");
        addMeasure(measures, MetricKeys.DUPLICATED_LINES, "50");
        addMeasure(measures, MetricKeys.DUPLICATED_BLOCKS, "5");
        addMeasure(measures, MetricKeys.DUPLICATED_FILES, "3");

        addMeasure(measures, MetricKeys.NCLOC, "2000");
        addMeasure(measures, MetricKeys.LINES, "2500");
        addMeasure(measures, MetricKeys.STATEMENTS, "1500");
        addMeasure(measures, MetricKeys.FUNCTIONS, "120");
        addMeasure(measures, MetricKeys.CLASSES, "30");
        addMeasure(measures, MetricKeys.FILES, "25");
        addMeasure(measures, MetricKeys.DIRECTORIES, "8");

        addMeasure(measures, MetricKeys.CLASS_COMPLEXITY, "4.2");
        addMeasure(measures, MetricKeys.FUNCTION_COMPLEXITY, "2.1");
        addMeasure(measures, MetricKeys.FILE_COMPLEXITY, "15.3");

        addMeasure(measures, MetricKeys.COMMENT_LINES, "300");
        addMeasure(measures, MetricKeys.COMMENT_LINES_DENSITY, "12.0");

        addMeasure(measures, MetricKeys.VIOLATIONS, "15");
        addMeasure(measures, MetricKeys.NEW_VIOLATIONS, "3");
        addMeasure(measures, MetricKeys.OPEN_ISSUES, "10");
        addMeasure(measures, MetricKeys.REOPENED_ISSUES, "1");
        addMeasure(measures, MetricKeys.CONFIRMED_ISSUES, "2");
        addMeasure(measures, MetricKeys.FALSE_POSITIVE_ISSUES, "1");
        addMeasure(measures, MetricKeys.WONT_FIX_ISSUES, "1");

        addMeasure(measures, MetricKeys.PROFILE, "Sonar way");

        // Period
        LeakPeriod period = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        measures.setPeriods(Collections.singletonList(period));

        // Wire period values into "new_*" measures
        wirePeriod(measures, MetricKeys.NEW_BUGS, period, "1");
        wirePeriod(measures, MetricKeys.NEW_VULNERABILITIES, period, "0");
        wirePeriod(measures, MetricKeys.NEW_CODE_SMELLS, period, "2");
        wirePeriod(measures, MetricKeys.NEW_TECHNICAL_DEBT, period, "30");
        wirePeriod(measures, MetricKeys.NEW_RELIABILITY_REMEDIATION_EFFORT, period, "10");
        wirePeriod(measures, MetricKeys.NEW_SECURITY_REMEDIATION_EFFORT, period, "0");
        wirePeriod(measures, MetricKeys.NEW_VIOLATIONS, period, "3");

        project.setMeasures(measures);

        // Quality gate OK
        project.setProjectStatus(new ProjectStatus(ProjectStatusKeys.STATUS_OK, Collections.emptyList(), Collections.emptyList()));

        // Quality profiles
        QualityProfile qp = new QualityProfile("sonar-way", "Sonar way", "java", "Java", false, true, 200L, "2024-01-01", 1L);
        project.setQualityProfiles(Collections.singletonList(qp));

        // Violations analysis lists (empty – tests empty-list branches)
        project.setMostViolatedRules(Collections.emptyList());
        project.setMostViolatedFiles(Collections.emptyList());
        project.setMostComplexFiles(Collections.emptyList());
        project.setMostDuplicatedFiles(Collections.emptyList());
        project.setIssues(Collections.emptyList());

        return project;
    }

    /** Builds a project with quality gate = ERROR and one failed condition. */
    private Project buildErrorGateProject() {
        Project project = buildOkProject();

        StatusPeriod sp = new StatusPeriod(1, "previous_version", "2024-01-01", "1.0");
        Condition failed = new Condition(
                ProjectStatusKeys.STATUS_ERROR, "coverage", "LT", 1, "80", "75", null);
        project.setProjectStatus(new ProjectStatus(
                ProjectStatusKeys.STATUS_ERROR,
                Collections.singletonList(failed),
                Collections.singletonList(sp)));
        return project;
    }

    // -------------------------------------------------------------------------
    // Measure / period helpers
    // -------------------------------------------------------------------------

    private static void addMeasure(Measures measures, String key, String value) {
        Measure m = new Measure();
        m.setValue(value);
        m.setPeriods(new ArrayList<>());
        measures.addMeasure(key, m);
    }

    private static void wirePeriod(Measures measures, String key, LeakPeriod leakPeriod, String value) {
        Measure m = measures.getMeasure(key);
        if (m == null) {
            m = new Measure();
            m.setValue(value);
            measures.addMeasure(key, m);
        }
        Period p = new Period();
        p.setIndex(leakPeriod.getIndex());
        p.setValue(value);
        List<Period> periods = m.getPeriods() == null ? new ArrayList<>() : new ArrayList<>(m.getPeriods());
        periods.add(p);
        m.setPeriods(periods);
    }

    // -------------------------------------------------------------------------
    // Lang properties stub
    // -------------------------------------------------------------------------

    private static Properties buildLangProperties() {
        Properties p = new Properties();
        p.setProperty("general.quality_profile", "Quality Profile");
        p.setProperty("general.quality_gate", "Quality Gate");
        p.setProperty("general.metric_dashboard", "Metric Dashboard");
        p.setProperty("general.violations_analysis", "Violations Analysis");
        p.setProperty("general.violations_details", "Violations Details");
        p.setProperty("general.profile_name", "Profile Name");
        p.setProperty("general.language", "Language");
        p.setProperty("general.language_name", "Language Name");
        p.setProperty("general.active_rules_count", "Active Rules");
        p.setProperty("general.most_violated_rules", "Most Violated Rules");
        p.setProperty("general.most_violated_files", "Most Violated Files");
        p.setProperty("general.most_complex_files", "Most Complex Files");
        p.setProperty("general.most_duplicated_files", "Most Duplicated Files");
        p.setProperty("general.no_violated_rules", "No violated rules");
        p.setProperty("general.no_violated_files", "No violated files");
        p.setProperty("general.no_complex_files", "No complex files");
        p.setProperty("general.no_duplicated_files", "No duplicated files");
        p.setProperty("general.no_violations", "No violations");
        p.setProperty("general.file_path", "File Path");
        p.setProperty("general.file_name", "File Name");
        p.setProperty("general.file_violations", "Violations");
        p.setProperty("general.file_complexity", "Complexity");
        p.setProperty("general.file_duplicated_lines", "Duplicated Lines");
        p.setProperty("general.rule_count", "Rule Count");
        p.setProperty("general.rule_name", "Rule Name");
        p.setProperty("general.period.previous_version", "Previous version");
        // Metric keys
        for (String key : MetricKeys.getAllMetricKeys()) {
            p.setProperty("metrics." + key, key.replace("_", " "));
        }
        // Domain names
        p.setProperty("metrics.reliability", "Reliability");
        p.setProperty("metrics.security", "Security");
        p.setProperty("metrics.maintainability", "Maintainability");
        p.setProperty("metrics.coverage", "Coverage");
        p.setProperty("metrics.duplications", "Duplications");
        p.setProperty("metrics.size", "Size");
        p.setProperty("metrics.complexity", "Complexity");
        p.setProperty("metrics.documentation", "Documentation");
        p.setProperty("metrics.issues", "Issues");
        return p;
    }

    // -------------------------------------------------------------------------
    // Reflection helpers
    // -------------------------------------------------------------------------

    private String invokeEscape(HTMLReporter reporter, String text) throws Exception {
        Method m = HTMLReporter.class.getDeclaredMethod("escape", String.class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(reporter, text);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void invokeAppendMetricRow(HTMLReporter reporter, StringBuilder sb, String label, String value)
            throws Exception {
        Method m = HTMLReporter.class.getDeclaredMethod("appendMetricRow", StringBuilder.class, String.class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(reporter, sb, label, value);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void invokeAppendMetricCard(HTMLReporter reporter, StringBuilder sb, String value, String label, boolean newCode)
            throws Exception {
        Method m = HTMLReporter.class.getDeclaredMethod("appendMetricCard", StringBuilder.class, String.class, String.class, boolean.class);
        m.setAccessible(true);
        try {
            m.invoke(reporter, sb, value, label, newCode);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void invokeAppendRatingCard(HTMLReporter reporter, StringBuilder sb, String rating, String label)
            throws Exception {
        Method m = HTMLReporter.class.getDeclaredMethod("appendRatingCard", StringBuilder.class, String.class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(reporter, sb, rating, label);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private String invokeGetProjectKey(HTMLReporter reporter) {
        try {
            Method m = HTMLReporter.class.getDeclaredMethod("getProjectKey");
            m.setAccessible(true);
            return (String) m.invoke(reporter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
