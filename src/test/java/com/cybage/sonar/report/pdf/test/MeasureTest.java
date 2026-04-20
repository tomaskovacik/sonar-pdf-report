package com.cybage.sonar.report.pdf.test;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Measure;
import com.cybage.sonar.report.pdf.entity.Period;

@Test(groups = { "metrics" })
public class MeasureTest {

    @Test
    public void testDefaultConstructor() {
        Measure m = new Measure();
        Assert.assertNull(m.getMetric());
        Assert.assertNull(m.getValue());
        Assert.assertNull(m.getMetricTitle());
        Assert.assertNull(m.getDataType());
        Assert.assertNull(m.getDomain());
        Assert.assertNull(m.getHigherValuesAreBetter());
        Assert.assertNull(m.getPeriods());
    }

    @Test
    public void testFullConstructor() {
        List<Period> periods = Arrays.asList(new Period(1, "2.0"));
        Measure m = new Measure("bugs", "5", "Bugs", "INT", "Reliability", false, periods);
        Assert.assertEquals(m.getMetric(), "bugs");
        Assert.assertEquals(m.getValue(), "5");
        Assert.assertEquals(m.getMetricTitle(), "Bugs");
        Assert.assertEquals(m.getDataType(), "INT");
        Assert.assertEquals(m.getDomain(), "Reliability");
        Assert.assertFalse(m.getHigherValuesAreBetter());
        Assert.assertEquals(m.getPeriods().size(), 1);
    }

    @Test
    public void testSetters() {
        Measure m = new Measure();
        List<Period> periods = Arrays.asList(new Period(1, "v1"));
        m.setMetric("coverage");
        m.setValue("85.0");
        m.setMetricTitle("Coverage");
        m.setDataType("PERCENT");
        m.setDomain("Coverage");
        m.setHigherValuesAreBetter(true);
        m.setPeriods(periods);

        Assert.assertEquals(m.getMetric(), "coverage");
        Assert.assertEquals(m.getValue(), "85.0");
        Assert.assertEquals(m.getMetricTitle(), "Coverage");
        Assert.assertEquals(m.getDataType(), "PERCENT");
        Assert.assertEquals(m.getDomain(), "Coverage");
        Assert.assertTrue(m.getHigherValuesAreBetter());
        Assert.assertEquals(m.getPeriods().size(), 1);
    }

    @Test
    public void testToString() {
        Measure m = new Measure("bugs", "5", "Bugs", "INT", "Reliability", false, null);
        Assert.assertNotNull(m.toString());
        Assert.assertTrue(m.toString().contains("bugs"));
    }
}
