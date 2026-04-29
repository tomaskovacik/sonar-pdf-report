package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.QualityProfileEntityBuilder;
import com.cybage.sonar.report.pdf.entity.QualityProfile;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"metrics"})
public class QualityProfileEntityBuilderTest {

    @Test
    public void testBuilderSetsAllFields() {
        QualityProfile qp = new QualityProfileEntityBuilder()
                .setKey("AYq")
                .setName("Sonar way")
                .setLanguage("java")
                .setLanguageName("Java")
                .setIsInherited(false)
                .setIsDefault(true)
                .setActiveRuleCount(100L)
                .setRulesUpdatedAt("2024-01-01")
                .setProjectCount(5L)
                .createQualityProfile();

        Assert.assertEquals(qp.getKey(), "AYq");
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
    public void testBuilderSettersReturnSameInstance() {
        QualityProfileEntityBuilder b = new QualityProfileEntityBuilder();
        Assert.assertSame(b.setKey("k"), b);
        Assert.assertSame(b.setName("n"), b);
        Assert.assertSame(b.setLanguage("java"), b);
        Assert.assertSame(b.setLanguageName("Java"), b);
        Assert.assertSame(b.setIsInherited(false), b);
        Assert.assertSame(b.setIsDefault(true), b);
        Assert.assertSame(b.setActiveRuleCount(1L), b);
        Assert.assertSame(b.setRulesUpdatedAt("2024"), b);
        Assert.assertSame(b.setProjectCount(1L), b);
    }

    @Test
    public void testBuilderWithNullsDoesNotThrow() {
        QualityProfile qp = new QualityProfileEntityBuilder().createQualityProfile();
        Assert.assertNotNull(qp);
        Assert.assertNull(qp.getKey());
        Assert.assertNull(qp.getName());
    }
}
