package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.util.IssueTypes;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class IssueTypesTest {

    @Test
    public void testBugConstant() {
        Assert.assertEquals(IssueTypes.BUG, "BUG");
    }

    @Test
    public void testVulnerabilityConstant() {
        Assert.assertEquals(IssueTypes.VULNERABILITY, "VULNERABILITY");
    }

    @Test
    public void testCodeSmellConstant() {
        Assert.assertEquals(IssueTypes.CODE_SMELL, "CODE_SMELL");
    }
}
