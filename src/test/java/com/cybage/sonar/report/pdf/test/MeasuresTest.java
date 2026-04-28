package com.cybage.sonar.report.pdf.test;

import java.util.Arrays;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Measure;
import com.cybage.sonar.report.pdf.entity.Measures;
import com.cybage.sonar.report.pdf.entity.LeakPeriod;

@Test(groups = { "metrics" })
public class MeasuresTest {

    private Measures measures;

    @BeforeMethod
    public void setUp() {
        measures = new Measures();
    }

    @Test
    public void testInitiallyEmpty() {
        Assert.assertEquals(measures.getMeasuresCount(), 0);
    }

    @Test
    public void testAddAndGetMeasure() {
        Measure m = new Measure();
        m.setMetric("bugs");
        m.setValue("5");
        measures.addMeasure("bugs", m);
        Measure retrieved = measures.getMeasure("bugs");
        Assert.assertNotNull(retrieved);
        Assert.assertEquals(retrieved.getValue(), "5");
    }

    @Test
    public void testContainsMeasureTrue() {
        measures.addMeasure("coverage", new Measure());
        Assert.assertTrue(measures.containsMeasure("coverage"));
    }

    @Test
    public void testContainsMeasureFalse() {
        Assert.assertFalse(measures.containsMeasure("nonexistent"));
    }

    @Test
    public void testGetMeasuresCount() {
        measures.addMeasure("bugs", new Measure());
        measures.addMeasure("coverage", new Measure());
        Assert.assertEquals(measures.getMeasuresCount(), 2);
    }

    @Test
    public void testGetMeasuresKeys() {
        measures.addMeasure("bugs", new Measure());
        measures.addMeasure("coverage", new Measure());
        Assert.assertEquals(measures.getMeasuresKeys().size(), 2);
        Assert.assertTrue(measures.getMeasuresKeys().contains("bugs"));
        Assert.assertTrue(measures.getMeasuresKeys().contains("coverage"));
    }

    @Test
    public void testGetMeasureReturnsNullForMissing() {
        Assert.assertNull(measures.getMeasure("missing_key"));
    }

    @Test
    public void testGetPeriodByIndex() {
        LeakPeriod p1 = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        LeakPeriod p2 = new LeakPeriod(2, "days", "2024-02-01", "30");
        measures.setPeriods(Arrays.asList(p1, p2));
        Optional<LeakPeriod> found = measures.getPeriod(1);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get().getMode(), "previous_version");
    }

    @Test
    public void testGetPeriodByIndexNotFound() {
        LeakPeriod p1 = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        measures.setPeriods(Arrays.asList(p1));
        Optional<LeakPeriod> found = measures.getPeriod(99);
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testGetPeriodByMode() {
        LeakPeriod p1 = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        LeakPeriod p2 = new LeakPeriod(2, "days", "2024-02-01", "30");
        measures.setPeriods(Arrays.asList(p1, p2));
        Optional<LeakPeriod> found = measures.getPeriod("days");
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get().getIndex(), Integer.valueOf(2));
    }

    @Test
    public void testGetPeriodByModeNotFound() {
        LeakPeriod p1 = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        measures.setPeriods(Arrays.asList(p1));
        Optional<LeakPeriod> found = measures.getPeriod("unknown_mode");
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testGetAndSetPeriods() {
        LeakPeriod p = new LeakPeriod(1, "previous_version", "2024-01-01", "1.0");
        measures.setPeriods(Arrays.asList(p));
        Assert.assertEquals(measures.getPeriods().size(), 1);
        Assert.assertEquals(measures.getPeriods().get(0).getIndex(), Integer.valueOf(1));
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(measures.toString());
    }
}
