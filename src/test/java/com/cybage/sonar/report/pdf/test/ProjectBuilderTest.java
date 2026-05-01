package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.ProjectBuilder;
import com.cybage.sonar.report.pdf.entity.Project;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Components;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Qualitygates;
import org.sonarqube.ws.Qualityprofiles;
import org.sonarqube.ws.client.HttpException;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.components.ComponentsService;
import org.sonarqube.ws.client.issues.IssuesService;
import org.sonarqube.ws.client.measures.MeasuresService;
import org.sonarqube.ws.client.projects.ProjectsService;
import org.sonarqube.ws.client.qualitygates.QualitygatesService;
import org.sonarqube.ws.client.qualityprofiles.QualityprofilesService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class ProjectBuilderTest {

    private WsClient               mockWsClient;
    private ComponentsService      mockComponentsService;
    private QualitygatesService    mockQualitygatesService;
    private QualityprofilesService mockQualityprofilesService;
    private IssuesService          mockIssuesService;
    private MeasuresService        mockMeasuresService;
    private ProjectsService        mockProjectsService;

    @BeforeMethod
    public void setUp() {
        mockWsClient               = mock(WsClient.class);
        mockComponentsService      = mock(ComponentsService.class);
        mockQualitygatesService    = mock(QualitygatesService.class);
        mockQualityprofilesService = mock(QualityprofilesService.class);
        mockIssuesService          = mock(IssuesService.class);
        mockMeasuresService        = mock(MeasuresService.class);
        mockProjectsService        = mock(ProjectsService.class);

        when(mockWsClient.components()).thenReturn(mockComponentsService);
        when(mockWsClient.qualitygates()).thenReturn(mockQualitygatesService);
        when(mockWsClient.qualityprofiles()).thenReturn(mockQualityprofilesService);
        when(mockWsClient.issues()).thenReturn(mockIssuesService);
        when(mockWsClient.measures()).thenReturn(mockMeasuresService);
        when(mockWsClient.projects()).thenReturn(mockProjectsService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Components.ShowWsResponse showResponse(String name) {
        return Components.ShowWsResponse.newBuilder()
                .setComponent(Components.Component.newBuilder()
                        .setKey("my:project").setName(name).setDescription("desc").build())
                .build();
    }

    private Qualitygates.ProjectStatusResponse okStatusResponse() {
        return Qualitygates.ProjectStatusResponse.newBuilder()
                .setProjectStatus(Qualitygates.ProjectStatusResponse.ProjectStatus.newBuilder()
                        .setStatus(Qualitygates.ProjectStatusResponse.Status.OK)
                        .build())
                .build();
    }

    // RuleBuilder calls getFacets().getFacets(0), and FileInfoBuilder calls getValues(0)
    // unconditionally, so we need 1 facet with 1 dummy value. The empty components list
    // means no FileInfo objects are produced.
    private Issues.SearchWsResponse emptyIssuesResponse() {
        return Issues.SearchWsResponse.newBuilder()
                .setFacets(Common.Facets.newBuilder()
                        .addFacets(Common.Facet.newBuilder()
                                .setProperty("files")
                                .addValues(Common.FacetValue.newBuilder()
                                        .setVal("dummy/File.java").setCount(0).build())
                                .build())
                        .build())
                .build();
    }

    private org.sonarqube.ws.Measures.ComponentWsResponse emptyMeasuresComponentResponse() {
        return org.sonarqube.ws.Measures.ComponentWsResponse.newBuilder()
                .setComponent(org.sonarqube.ws.Measures.Component.newBuilder()
                        .setKey("my:project").build())
                .build();
    }

    private void setUpHappyPathMocks() throws Exception {
        when(mockComponentsService.show(any())).thenReturn(showResponse("My Project"));
        when(mockQualitygatesService.projectStatus(any())).thenReturn(okStatusResponse());
        when(mockQualityprofilesService.search(any())).thenReturn(
                Qualityprofiles.SearchWsResponse.newBuilder().build());
        when(mockIssuesService.search(any())).thenReturn(emptyIssuesResponse());
        when(mockMeasuresService.component(any())).thenReturn(emptyMeasuresComponentResponse());
        when(mockMeasuresService.componentTree(any())).thenReturn(
                org.sonarqube.ws.Measures.ComponentTreeWsResponse.newBuilder().build());
    }

    // -------------------------------------------------------------------------
    // getInstance
    // -------------------------------------------------------------------------

    @Test
    public void testGetInstanceReturnsNonNull() {
        Assert.assertNotNull(ProjectBuilder.getInstance(mockWsClient));
    }

    // -------------------------------------------------------------------------
    // initializeProject — happy path
    // -------------------------------------------------------------------------

    @Test
    public void testInitializeProjectSetsName() throws Exception {
        setUpHappyPathMocks();

        Project project = new ProjectBuilder(mockWsClient).initializeProject(
                "my:project", "1.0", Collections.singletonList("java"),
                Collections.emptySet(), Collections.emptySet(), null);

        Assert.assertEquals(project.getName(), "My Project");
        Assert.assertNotNull(project.getMeasures());
        Assert.assertNotNull(project.getProjectStatus());
    }

    @Test
    public void testInitializeProjectWithBranch() throws Exception {
        setUpHappyPathMocks();

        Project project = new ProjectBuilder(mockWsClient).initializeProject(
                "my:project", "1.0", Collections.singletonList("java"),
                Collections.emptySet(), Collections.emptySet(), "feature/my-branch");

        Assert.assertEquals(project.getName(), "My Project");
    }

    @Test
    public void testInitializeProjectWithTypesOfIssue() throws Exception {
        setUpHappyPathMocks();

        Project project = new ProjectBuilder(mockWsClient).initializeProject(
                "my:project", "1.0", Collections.singletonList("java"),
                Collections.emptySet(), Set.of("BUG"), null);

        Assert.assertNotNull(project);
        Assert.assertNotNull(project.getIssues());
        Assert.assertTrue(project.getIssues().isEmpty());
    }

    // -------------------------------------------------------------------------
    // fetchProjectComponent — null name path
    // -------------------------------------------------------------------------

    @Test
    public void testInitializeProjectNullNameReturnsProjectWithKey() throws Exception {
        // ShowWsResponse with no component → hasComponent() == false → name is null → ReportException caught
        when(mockComponentsService.show(any())).thenReturn(
                Components.ShowWsResponse.newBuilder().build());

        Project project = new ProjectBuilder(mockWsClient).initializeProject(
                "my:project", "1.0", Collections.singletonList("java"),
                Collections.emptySet(), Collections.emptySet(), null);

        Assert.assertNotNull(project);
        Assert.assertEquals(project.getKey(), "my:project");
    }

    // -------------------------------------------------------------------------
    // fetchProjectComponent — HTTP 403 fallback
    // -------------------------------------------------------------------------

    @Test
    public void testFetchProjectComponent403FallbackSuccess() throws Exception {
        when(mockComponentsService.show(any())).thenThrow(
                new HttpException("http://test", 403, "Forbidden"));
        when(mockProjectsService.search(any())).thenReturn(
                org.sonarqube.ws.Projects.SearchWsResponse.newBuilder()
                        .addComponents(org.sonarqube.ws.Projects.SearchWsResponse.Component.newBuilder()
                                .setKey("my:project").setName("Fallback Project").build())
                        .build());
        when(mockQualitygatesService.projectStatus(any())).thenReturn(okStatusResponse());
        when(mockQualityprofilesService.search(any())).thenReturn(
                Qualityprofiles.SearchWsResponse.newBuilder().build());
        when(mockIssuesService.search(any())).thenReturn(emptyIssuesResponse());
        when(mockMeasuresService.component(any())).thenReturn(emptyMeasuresComponentResponse());
        when(mockMeasuresService.componentTree(any())).thenReturn(
                org.sonarqube.ws.Measures.ComponentTreeWsResponse.newBuilder().build());

        Project project = new ProjectBuilder(mockWsClient).initializeProject(
                "my:project", "1.0", Collections.singletonList("java"),
                Collections.emptySet(), Collections.emptySet(), null);

        Assert.assertEquals(project.getName(), "Fallback Project");
    }

    @Test
    public void testFetchProjectComponent403FallbackNoResults() throws Exception {
        // search returns 0 components → null name → ReportException caught → project with key only
        when(mockComponentsService.show(any())).thenThrow(
                new HttpException("http://test", 403, "Forbidden"));
        when(mockProjectsService.search(any())).thenReturn(
                org.sonarqube.ws.Projects.SearchWsResponse.newBuilder().build());

        Project project = new ProjectBuilder(mockWsClient).initializeProject(
                "my:project", "1.0", Collections.singletonList("java"),
                Collections.emptySet(), Collections.emptySet(), null);

        Assert.assertNotNull(project);
        Assert.assertEquals(project.getKey(), "my:project");
    }

    @Test
    public void testFetchProjectComponentNon403HttpExceptionCaughtByOuterHandler() throws Exception {
        // Non-403 is rethrown from fetchProjectComponent, caught by outer catch in initializeProject
        when(mockComponentsService.show(any())).thenThrow(
                new HttpException("http://test", 401, "Unauthorized"));

        Project project = new ProjectBuilder(mockWsClient).initializeProject(
                "my:project", "1.0", Collections.singletonList("java"),
                Collections.emptySet(), Collections.emptySet(), null);

        Assert.assertNotNull(project);
        Assert.assertEquals(project.getKey(), "my:project");
    }
}
