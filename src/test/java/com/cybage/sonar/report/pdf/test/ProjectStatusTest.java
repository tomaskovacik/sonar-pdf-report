package com.cybage.sonar.report.pdf.test;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Condition;
import com.cybage.sonar.report.pdf.entity.ProjectStatus;
import com.cybage.sonar.report.pdf.entity.StatusPeriod;

@Test(groups = { "metrics" })
public class ProjectStatusTest {

    @Test
    public void testConstructorAndGetters() {
        Condition condition = new Condition("ERROR", "coverage", "LT", 1, "80", "75", "70");
        StatusPeriod statusPeriod = new StatusPeriod(1, "previous_version", "2024-01-01", "1.0");
        ProjectStatus ps = new ProjectStatus("ERROR", Arrays.asList(condition), Arrays.asList(statusPeriod));

        Assert.assertEquals(ps.getStatus(), "ERROR");
        Assert.assertEquals(ps.getConditions().size(), 1);
        Assert.assertEquals(ps.getStatusPeriods().size(), 1);
    }

    @Test
    public void testSetters() {
        ProjectStatus ps = new ProjectStatus("OK", null, null);
        ps.setStatus("WARN");
        Condition c = new Condition("WARN", "bugs", "GT", 0, "10", "5", "8");
        ps.setConditions(Arrays.asList(c));
        StatusPeriod sp = new StatusPeriod(1, "days", "2024-01-01", "30");
        ps.setStatusPeriods(Arrays.asList(sp));

        Assert.assertEquals(ps.getStatus(), "WARN");
        Assert.assertEquals(ps.getConditions().size(), 1);
        Assert.assertEquals(ps.getStatusPeriods().size(), 1);
    }

    @Test
    public void testToString() {
        ProjectStatus ps = new ProjectStatus("OK", null, null);
        Assert.assertNotNull(ps.toString());
        Assert.assertTrue(ps.toString().contains("OK"));
    }
}
