package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Issue;

@Test(groups = { "metrics" })
public class IssueTest {

    @Test
    public void testConstructorAndGetters() {
        Issue issue = new Issue("MyProject:MyFile.java", "src/MyFile.java", "MAJOR", 42, "OPEN", "Null pointer dereference", "BUG", "30min");
        Assert.assertEquals(issue.getComponent(), "MyProject:MyFile.java");
        Assert.assertEquals(issue.getComponentPath(), "src/MyFile.java");
        Assert.assertEquals(issue.getSeverity(), "MAJOR");
        Assert.assertEquals(issue.getLine(), Integer.valueOf(42));
        Assert.assertEquals(issue.getStatus(), "OPEN");
        Assert.assertEquals(issue.getMessage(), "Null pointer dereference");
        Assert.assertEquals(issue.getType(), "BUG");
        Assert.assertEquals(issue.getEffort(), "30min");
    }

    @Test
    public void testSetters() {
        Issue issue = new Issue("A", "path/A.java", "INFO", 1, "CLOSED", "msg", "CODE_SMELL", "5min");
        issue.setComponent("B");
        issue.setComponentPath("path/B.java");
        issue.setSeverity("CRITICAL");
        issue.setLine(100);
        issue.setStatus("REOPENED");
        issue.setMessage("updated message");
        issue.setType("VULNERABILITY");
        issue.setEffort("1h 0min");

        Assert.assertEquals(issue.getComponent(), "B");
        Assert.assertEquals(issue.getComponentPath(), "path/B.java");
        Assert.assertEquals(issue.getSeverity(), "CRITICAL");
        Assert.assertEquals(issue.getLine(), Integer.valueOf(100));
        Assert.assertEquals(issue.getStatus(), "REOPENED");
        Assert.assertEquals(issue.getMessage(), "updated message");
        Assert.assertEquals(issue.getType(), "VULNERABILITY");
        Assert.assertEquals(issue.getEffort(), "1h 0min");
    }

    @Test
    public void testToString() {
        Issue issue = new Issue("A", "path/A.java", "INFO", 1, "OPEN", "msg", "BUG", "5min");
        Assert.assertNotNull(issue.toString());
        Assert.assertTrue(issue.toString().contains("BUG"));
    }
}
