package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.util.MetricDataTypes;
import com.cybage.sonar.report.pdf.util.SonarUtil;

@Test(groups = { "metrics" })
public class SonarUtilTest {

    @Test
    public void testGetWorkDurConversionMinutes() {
        Assert.assertEquals(SonarUtil.getWorkDurConversion(30), "30min");
    }

    @Test
    public void testGetWorkDurConversionZeroMinutes() {
        Assert.assertEquals(SonarUtil.getWorkDurConversion(0), "0min");
    }

    @Test
    public void testGetWorkDurConversionExactlyOneHour() {
        Assert.assertEquals(SonarUtil.getWorkDurConversion(60), "1h 0min");
    }

    @Test
    public void testGetWorkDurConversionHoursAndMinutes() {
        Assert.assertEquals(SonarUtil.getWorkDurConversion(90), "1h 30min");
    }

    @Test
    public void testGetWorkDurConversionJustBelowEightHours() {
        Assert.assertEquals(SonarUtil.getWorkDurConversion(479), "7h 59min");
    }

    @Test
    public void testGetWorkDurConversionExactlyEightHours() {
        // 480 minutes = 1 day, 0 hours, 0 min
        Assert.assertEquals(SonarUtil.getWorkDurConversion(480), "1d 0h 0min");
    }

    @Test
    public void testGetWorkDurConversionMoreThanOneDay() {
        // 960 minutes = 2 days, 0 hours, 0 min
        Assert.assertEquals(SonarUtil.getWorkDurConversion(960), "2d 0h 0min");
    }

    @Test
    public void testGetWorkDurConversionDaysHoursMinutes() {
        // 570 minutes = 1 day (480), remainder = 90 -> 1h 30min
        Assert.assertEquals(SonarUtil.getWorkDurConversion(570), "1d 1h 30min");
    }

    @Test
    public void testGetFormattedValueWorkDur() {
        Assert.assertEquals(SonarUtil.getFormattedValue("30", MetricDataTypes.WORKDUR), "30min");
    }

    @Test
    public void testGetFormattedValuePercent() {
        Assert.assertEquals(SonarUtil.getFormattedValue("85.5", MetricDataTypes.PERCENT), "85.5%");
    }

    @Test
    public void testGetFormattedValueRating() {
        Assert.assertEquals(SonarUtil.getFormattedValue("1.0", MetricDataTypes.RATING), "A");
    }

    @Test
    public void testGetFormattedValueBoolTrue() {
        Assert.assertEquals(SonarUtil.getFormattedValue("TRUE", MetricDataTypes.BOOL), "TRUE");
    }

    @Test
    public void testGetFormattedValueBoolFalse() {
        Assert.assertEquals(SonarUtil.getFormattedValue("false", MetricDataTypes.BOOL), "FALSE");
    }

    @Test
    public void testGetFormattedValueDefault() {
        Assert.assertEquals(SonarUtil.getFormattedValue("42", MetricDataTypes.INT), "42");
    }

    @Test
    public void testGetFormattedValueMillisec() {
        // 60000 ms / 1000 / 60 = 1 minute
        Assert.assertEquals(SonarUtil.getFormattedValue("60000", MetricDataTypes.MILLISEC), "1min");
    }
}
