package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.QualityProfile;

@Test(groups = { "metrics" })
public class QualityProfileTest {

    @Test
    public void testConstructorAndGetters() {
        QualityProfile qp = new QualityProfile("key1", "Sonar way", "java", "Java", false, true, 100L, "2024-01-01", 5L);
        Assert.assertEquals(qp.getKey(), "key1");
        Assert.assertEquals(qp.getName(), "Sonar way");
        Assert.assertEquals(qp.getLanguage(), "java");
        Assert.assertEquals(qp.getLanguageName(), "Java");
        Assert.assertFalse(qp.getIsInherited());
        Assert.assertTrue(qp.getIsDefault());
        Assert.assertEquals(qp.getActiveRuleCount(), Long.valueOf(100L));
        Assert.assertEquals(qp.getRulesUpdatedAt(), "2024-01-01");
        Assert.assertEquals(qp.getProjectCount(), Long.valueOf(5L));
    }

    @Test
    public void testSetters() {
        QualityProfile qp = new QualityProfile("k", "n", "l", "ln", false, false, 0L, "", 0L);
        qp.setKey("key2");
        qp.setName("Updated Name");
        qp.setLanguage("python");
        qp.setLanguageName("Python");
        qp.setIsInherited(true);
        qp.setIsDefault(false);
        qp.setActiveRuleCount(50L);
        qp.setRulesUpdatedAt("2024-06-01");
        qp.setProjectCount(10L);

        Assert.assertEquals(qp.getKey(), "key2");
        Assert.assertEquals(qp.getName(), "Updated Name");
        Assert.assertEquals(qp.getLanguage(), "python");
        Assert.assertEquals(qp.getLanguageName(), "Python");
        Assert.assertTrue(qp.getIsInherited());
        Assert.assertFalse(qp.getIsDefault());
        Assert.assertEquals(qp.getActiveRuleCount(), Long.valueOf(50L));
        Assert.assertEquals(qp.getRulesUpdatedAt(), "2024-06-01");
        Assert.assertEquals(qp.getProjectCount(), Long.valueOf(10L));
    }

    @Test
    public void testToString() {
        QualityProfile qp = new QualityProfile("key1", "Sonar way", "java", "Java", false, true, 100L, "2024-01-01", 5L);
        Assert.assertNotNull(qp.toString());
        Assert.assertTrue(qp.toString().contains("Sonar way"));
    }
}
