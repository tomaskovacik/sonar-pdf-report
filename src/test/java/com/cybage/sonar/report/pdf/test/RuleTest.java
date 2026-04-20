package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Rule;

@Test(groups = { "metrics" })
public class RuleTest {

    @Test
    public void testDefaultConstructor() {
        Rule rule = new Rule();
        Assert.assertEquals(rule.getKey(), "");
        Assert.assertEquals(rule.getName(), "");
        Assert.assertEquals(rule.getCount(), Long.valueOf(0L));
        Assert.assertEquals(rule.getLanguageName(), "");
        Assert.assertEquals(rule.getSeverity(), "");
    }

    @Test
    public void testFullConstructor() {
        Rule rule = new Rule("squid:S1234", "Null Pointer", 5L, "Java", "CRITICAL");
        Assert.assertEquals(rule.getKey(), "squid:S1234");
        Assert.assertEquals(rule.getName(), "Null Pointer");
        Assert.assertEquals(rule.getCount(), Long.valueOf(5L));
        Assert.assertEquals(rule.getLanguageName(), "Java");
        Assert.assertEquals(rule.getSeverity(), "CRITICAL");
    }

    @Test
    public void testSetters() {
        Rule rule = new Rule();
        rule.setKey("squid:S5678");
        rule.setName("Resource Leak");
        rule.setCount(10L);
        rule.setLanguageName("Python");
        rule.setSeverity("MAJOR");

        Assert.assertEquals(rule.getKey(), "squid:S5678");
        Assert.assertEquals(rule.getName(), "Resource Leak");
        Assert.assertEquals(rule.getCount(), Long.valueOf(10L));
        Assert.assertEquals(rule.getLanguageName(), "Python");
        Assert.assertEquals(rule.getSeverity(), "MAJOR");
    }

    @Test
    public void testToString() {
        Rule rule = new Rule("squid:S1234", "Null Pointer", 5L, "Java", "CRITICAL");
        Assert.assertNotNull(rule.toString());
        Assert.assertTrue(rule.toString().contains("squid:S1234"));
    }
}
