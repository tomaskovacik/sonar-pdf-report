package com.cybage.sonar.report.pdf.test;

import java.util.Arrays;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.entity.Measure;
import com.cybage.sonar.report.pdf.entity.Measures;
import com.cybage.sonar.report.pdf.entity.Project;
import com.cybage.sonar.report.pdf.entity.QualityProfile;

@Test(groups = { "report" })
public class ProjectTest {

    private Project project;

    @BeforeMethod
    public void setUp() {
        project = new Project("com.example:my-project");
    }

    @Test
    public void testConstructorWithKey() {
        Assert.assertEquals(project.getKey(), "com.example:my-project");
    }

    @Test
    public void testConstructorWithKeyVersionLanguages() {
        Project p = new Project("key", "1.0", Arrays.asList("java", "xml"));
        Assert.assertEquals(p.getKey(), "key");
        Assert.assertEquals(p.getVersion(), "1.0");
        Assert.assertEquals(p.getLanguages().size(), 2);
    }

    @Test
    public void testGetMeasureWhenPresent() {
        Measures measures = new Measures();
        Measure m = new Measure();
        m.setValue("42");
        measures.addMeasure("bugs", m);
        project.setMeasures(measures);

        Measure result = project.getMeasure("bugs");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getValue(), "42");
    }

    @Test
    public void testGetMeasureWhenAbsentReturnsEmptyMeasure() {
        Measures measures = new Measures();
        project.setMeasures(measures);

        Measure result = project.getMeasure("nonexistent");
        Assert.assertNotNull(result);
        Assert.assertNull(result.getValue());
    }

    @Test
    public void testGetMeasureWhenMeasuresNull() {
        Measure result = project.getMeasure("bugs");
        Assert.assertNotNull(result);
        Assert.assertNull(result.getValue());
    }

    @Test
    public void testGetChildByKey() {
        Project child1 = new Project("child:one");
        Project child2 = new Project("child:two");
        project.setSubprojects(Arrays.asList(child1, child2));

        Project found = project.getChildByKey("child:two");
        Assert.assertNotNull(found);
        Assert.assertEquals(found.getKey(), "child:two");
    }

    @Test
    public void testGetChildByKeyNotFound() {
        project.setSubprojects(Arrays.asList(new Project("child:one")));
        Project found = project.getChildByKey("child:missing");
        Assert.assertNull(found);
    }

    @Test
    public void testGetQualityProfileByLanguage() {
        QualityProfile qp = new QualityProfile("key1", "Sonar way", "java", "Java", false, true, 100L, "2024-01-01", 5L);
        project.setQualityProfiles(Arrays.asList(qp));

        Optional<QualityProfile> found = project.getQualityProfileByLanguage("java");
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get().getName(), "Sonar way");
    }

    @Test
    public void testGetQualityProfileByLanguageNotFound() {
        QualityProfile qp = new QualityProfile("key1", "Sonar way", "java", "Java", false, true, 100L, "2024-01-01", 5L);
        project.setQualityProfiles(Arrays.asList(qp));

        Optional<QualityProfile> found = project.getQualityProfileByLanguage("python");
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testSettersAndGetters() {
        project.setName("My Project");
        project.setVersion("2.0");
        project.setDescription("A test project");
        project.setId((short) 1);
        project.setLanguages(Arrays.asList("java"));
        project.setLinks(Arrays.asList("http://example.com"));

        Assert.assertEquals(project.getName(), "My Project");
        Assert.assertEquals(project.getVersion(), "2.0");
        Assert.assertEquals(project.getDescription(), "A test project");
        Assert.assertEquals(project.getId(), (short) 1);
        Assert.assertEquals(project.getLanguages().size(), 1);
        Assert.assertEquals(project.getLinks().size(), 1);
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(project.toString());
    }
}
