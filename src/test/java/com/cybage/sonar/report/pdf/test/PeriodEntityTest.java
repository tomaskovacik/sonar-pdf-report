package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Period;
import com.cybage.sonar.report.pdf.entity.LeakPeriod;

@Test(groups = { "metrics" })
public class PeriodEntityTest {

    @Test
    public void testPeriodDefaultConstructor() {
        Period p = new Period();
        Assert.assertEquals(p.getIndex(), Integer.valueOf(-1));
        Assert.assertNull(p.getValue());
    }

    @Test
    public void testPeriodFullConstructor() {
        Period p = new Period(2, "1.5");
        Assert.assertEquals(p.getIndex(), Integer.valueOf(2));
        Assert.assertEquals(p.getValue(), "1.5");
    }

    @Test
    public void testPeriodSetters() {
        Period p = new Period();
        p.setIndex(3);
        p.setValue("2.0");
        Assert.assertEquals(p.getIndex(), Integer.valueOf(3));
        Assert.assertEquals(p.getValue(), "2.0");
    }

    @Test
    public void testPeriodToString() {
        Period p = new Period(1, "1.0");
        Assert.assertNotNull(p.toString());
        Assert.assertTrue(p.toString().contains("1.0"));
    }

    @Test
    public void testPeriod_Constructor() {
        LeakPeriod p = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        Assert.assertEquals(p.getIndex(), Integer.valueOf(1));
        Assert.assertEquals(p.getMode(), "previous_version");
        Assert.assertEquals(p.getDate(), "2024-01-01");
        Assert.assertEquals(p.getParameter(), "1.0");
    }

    @Test
    public void testPeriod_Setters() {
        LeakPeriod p = new LeakPeriod(1, "days", "2024-01-01", "30");
        p.setIndex(2);
        p.setMode("previous_version");
        p.setDate("2024-06-01");
        p.setParameter("1.5");
        Assert.assertEquals(p.getIndex(), Integer.valueOf(2));
        Assert.assertEquals(p.getMode(), "previous_version");
        Assert.assertEquals(p.getDate(), "2024-06-01");
        Assert.assertEquals(p.getParameter(), "1.5");
    }

    @Test
    public void testPeriod_ToString() {
        LeakPeriod p = new LeakPeriod(1, "days", "2024-01-01", "30");
        Assert.assertNotNull(p.toString());
        Assert.assertTrue(p.toString().contains("days"));
    }
}
