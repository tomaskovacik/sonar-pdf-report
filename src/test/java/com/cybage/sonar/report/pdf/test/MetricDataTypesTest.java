package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.util.MetricDataTypes;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class MetricDataTypesTest {

    @Test
    public void testIntConstant() {
        Assert.assertEquals(MetricDataTypes.INT, "INT");
    }

    @Test
    public void testFloatConstant() {
        Assert.assertEquals(MetricDataTypes.FLOAT, "FLOAT");
    }

    @Test
    public void testPercentConstant() {
        Assert.assertEquals(MetricDataTypes.PERCENT, "PERCENT");
    }

    @Test
    public void testBoolConstant() {
        Assert.assertEquals(MetricDataTypes.BOOL, "BOOL");
    }

    @Test
    public void testStringConstant() {
        Assert.assertEquals(MetricDataTypes.STRING, "STRING");
    }

    @Test
    public void testMillisecConstant() {
        Assert.assertEquals(MetricDataTypes.MILLISEC, "MILLISEC");
    }

    @Test
    public void testDataConstant() {
        Assert.assertEquals(MetricDataTypes.DATA, "DATA");
    }

    @Test
    public void testLevelConstant() {
        Assert.assertEquals(MetricDataTypes.LEVEL, "LEVEL");
    }

    @Test
    public void testDistribConstant() {
        Assert.assertEquals(MetricDataTypes.DISTRIB, "DISTRIB");
    }

    @Test
    public void testRatingConstant() {
        Assert.assertEquals(MetricDataTypes.RATING, "RATING");
    }

    @Test
    public void testWorkdurConstant() {
        Assert.assertEquals(MetricDataTypes.WORKDUR, "WORK_DUR");
    }
}
