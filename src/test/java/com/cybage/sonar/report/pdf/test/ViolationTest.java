package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Priority;
import com.cybage.sonar.report.pdf.entity.Violation;

@Test(groups = { "metrics" })
public class ViolationTest {

    @Test
    public void testConstructorAndGetters() {
        Violation v = new Violation("10", "MyFile.java", "some source code");
        Assert.assertEquals(v.getLine(), "10");
        Assert.assertEquals(v.getResource(), "MyFile.java");
        Assert.assertEquals(v.getSource(), "some source code");
    }

    @Test
    public void testSetters() {
        Violation v = new Violation("1", "A.java", "src");
        v.setLine("20");
        v.setResource("B.java");
        v.setSource("updated source");
        Assert.assertEquals(v.getLine(), "20");
        Assert.assertEquals(v.getResource(), "B.java");
        Assert.assertEquals(v.getSource(), "updated source");
    }

    @Test
    public void testGetViolationLevelByKeyInfo() {
        Assert.assertEquals(Violation.getViolationLevelByKey(Priority.INFO), "info_violations");
    }

    @Test
    public void testGetViolationLevelByKeyMinor() {
        Assert.assertEquals(Violation.getViolationLevelByKey(Priority.MINOR), "minor_violations");
    }

    @Test
    public void testGetViolationLevelByKeyMajor() {
        Assert.assertEquals(Violation.getViolationLevelByKey(Priority.MAJOR), "major_violations");
    }

    @Test
    public void testGetViolationLevelByKeyCritical() {
        Assert.assertEquals(Violation.getViolationLevelByKey(Priority.CRITICAL), "critical_violations");
    }

    @Test
    public void testGetViolationLevelByKeyBlocker() {
        Assert.assertEquals(Violation.getViolationLevelByKey(Priority.BLOCKER), "blocker_violations");
    }

    @Test
    public void testGetViolationLevelByKeyUnknown() {
        Assert.assertNull(Violation.getViolationLevelByKey("UNKNOWN"));
    }
}
