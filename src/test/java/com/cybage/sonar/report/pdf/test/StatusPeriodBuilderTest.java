package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.StatusPeriodBuilder;
import com.cybage.sonar.report.pdf.entity.StatusPeriod;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class StatusPeriodBuilderTest {

    @Test
    public void testBuilderSetsAllFields() {
        StatusPeriod sp = new StatusPeriodBuilder()
                .setIndex(1)
                .setMode("previous_version")
                .setDate("2024-01-01")
                .setParameter("1.0")
                .createStatusPeriod();

        Assert.assertEquals(sp.getIndex(), Integer.valueOf(1));
        Assert.assertEquals(sp.getMode(), "previous_version");
        Assert.assertEquals(sp.getDate(), "2024-01-01");
        Assert.assertEquals(sp.getParameter(), "1.0");
    }

    @Test
    public void testBuilderSettersReturnSameInstance() {
        StatusPeriodBuilder b = new StatusPeriodBuilder();
        Assert.assertSame(b.setIndex(1), b);
        Assert.assertSame(b.setMode("days"), b);
        Assert.assertSame(b.setDate("2024-01-01"), b);
        Assert.assertSame(b.setParameter("30"), b);
    }

    @Test
    public void testBuilderWithNullsDoesNotThrow() {
        StatusPeriod sp = new StatusPeriodBuilder().createStatusPeriod();
        Assert.assertNotNull(sp);
        Assert.assertNull(sp.getMode());
    }
}
