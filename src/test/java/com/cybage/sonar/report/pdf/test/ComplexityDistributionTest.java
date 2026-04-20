package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.ComplexityDistribution;

@Test(groups = { "metrics" })
public class ComplexityDistributionTest {

    @Test
    public void testConstructionSingleEntry() {
        ComplexityDistribution cd = new ComplexityDistribution("5=10");
        Assert.assertEquals(cd.getxValues().length, 1);
        Assert.assertEquals(cd.getyValues().length, 1);
        Assert.assertEquals(cd.getxValues()[0], "5");
        Assert.assertEquals(cd.getyValues()[0], "10");
    }

    @Test
    public void testConstructionMultipleEntries() {
        ComplexityDistribution cd = new ComplexityDistribution("1=5;5=3;10=1");
        Assert.assertEquals(cd.getxValues().length, 3);
        Assert.assertEquals(cd.getyValues().length, 3);
        Assert.assertEquals(cd.getxValues()[0], "1");
        Assert.assertEquals(cd.getxValues()[1], "5");
        Assert.assertEquals(cd.getxValues()[2], "10");
        Assert.assertEquals(cd.getyValues()[0], "5");
        Assert.assertEquals(cd.getyValues()[1], "3");
        Assert.assertEquals(cd.getyValues()[2], "1");
    }

    @Test
    public void testFormatYValuesSingle() {
        ComplexityDistribution cd = new ComplexityDistribution("5=10");
        Assert.assertEquals(cd.formatYValues(), "10");
    }

    @Test
    public void testFormatYValuesMultiple() {
        ComplexityDistribution cd = new ComplexityDistribution("1=5;5=3;10=1");
        Assert.assertEquals(cd.formatYValues(), "5,3,1");
    }

    @Test
    public void testFormatXValuesSingle() {
        ComplexityDistribution cd = new ComplexityDistribution("5=10");
        Assert.assertEquals(cd.formatXValues(), "5%2b");
    }

    @Test
    public void testFormatXValuesMultiple() {
        ComplexityDistribution cd = new ComplexityDistribution("1=5;5=3;10=1");
        Assert.assertEquals(cd.formatXValues(), "1%2b,5%2b,10%2b");
    }

    @Test
    public void testSetters() {
        ComplexityDistribution cd = new ComplexityDistribution("1=5");
        cd.setxValues(new String[]{"a", "b"});
        cd.setyValues(new String[]{"10", "20"});
        Assert.assertEquals(cd.getxValues()[0], "a");
        Assert.assertEquals(cd.getxValues()[1], "b");
        Assert.assertEquals(cd.getyValues()[0], "10");
        Assert.assertEquals(cd.getyValues()[1], "20");
    }
}
