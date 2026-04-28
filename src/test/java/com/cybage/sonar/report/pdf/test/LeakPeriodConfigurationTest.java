package com.cybage.sonar.report.pdf.test;

import java.util.Arrays;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import com.cybage.sonar.report.pdf.entity.Measures;
import com.cybage.sonar.report.pdf.entity.LeakPeriod;

@Test(groups = { "metrics" })
public class LeakPeriodConfigurationTest {

    @Test
    public void testGetPeriodByConfiguredMode() {
        LeakPeriodConfiguration config = new LeakPeriodConfiguration();
        config.update("previous_version");

        Measures measures = new Measures();
        LeakPeriod p1 = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        LeakPeriod p2 = new LeakPeriod(2, "days", "2024-02-01", "30");
        measures.setPeriods(Arrays.asList(p1, p2));

        Optional<LeakPeriod> period = config.getPeriod(measures);
        Assert.assertTrue(period.isPresent());
        Assert.assertEquals(period.get().getMode(), "previous_version");
    }

    @Test
    public void testGetPeriodWithoutConfigurationUsesFirstPeriod() {
        LeakPeriodConfiguration config = new LeakPeriodConfiguration();

        Measures measures = new Measures();
        LeakPeriod p = new LeakPeriod(1, "days", "2024-01-01", "30");
        measures.setPeriods(Arrays.asList(p));

        Optional<LeakPeriod> period = config.getPeriod(measures);
        Assert.assertTrue(period.isPresent());
        Assert.assertEquals(period.get().getMode(), "days");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetPeriodThrowsWhenNoPeriods() {
        LeakPeriodConfiguration config = new LeakPeriodConfiguration();
        Measures measures = new Measures();
        measures.setPeriods(Arrays.asList());
        config.getPeriod(measures);
    }

    @Test
    public void testToString() {
        LeakPeriodConfiguration config = new LeakPeriodConfiguration();
        config.update("previous_version");
        Assert.assertTrue(config.toString().contains("previous_version"));
    }

    @Test
    public void testToStringWithNoConfig() {
        LeakPeriodConfiguration config = new LeakPeriodConfiguration();
        Assert.assertNotNull(config.toString());
    }
}
