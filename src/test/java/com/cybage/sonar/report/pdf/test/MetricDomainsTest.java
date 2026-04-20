package com.cybage.sonar.report.pdf.test;

import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.util.MetricDomains;
import com.cybage.sonar.report.pdf.util.MetricKeys;

@Test(groups = { "metrics" })
public class MetricDomainsTest {

    @Test
    public void testGetDomainsNotEmpty() {
        Set<String> domains = MetricDomains.getDomains();
        Assert.assertNotNull(domains);
        Assert.assertTrue(domains.size() > 0);
    }

    @Test
    public void testGetDomainsContainsReliability() {
        Assert.assertTrue(MetricDomains.getDomains().contains(MetricDomains.RELIABILITY));
    }

    @Test
    public void testGetDomainsContainsSecurity() {
        Assert.assertTrue(MetricDomains.getDomains().contains(MetricDomains.SECURITY));
    }

    @Test
    public void testGetDomainsContainsMaintainability() {
        Assert.assertTrue(MetricDomains.getDomains().contains(MetricDomains.MAINTAINABILITY));
    }

    @Test
    public void testGetMetricKeysForReliability() {
        List<String> keys = MetricDomains.getMetricKeys(MetricDomains.RELIABILITY);
        Assert.assertNotNull(keys);
        Assert.assertTrue(keys.contains(MetricKeys.BUGS));
        Assert.assertTrue(keys.contains(MetricKeys.RELIABILITY_RATING));
    }

    @Test
    public void testGetMetricKeysForSecurity() {
        List<String> keys = MetricDomains.getMetricKeys(MetricDomains.SECURITY);
        Assert.assertNotNull(keys);
        Assert.assertTrue(keys.contains(MetricKeys.VULNERABILITIES));
        Assert.assertTrue(keys.contains(MetricKeys.SECURITY_RATING));
    }

    @Test
    public void testGetMetricKeysForSize() {
        List<String> keys = MetricDomains.getMetricKeys(MetricDomains.SIZE);
        Assert.assertNotNull(keys);
        Assert.assertTrue(keys.contains(MetricKeys.NCLOC));
        Assert.assertTrue(keys.contains(MetricKeys.LINES));
    }

    @Test
    public void testGetMetricKeysForUnknownDomainReturnsNull() {
        List<String> keys = MetricDomains.getMetricKeys("Unknown Domain");
        Assert.assertNull(keys);
    }

    @Test
    public void testDomainConstants() {
        Assert.assertEquals(MetricDomains.RELIABILITY, "Reliability");
        Assert.assertEquals(MetricDomains.SECURITY, "Security");
        Assert.assertEquals(MetricDomains.MAINTAINABILITY, "Maintainability");
        Assert.assertEquals(MetricDomains.COVERAGE, "Coverage");
        Assert.assertEquals(MetricDomains.DUPLICATIONS, "Duplications");
        Assert.assertEquals(MetricDomains.SIZE, "Size");
        Assert.assertEquals(MetricDomains.COMPLEXITY, "Complexity");
        Assert.assertEquals(MetricDomains.DOCUMENTATION, "Documentation");
        Assert.assertEquals(MetricDomains.ISSUES, "Issues");
    }
}
