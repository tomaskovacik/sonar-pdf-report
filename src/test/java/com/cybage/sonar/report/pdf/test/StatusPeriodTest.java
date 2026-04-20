package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.StatusPeriod;

@Test(groups = { "metrics" })
public class StatusPeriodTest {

    @Test
    public void testDefaultConstructor() {
        StatusPeriod sp = new StatusPeriod();
        Assert.assertNull(sp.getIndex());
        Assert.assertNull(sp.getMode());
        Assert.assertNull(sp.getDate());
        Assert.assertNull(sp.getParameter());
    }

    @Test
    public void testFullConstructor() {
        StatusPeriod sp = new StatusPeriod(1, "previous_version", "2024-01-01", "1.0");
        Assert.assertEquals(sp.getIndex(), Integer.valueOf(1));
        Assert.assertEquals(sp.getMode(), "previous_version");
        Assert.assertEquals(sp.getDate(), "2024-01-01");
        Assert.assertEquals(sp.getParameter(), "1.0");
    }

    @Test
    public void testSetters() {
        StatusPeriod sp = new StatusPeriod();
        sp.setIndex(2);
        sp.setMode("days");
        sp.setDate("2024-06-01");
        sp.setParameter("30");

        Assert.assertEquals(sp.getIndex(), Integer.valueOf(2));
        Assert.assertEquals(sp.getMode(), "days");
        Assert.assertEquals(sp.getDate(), "2024-06-01");
        Assert.assertEquals(sp.getParameter(), "30");
    }

    @Test
    public void testToString() {
        StatusPeriod sp = new StatusPeriod(1, "previous_version", "2024-01-01", "1.0");
        Assert.assertNotNull(sp.toString());
        Assert.assertTrue(sp.toString().contains("previous_version"));
    }
}
