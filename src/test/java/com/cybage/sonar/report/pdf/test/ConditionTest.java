package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Condition;

@Test(groups = { "metrics" })
public class ConditionTest {

    @Test
    public void testConstructorAndGetters() {
        Condition c = new Condition("ERROR", "coverage", "LT", 1, "80", "75", "70");
        Assert.assertEquals(c.getStatus(), "ERROR");
        Assert.assertEquals(c.getMetricKey(), "coverage");
        Assert.assertEquals(c.getComparator(), "LT");
        Assert.assertEquals(c.getPeriodIndex(), Integer.valueOf(1));
        Assert.assertEquals(c.getErrorThreshold(), "80");
        Assert.assertEquals(c.getActualValue(), "75");
        Assert.assertEquals(c.getWarningThreshold(), "70");
    }

    @Test
    public void testSetters() {
        Condition c = new Condition("OK", "bugs", "GT", 0, "10", "5", "8");
        c.setStatus("WARN");
        c.setMetricKey("vulnerabilities");
        c.setComparator("EQ");
        c.setPeriodIndex(2);
        c.setErrorThreshold("20");
        c.setActualValue("15");
        c.setWarningThreshold("18");

        Assert.assertEquals(c.getStatus(), "WARN");
        Assert.assertEquals(c.getMetricKey(), "vulnerabilities");
        Assert.assertEquals(c.getComparator(), "EQ");
        Assert.assertEquals(c.getPeriodIndex(), Integer.valueOf(2));
        Assert.assertEquals(c.getErrorThreshold(), "20");
        Assert.assertEquals(c.getActualValue(), "15");
        Assert.assertEquals(c.getWarningThreshold(), "18");
    }

    @Test
    public void testToString() {
        Condition c = new Condition("OK", "bugs", "GT", 1, "10", "5", "8");
        String str = c.toString();
        Assert.assertTrue(str.contains("OK"));
        Assert.assertTrue(str.contains("bugs"));
    }
}
