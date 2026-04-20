package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.util.ProjectStatusKeys;

@Test(groups = { "metrics" })
public class ProjectStatusKeysTest {

    @Test
    public void testGetComparatorEQ() {
        Assert.assertEquals(ProjectStatusKeys.getComparatorAsString("EQ"), "=");
    }

    @Test
    public void testGetComparatorNE() {
        Assert.assertEquals(ProjectStatusKeys.getComparatorAsString("NE"), "!=");
    }

    @Test
    public void testGetComparatorLT() {
        Assert.assertEquals(ProjectStatusKeys.getComparatorAsString("LT"), "<");
    }

    @Test
    public void testGetComparatorGT() {
        Assert.assertEquals(ProjectStatusKeys.getComparatorAsString("GT"), ">");
    }

    @Test
    public void testGetComparatorUnknown() {
        Assert.assertNull(ProjectStatusKeys.getComparatorAsString("UNKNOWN"));
    }

    @Test
    public void testGetStatusAsStringOK() {
        Assert.assertEquals(ProjectStatusKeys.getStatusAsString(ProjectStatusKeys.STATUS_OK), "Passed");
    }

    @Test
    public void testGetStatusAsStringError() {
        Assert.assertEquals(ProjectStatusKeys.getStatusAsString(ProjectStatusKeys.STATUS_ERROR), "Failed");
    }

    @Test
    public void testGetStatusAsStringUnknown() {
        String result = ProjectStatusKeys.getStatusAsString("UNKNOWN_STATUS");
        Assert.assertTrue(result.startsWith("Undefined!"));
    }
}
