package com.cybage.sonar.report.pdf.test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.util.MetricKeys;

@Test(groups = { "metrics" })
public class MetricKeysTest {

    @Test
    public void testGetAllMetricKeysNotEmpty() {
        Set<String> keys = MetricKeys.getAllMetricKeys();
        Assert.assertNotNull(keys);
        Assert.assertTrue(keys.size() > 0);
    }

    @Test
    public void testGetAllMetricKeysContainsBugs() {
        Set<String> keys = MetricKeys.getAllMetricKeys();
        Assert.assertTrue(keys.contains(MetricKeys.BUGS));
    }

    @Test
    public void testGetAllMetricKeysContainsCoverage() {
        Set<String> keys = MetricKeys.getAllMetricKeys();
        Assert.assertTrue(keys.contains(MetricKeys.COVERAGE));
    }

    @Test
    public void testGetAllMetricKeysContainsVulnerabilities() {
        Set<String> keys = MetricKeys.getAllMetricKeys();
        Assert.assertTrue(keys.contains(MetricKeys.VULNERABILITIES));
    }

    @Test
    public void testGetAllMetricKeysAsString() {
        List<String> metricList = Arrays.asList("bugs", "coverage", "violations");
        String result = MetricKeys.getAllMetricKeysAsString(metricList);
        Assert.assertEquals(result, "bugs,coverage,violations");
    }

    @Test
    public void testGetAllMetricKeysAsStringSingle() {
        List<String> metricList = Arrays.asList("bugs");
        String result = MetricKeys.getAllMetricKeysAsString(metricList);
        Assert.assertEquals(result, "bugs");
    }

    @Test
    public void testGetAllMetricKeysAsStringEmpty() {
        List<String> metricList = Arrays.asList();
        String result = MetricKeys.getAllMetricKeysAsString(metricList);
        Assert.assertEquals(result, "");
    }

    @Test
    public void testConstantsNotNull() {
        Assert.assertNotNull(MetricKeys.BUGS);
        Assert.assertNotNull(MetricKeys.COVERAGE);
        Assert.assertNotNull(MetricKeys.VIOLATIONS);
        Assert.assertNotNull(MetricKeys.NCLOC);
        Assert.assertNotNull(MetricKeys.COMPLEXITY);
    }
}
