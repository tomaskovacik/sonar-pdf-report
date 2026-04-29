package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.entity.FileInfo;
import com.cybage.sonar.report.pdf.entity.Issue;
import com.cybage.sonar.report.pdf.entity.Project;
import com.cybage.sonar.report.pdf.entity.Rule;
import com.cybage.sonar.report.pdf.util.Credentials;
import com.cybage.sonar.report.pdf.util.SonarIssuesDumper;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Test(groups = {"metrics"})
public class SonarIssuesDumperTest {

    private File outputDir;
    private Credentials credentials;

    @BeforeMethod
    public void setUp() throws IOException {
        outputDir   = Files.createTempDirectory("sonar-dumper-test").toFile();
        credentials = new Credentials("http://localhost:9000", "token");
    }

    // ---- dump() writes valid JSON file ----

    @Test
    public void testDumpCreatesFile() {
        Project project = minimalProject();
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(new File(outputDir, "sonar-issues.json").exists());
    }

    @Test
    public void testDumpFileIsValidJsonObject() throws IOException {
        Project project = minimalProject();
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        String json = readJson();
        Assert.assertTrue(json.startsWith("{"), "should start with {");
        Assert.assertTrue(json.trim().endsWith("}"), "should end with }");
    }

    @Test
    public void testDumpContainsProjectKey() throws IOException {
        Project project = minimalProject();
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(readJson().contains("\"project\": \"test:key\""));
    }

    @Test
    public void testDumpContainsVersion() throws IOException {
        Project project = minimalProject();
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(readJson().contains("\"version\": \"1.0\""));
    }

    @Test
    public void testDumpWithIssuesContainsIssueData() throws IOException {
        Project project = minimalProject();
        project.setIssues(List.of(
                new Issue("Foo.java", "src/Foo.java", "MAJOR", 42, "OPEN", "Some problem", "BUG", "5min")));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        String json = readJson();
        Assert.assertTrue(json.contains("\"severity\": \"MAJOR\""));
        Assert.assertTrue(json.contains("\"component\": \"Foo.java\""));
        Assert.assertTrue(json.contains("\"message\": \"Some problem\""));
    }

    @Test
    public void testDumpWithNoIssuesWritesEmptyArray() throws IOException {
        Project project = minimalProject();
        project.setIssues(Collections.emptyList());
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(readJson().contains("\"issues\": [],"));
    }

    @Test
    public void testDumpWithMultipleIssuesAllPresent() throws IOException {
        Project project = minimalProject();
        project.setIssues(Arrays.asList(
                new Issue("A.java", "src/A.java", "CRITICAL", 1, "OPEN", "msg1", "BUG", "10min"),
                new Issue("B.java", "src/B.java", "MINOR",    2, "OPEN", "msg2", "CODE_SMELL", "2min")));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        String json = readJson();
        Assert.assertTrue(json.contains("\"component\": \"A.java\""));
        Assert.assertTrue(json.contains("\"component\": \"B.java\""));
    }

    @Test
    public void testDumpWithRulesContainsRuleData() throws IOException {
        Project project = minimalProject();
        project.setMostViolatedRules(List.of(
                new Rule("java:S001", "Naming", 5L, "java", "MAJOR")));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        String json = readJson();
        Assert.assertTrue(json.contains("\"key\": \"java:S001\""));
        Assert.assertTrue(json.contains("\"severity\": \"MAJOR\""));
    }

    @Test
    public void testDumpDeduplicatesAllSeverityRules() throws IOException {
        Project project = minimalProject();
        project.setMostViolatedRules(Arrays.asList(
                new Rule("java:S001", "Rule", 10L, "java", "ALL"),
                new Rule("java:S001", "Rule", 5L,  "java", "MAJOR")));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        String json = readJson();
        // "ALL" severity entry must be filtered out; MAJOR kept
        Assert.assertFalse(json.contains("\"severity\": \"ALL\""), "ALL severity should be filtered");
        Assert.assertTrue(json.contains("\"severity\": \"MAJOR\""));
    }

    @Test
    public void testDumpDeduplicatesDuplicateRuleKeys() throws IOException {
        Project project = minimalProject();
        project.setMostViolatedRules(Arrays.asList(
                new Rule("java:S001", "Rule", 5L, "java", "MAJOR"),
                new Rule("java:S001", "Rule", 3L, "java", "MAJOR")));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        String json = readJson();
        // key should appear exactly once
        int count = 0;
        int idx   = 0;
        while ((idx = json.indexOf("\"key\": \"java:S001\"", idx)) >= 0) { count++; idx++; }
        Assert.assertEquals(count, 1, "duplicate rule key should appear only once");
    }

    @Test
    public void testDumpWithNullRulesWritesEmptyArray() throws IOException {
        Project project = minimalProject();
        project.setMostViolatedRules(null);
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(readJson().contains("\"rules\": [],"));
    }

    @Test
    public void testDumpWithMostViolatedFilesPresent() throws IOException {
        Project project = minimalProject();
        FileInfo fi = new FileInfo();
        fi.setName("Foo.java");
        fi.setPath("src/Foo.java");
        fi.setViolations("10");
        project.setMostViolatedFiles(List.of(fi));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(readJson().contains("\"name\": \"Foo.java\""));
    }

    @Test
    public void testDumpEscapesSpecialCharsInMessage() throws IOException {
        Project project = minimalProject();
        project.setIssues(List.of(
                new Issue("Foo.java", "src/Foo.java", "MAJOR", 1, "OPEN", "Say \"hello\"", "BUG", "1min")));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(readJson().contains("\\\"hello\\\""), "quotes should be escaped");
    }

    @Test
    public void testDumpHandlesNullLineGracefully() throws IOException {
        Project project = minimalProject();
        project.setIssues(List.of(
                new Issue("Foo.java", "src/Foo.java", "MAJOR", null, "OPEN", "msg", "BUG", "1min")));
        SonarIssuesDumper.dump(project, credentials, null, outputDir);
        Assert.assertTrue(readJson().contains("\"line\": null"));
    }

    @Test
    public void testDumpToUnwritableDirectoryDoesNotThrow() {
        File unwritable = new File("/nonexistent/path/that/does/not/exist");
        Project project = minimalProject();
        // should log a warning and not throw
        SonarIssuesDumper.dump(project, credentials, null, unwritable);
    }

    // ---- helpers ----

    private Project minimalProject() {
        Project p = new Project("test:key", "1.0", Collections.singletonList("java"));
        p.setName("Test Project");
        p.setIssues(Collections.emptyList());
        p.setMostViolatedRules(Collections.emptyList());
        p.setMostViolatedFiles(Collections.emptyList());
        p.setMostComplexFiles(Collections.emptyList());
        p.setMostDuplicatedFiles(Collections.emptyList());
        return p;
    }

    private String readJson() throws IOException {
        return Files.readString(new File(outputDir, "sonar-issues.json").toPath(), StandardCharsets.UTF_8);
    }
}
