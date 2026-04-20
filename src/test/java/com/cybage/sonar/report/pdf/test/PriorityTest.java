package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Priority;

@Test(groups = { "metrics" })
public class PriorityTest {

    @Test
    public void testGetPriorityAll() {
        Assert.assertEquals(Priority.getPriority(Priority.ALL), "ALL");
    }

    @Test
    public void testGetPriorityInfo() {
        Assert.assertEquals(Priority.getPriority(Priority.INFO), Priority.INFO);
    }

    @Test
    public void testGetPriorityMinor() {
        Assert.assertEquals(Priority.getPriority(Priority.MINOR), Priority.MINOR);
    }

    @Test
    public void testGetPriorityMajor() {
        Assert.assertEquals(Priority.getPriority(Priority.MAJOR), Priority.MAJOR);
    }

    @Test
    public void testGetPriorityCritical() {
        Assert.assertEquals(Priority.getPriority(Priority.CRITICAL), Priority.CRITICAL);
    }

    @Test
    public void testGetPriorityBlocker() {
        Assert.assertEquals(Priority.getPriority(Priority.BLOCKER), Priority.BLOCKER);
    }

    @Test
    public void testGetPrioritiesArrayLength() {
        String[] priorities = Priority.getPrioritiesArray();
        Assert.assertEquals(priorities.length, 6);
    }

    @Test
    public void testGetPrioritiesArrayContainsAll() {
        String[] priorities = Priority.getPrioritiesArray();
        Assert.assertEquals(priorities[0], Priority.ALL);
        Assert.assertEquals(priorities[1], Priority.INFO);
        Assert.assertEquals(priorities[2], Priority.MINOR);
        Assert.assertEquals(priorities[3], Priority.MAJOR);
        Assert.assertEquals(priorities[4], Priority.CRITICAL);
        Assert.assertEquals(priorities[5], Priority.BLOCKER);
    }
}
