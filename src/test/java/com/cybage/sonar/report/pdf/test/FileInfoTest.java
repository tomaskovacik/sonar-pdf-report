package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.FileInfo;

@Test(groups = { "metrics" })
public class FileInfoTest {

    private FileInfo fileInfo;

    @BeforeMethod
    public void setUp() {
        fileInfo = new FileInfo();
        fileInfo.setKey("com.example:MyFile.java");
        fileInfo.setName("MyFile.java");
        fileInfo.setPath("src/main/java/com/example/MyFile.java");
        fileInfo.setViolations("3");
        fileInfo.setComplexity("10");
        fileInfo.setDuplicatedLines("5");
    }

    @Test
    public void testGetters() {
        Assert.assertEquals(fileInfo.getKey(), "com.example:MyFile.java");
        Assert.assertEquals(fileInfo.getName(), "MyFile.java");
        Assert.assertEquals(fileInfo.getPath(), "src/main/java/com/example/MyFile.java");
        Assert.assertEquals(fileInfo.getViolations(), "3");
        Assert.assertEquals(fileInfo.getComplexity(), "10");
        Assert.assertEquals(fileInfo.getDuplicatedLines(), "5");
    }

    @Test
    public void testIsContentSetViolationsTrue() {
        Assert.assertTrue(fileInfo.isContentSet(FileInfo.VIOLATIONS_CONTENT));
    }

    @Test
    public void testIsContentSetViolationsFalse() {
        fileInfo.setViolations("0");
        Assert.assertFalse(fileInfo.isContentSet(FileInfo.VIOLATIONS_CONTENT));
    }

    @Test
    public void testIsContentSetCcnTrue() {
        Assert.assertTrue(fileInfo.isContentSet(FileInfo.CCN_CONTENT));
    }

    @Test
    public void testIsContentSetCcnFalse() {
        fileInfo.setComplexity("0");
        Assert.assertFalse(fileInfo.isContentSet(FileInfo.CCN_CONTENT));
    }

    @Test
    public void testIsContentSetDuplicationsTrue() {
        Assert.assertTrue(fileInfo.isContentSet(FileInfo.DUPLICATIONS_CONTENT));
    }

    @Test
    public void testIsContentSetDuplicationsFalse() {
        fileInfo.setDuplicatedLines("0");
        Assert.assertFalse(fileInfo.isContentSet(FileInfo.DUPLICATIONS_CONTENT));
    }

    @Test
    public void testIsContentSetUnknownType() {
        Assert.assertFalse(fileInfo.isContentSet(99));
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(fileInfo.toString());
    }
}
