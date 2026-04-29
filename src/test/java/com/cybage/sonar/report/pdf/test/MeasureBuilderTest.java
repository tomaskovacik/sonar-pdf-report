package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.MeasureBuilder;
import com.cybage.sonar.report.pdf.entity.Measure;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Measures;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class MeasureBuilderTest {

    private static Common.Metric metric(String key, String name, String type, String domain) {
        return Common.Metric.newBuilder()
                .setKey(key).setName(name).setType(type).setDomain(domain)
                .build();
    }

    @Test
    public void testInitFromNodeBasicFields() {
        Measures.Measure node = Measures.Measure.newBuilder()
                .setMetric("bugs").setValue("5").build();
        Common.Metric m = metric("bugs", "Bugs", "INT", "Reliability");

        Measure result = MeasureBuilder.initFromNode(node, m);

        Assert.assertEquals(result.getMetric(), "bugs");
        Assert.assertEquals(result.getValue(), "5");
        Assert.assertEquals(result.getDataType(), "INT");
        Assert.assertEquals(result.getDomain(), "Reliability");
    }

    @Test
    public void testInitFromNodeNoPeriod() {
        Measures.Measure node = Measures.Measure.newBuilder()
                .setMetric("coverage").setValue("82.5").build();
        Common.Metric m = metric("coverage", "Coverage", "PERCENT", "Coverage");

        Measure result = MeasureBuilder.initFromNode(node, m);

        Assert.assertNotNull(result.getPeriods());
        Assert.assertTrue(result.getPeriods().isEmpty(), "no period should be present");
    }

    @Test
    public void testInitFromNodeWithPeriod() {
        Measures.PeriodValue pv = Measures.PeriodValue.newBuilder()
                .setIndex(1).setValue("3").build();
        Measures.Measure node = Measures.Measure.newBuilder()
                .setMetric("new_bugs").setPeriod(pv).build();
        Common.Metric m = metric("new_bugs", "New Bugs", "INT", "Reliability");

        Measure result = MeasureBuilder.initFromNode(node, m);

        Assert.assertEquals(result.getPeriods().size(), 1);
        Assert.assertEquals(result.getPeriods().get(0).getIndex(), Integer.valueOf(1));
        Assert.assertEquals(result.getPeriods().get(0).getValue(), "3");
    }

    @Test
    public void testInitFromNodeMetricName() {
        Measures.Measure node = Measures.Measure.newBuilder().setMetric("sqale_index").build();
        Common.Metric m = metric("sqale_index", "Technical Debt", "WORK_DUR", "Maintainability");

        Measure result = MeasureBuilder.initFromNode(node, m);

        Assert.assertEquals(result.getMetricTitle(), "Technical Debt");
    }

    @Test
    public void testInitFromNodeHigherValuesAreBetter() {
        Measures.Measure node = Measures.Measure.newBuilder().setMetric("coverage").build();
        Common.Metric m = Common.Metric.newBuilder()
                .setKey("coverage").setName("Coverage").setType("PERCENT")
                .setHigherValuesAreBetter(true).build();

        Measure result = MeasureBuilder.initFromNode(node, m);

        Assert.assertTrue(result.getHigherValuesAreBetter());
    }
}
