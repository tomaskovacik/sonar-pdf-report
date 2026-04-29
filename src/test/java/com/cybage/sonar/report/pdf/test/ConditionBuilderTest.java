package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.ConditionBuilder;
import com.cybage.sonar.report.pdf.entity.Condition;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class ConditionBuilderTest {

    @Test
    public void testBuilderSetsAllFields() {
        Condition c = new ConditionBuilder()
                .setStatus("ERROR")
                .setMetricKey("coverage")
                .setComparator("LT")
                .setPeriodIndex(1)
                .setErrorThreshold("80")
                .setActualValue("70")
                .setWarningThreshold("90")
                .createCondition();

        Assert.assertEquals(c.getStatus(), "ERROR");
        Assert.assertEquals(c.getMetricKey(), "coverage");
        Assert.assertEquals(c.getComparator(), "LT");
        Assert.assertEquals(c.getPeriodIndex(), Integer.valueOf(1));
        Assert.assertEquals(c.getErrorThreshold(), "80");
        Assert.assertEquals(c.getActualValue(), "70");
        Assert.assertEquals(c.getWarningThreshold(), "90");
    }

    @Test
    public void testBuilderSettersReturnSameInstance() {
        ConditionBuilder b = new ConditionBuilder();
        Assert.assertSame(b.setStatus("OK"), b);
        Assert.assertSame(b.setMetricKey("bugs"), b);
        Assert.assertSame(b.setComparator("GT"), b);
        Assert.assertSame(b.setPeriodIndex(0), b);
        Assert.assertSame(b.setErrorThreshold("10"), b);
        Assert.assertSame(b.setActualValue("5"), b);
        Assert.assertSame(b.setWarningThreshold("8"), b);
    }

    @Test
    public void testBuilderWithNullsDoesNotThrow() {
        Condition c = new ConditionBuilder().createCondition();
        Assert.assertNotNull(c);
        Assert.assertNull(c.getStatus());
        Assert.assertNull(c.getMetricKey());
    }

    @Test
    public void testBuilderCreatesTwoIndependentInstances() {
        Condition c1 = new ConditionBuilder().setStatus("OK").createCondition();
        Condition c2 = new ConditionBuilder().setStatus("ERROR").createCondition();
        Assert.assertFalse(c1.getStatus().equals(c2.getStatus()));
    }
}
