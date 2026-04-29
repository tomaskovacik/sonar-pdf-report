package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.ProjectStatusBuilder;
import com.cybage.sonar.report.pdf.entity.ProjectStatus;
import org.sonarqube.ws.Qualitygates;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.qualitygates.QualitygatesService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class ProjectStatusBuilderTest {

    private WsClient            mockWsClient;
    private QualitygatesService mockQualitygatesService;

    @BeforeMethod
    public void setUp() throws Exception {
        mockWsClient            = mock(WsClient.class);
        mockQualitygatesService = mock(QualitygatesService.class);
        when(mockWsClient.qualitygates()).thenReturn(mockQualitygatesService);

        Field f = ProjectStatusBuilder.class.getDeclaredField("builder");
        f.setAccessible(true);
        f.set(null, null);
    }

    private Qualitygates.ProjectStatusResponse okResponse() {
        return Qualitygates.ProjectStatusResponse.newBuilder()
                .setProjectStatus(Qualitygates.ProjectStatusResponse.ProjectStatus.newBuilder()
                        .setStatus(Qualitygates.ProjectStatusResponse.Status.OK)
                        .build())
                .build();
    }

    @Test
    public void testGetInstanceReturnsNewBuilder() {
        ProjectStatusBuilder b = ProjectStatusBuilder.getInstance(mockWsClient);
        Assert.assertNotNull(b);
    }

    @Test
    public void testInitProjectStatusOkStatus() {
        when(mockQualitygatesService.projectStatus(any())).thenReturn(okResponse());

        ProjectStatus status = ProjectStatusBuilder.getInstance(mockWsClient)
                .initProjectStatusByProjectKey("my:project", null);

        Assert.assertNotNull(status);
        Assert.assertEquals(status.getStatus(), "OK");
    }

    @Test
    public void testInitProjectStatusErrorStatus() {
        Qualitygates.ProjectStatusResponse response = Qualitygates.ProjectStatusResponse.newBuilder()
                .setProjectStatus(Qualitygates.ProjectStatusResponse.ProjectStatus.newBuilder()
                        .setStatus(Qualitygates.ProjectStatusResponse.Status.ERROR)
                        .build())
                .build();
        when(mockQualitygatesService.projectStatus(any())).thenReturn(response);

        ProjectStatus status = ProjectStatusBuilder.getInstance(mockWsClient)
                .initProjectStatusByProjectKey("my:project", null);

        Assert.assertEquals(status.getStatus(), "ERROR");
    }

    @Test
    public void testInitProjectStatusWithBranch() {
        when(mockQualitygatesService.projectStatus(any())).thenReturn(okResponse());

        ProjectStatus status = ProjectStatusBuilder.getInstance(mockWsClient)
                .initProjectStatusByProjectKey("my:project", "feature/branch");

        Assert.assertNotNull(status);
    }

    @Test
    public void testInitProjectStatusWithConditions() {
        Qualitygates.ProjectStatusResponse response = Qualitygates.ProjectStatusResponse.newBuilder()
                .setProjectStatus(Qualitygates.ProjectStatusResponse.ProjectStatus.newBuilder()
                        .setStatus(Qualitygates.ProjectStatusResponse.Status.ERROR)
                        .addConditions(Qualitygates.ProjectStatusResponse.Condition.newBuilder()
                                .setStatus(Qualitygates.ProjectStatusResponse.Status.ERROR)
                                .setMetricKey("coverage")
                                .setComparator(Qualitygates.ProjectStatusResponse.Comparator.LT)
                                .setErrorThreshold("80")
                                .setActualValue("70")
                                .build())
                        .build())
                .build();
        when(mockQualitygatesService.projectStatus(any())).thenReturn(response);

        ProjectStatus status = ProjectStatusBuilder.getInstance(mockWsClient)
                .initProjectStatusByProjectKey("my:project", null);

        Assert.assertNotNull(status.getConditions());
        Assert.assertEquals(status.getConditions().size(), 1);
        Assert.assertEquals(status.getConditions().get(0).getMetricKey(), "coverage");
    }

    @Test
    public void testInitProjectStatusWithPeriod() {
        Qualitygates.ProjectStatusResponse response = Qualitygates.ProjectStatusResponse.newBuilder()
                .setProjectStatus(Qualitygates.ProjectStatusResponse.ProjectStatus.newBuilder()
                        .setStatus(Qualitygates.ProjectStatusResponse.Status.OK)
                        .setPeriod(Qualitygates.ProjectStatusResponse.NewCodePeriod.newBuilder()
                                .setMode("previous_version")
                                .setDate("2024-01-01")
                                .setParameter("1.0")
                                .build())
                        .build())
                .build();
        when(mockQualitygatesService.projectStatus(any())).thenReturn(response);

        ProjectStatus status = ProjectStatusBuilder.getInstance(mockWsClient)
                .initProjectStatusByProjectKey("my:project", null);

        Assert.assertNotNull(status.getStatusPeriods());
        Assert.assertEquals(status.getStatusPeriods().size(), 1);
        Assert.assertEquals(status.getStatusPeriods().get(0).getMode(), "previous_version");
    }

    @Test
    public void testInitProjectStatusNoPeriod() {
        when(mockQualitygatesService.projectStatus(any())).thenReturn(okResponse());

        ProjectStatus status = ProjectStatusBuilder.getInstance(mockWsClient)
                .initProjectStatusByProjectKey("my:project", null);

        Assert.assertTrue(status.getStatusPeriods().isEmpty());
    }
}
