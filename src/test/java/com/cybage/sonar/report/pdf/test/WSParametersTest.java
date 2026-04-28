package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.WSParameters;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class WSParametersTest {

    @Test
    public void testMetricsConstant() {
        Assert.assertEquals(WSParameters.METRICS, "metrics");
    }

    @Test
    public void testPeriodConstant() {
        Assert.assertEquals(WSParameters.PERIOD, "period");
    }
}
