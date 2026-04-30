package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.FileInfoBuilder;
import com.cybage.sonar.report.pdf.entity.FileInfo;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.issues.IssuesService;
import org.sonarqube.ws.client.measures.MeasuresService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class FileInfoBuilderTest {

    private WsClient        mockWsClient;
    private IssuesService   mockIssuesService;
    private MeasuresService mockMeasuresService;

    @BeforeMethod
    public void setUp() throws Exception {
        mockWsClient        = mock(WsClient.class);
        mockIssuesService   = mock(IssuesService.class);
        mockMeasuresService = mock(MeasuresService.class);
        when(mockWsClient.issues()).thenReturn(mockIssuesService);
        when(mockWsClient.measures()).thenReturn(mockMeasuresService);

        Field f = FileInfoBuilder.class.getDeclaredField("builder");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    public void testGetInstanceReturnsNewBuilder() {
        Assert.assertNotNull(FileInfoBuilder.getInstance(mockWsClient));
    }

    @Test
    public void testInitMostViolatedFilesReturnsMappedFile() {
        Issues.SearchWsResponse response = Issues.SearchWsResponse.newBuilder()
                .setFacets(Common.Facets.newBuilder()
                        .addFacets(Common.Facet.newBuilder()
                                .setProperty(FileInfoBuilder.FACET_FILES)
                                .addValues(Common.FacetValue.newBuilder()
                                        .setVal("src/Main.java")
                                        .setCount(7)
                                        .build())
                                .build())
                        .build())
                .addComponents(Issues.Component.newBuilder()
                        .setKey("my:project:src/Main.java")
                        .setPath("src/Main.java")
                        .setName("Main.java")
                        .setQualifier(FileInfoBuilder.S_QUALIFIER_FIL)
                        .build())
                .build();
        when(mockIssuesService.search(any())).thenReturn(response);

        List<FileInfo> files = FileInfoBuilder.getInstance(mockWsClient)
                .initProjectMostViolatedFilesByProjectKey("my:project", null);

        Assert.assertEquals(files.size(), 1);
        Assert.assertEquals(files.get(0).getName(), "Main.java");
        Assert.assertEquals(files.get(0).getPath(), "src/Main.java");
        Assert.assertEquals(files.get(0).getViolations(), "7");
    }

    @Test
    public void testInitMostViolatedFilesWithBranch() {
        Issues.SearchWsResponse response = Issues.SearchWsResponse.newBuilder()
                .setFacets(Common.Facets.newBuilder()
                        .addFacets(Common.Facet.newBuilder()
                                .setProperty(FileInfoBuilder.FACET_FILES)
                                .addValues(Common.FacetValue.newBuilder()
                                        .setVal("src/Foo.java").setCount(2)
                                        .build())
                                .build())
                        .build())
                .addComponents(Issues.Component.newBuilder()
                        .setKey("my:project:src/Foo.java")
                        .setPath("src/Foo.java")
                        .setName("Foo.java")
                        .setQualifier(FileInfoBuilder.S_QUALIFIER_FIL)
                        .build())
                .build();
        when(mockIssuesService.search(any())).thenReturn(response);

        List<FileInfo> files = FileInfoBuilder.getInstance(mockWsClient)
                .initProjectMostViolatedFilesByProjectKey("my:project", "feature/x");

        Assert.assertFalse(files.isEmpty());
        Assert.assertEquals(files.get(0).getName(), "Foo.java");
    }

    @Test
    public void testInitMostComplexFilesReturnsMappedFile() {
        Measures.ComponentTreeWsResponse response = Measures.ComponentTreeWsResponse.newBuilder()
                .addComponents(Measures.Component.newBuilder()
                        .setKey("my:project:src/Complex.java")
                        .setName("Complex.java")
                        .setPath("src/Complex.java")
                        .addMeasures(org.sonarqube.ws.Measures.Measure.newBuilder()
                                .setMetric("complexity").setValue("25").build())
                        .build())
                .build();
        when(mockMeasuresService.componentTree(any())).thenReturn(response);

        List<FileInfo> files = FileInfoBuilder.getInstance(mockWsClient)
                .initProjectMostComplexFilesByProjectKey("my:project", null);

        Assert.assertEquals(files.size(), 1);
        Assert.assertEquals(files.get(0).getName(), "Complex.java");
        Assert.assertEquals(files.get(0).getComplexity(), "25");
    }

    @Test
    public void testInitMostComplexFilesWithBranch() {
        Measures.ComponentTreeWsResponse response = Measures.ComponentTreeWsResponse.newBuilder()
                .addComponents(Measures.Component.newBuilder()
                        .setKey("my:project:src/C.java")
                        .setName("C.java").setPath("src/C.java")
                        .addMeasures(org.sonarqube.ws.Measures.Measure.newBuilder()
                                .setMetric("complexity").setValue("10").build())
                        .build())
                .build();
        when(mockMeasuresService.componentTree(any())).thenReturn(response);

        List<FileInfo> files = FileInfoBuilder.getInstance(mockWsClient)
                .initProjectMostComplexFilesByProjectKey("my:project", "main");

        Assert.assertFalse(files.isEmpty());
    }

    @Test
    public void testInitMostDuplicatedFilesReturnsMappedFile() {
        Measures.ComponentTreeWsResponse response = Measures.ComponentTreeWsResponse.newBuilder()
                .addComponents(Measures.Component.newBuilder()
                        .setKey("my:project:src/Dup.java")
                        .setName("Dup.java")
                        .setPath("src/Dup.java")
                        .addMeasures(org.sonarqube.ws.Measures.Measure.newBuilder()
                                .setMetric("duplicated_lines").setValue("42").build())
                        .build())
                .build();
        when(mockMeasuresService.componentTree(any())).thenReturn(response);

        List<FileInfo> files = FileInfoBuilder.getInstance(mockWsClient)
                .initProjectMostDuplicatedFilesByProjectKey("my:project", null);

        Assert.assertEquals(files.size(), 1);
        Assert.assertEquals(files.get(0).getName(), "Dup.java");
        Assert.assertEquals(files.get(0).getDuplicatedLines(), "42");
    }

    @Test
    public void testInitMostDuplicatedFilesEmptyResult() {
        when(mockMeasuresService.componentTree(any())).thenReturn(
                Measures.ComponentTreeWsResponse.newBuilder().build());

        List<FileInfo> files = FileInfoBuilder.getInstance(mockWsClient)
                .initProjectMostDuplicatedFilesByProjectKey("my:project", null);

        Assert.assertNotNull(files);
        Assert.assertTrue(files.isEmpty());
    }
}
