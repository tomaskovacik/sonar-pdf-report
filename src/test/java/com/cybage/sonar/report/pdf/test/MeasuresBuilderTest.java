package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.MeasuresBuilder;
import com.cybage.sonar.report.pdf.entity.Measures;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.measures.MeasuresService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class MeasuresBuilderTest {

    private WsClient        mockWsClient;
    private MeasuresService mockMeasuresService;

    @BeforeMethod
    public void setUp() {
        mockWsClient        = mock(WsClient.class);
        mockMeasuresService = mock(MeasuresService.class);
        when(mockWsClient.measures()).thenReturn(mockMeasuresService);
    }

    private org.sonarqube.ws.Measures.ComponentWsResponse emptyComponentResponse() {
        return org.sonarqube.ws.Measures.ComponentWsResponse.newBuilder()
                .setComponent(org.sonarqube.ws.Measures.Component.newBuilder()
                        .setKey("my:project").build())
                .build();
    }

    @Test
    public void testGetInstanceReturnsNewBuilder() {
        Assert.assertNotNull(MeasuresBuilder.getInstance(mockWsClient));
    }

    @Test
    public void testInitMeasuresReturnsEmptyWhenNoMeasures() throws Exception {
        when(mockMeasuresService.component(any())).thenReturn(emptyComponentResponse());

        Measures measures = MeasuresBuilder.getInstance(mockWsClient)
                .initMeasuresByProjectKey("my:project", null, null);

        Assert.assertNotNull(measures);
        Assert.assertEquals(measures.getMeasuresCount(), 0);
    }

    @Test
    public void testInitMeasuresWithOneMeasure() throws Exception {
        org.sonarqube.ws.Measures.ComponentWsResponse response =
                org.sonarqube.ws.Measures.ComponentWsResponse.newBuilder()
                        .setComponent(org.sonarqube.ws.Measures.Component.newBuilder()
                                .setKey("my:project")
                                .addMeasures(org.sonarqube.ws.Measures.Measure.newBuilder()
                                        .setMetric("bugs").setValue("3").build())
                                .build())
                        .setMetrics(org.sonarqube.ws.Measures.Metrics.newBuilder()
                                .addMetrics(Common.Metric.newBuilder()
                                        .setKey("bugs").setName("Bugs")
                                        .setType("INT").setDomain("Reliability")
                                        .build())
                                .build())
                        .build();
        when(mockMeasuresService.component(any())).thenReturn(response);

        Measures measures = MeasuresBuilder.getInstance(mockWsClient)
                .initMeasuresByProjectKey("my:project", null, null);

        Assert.assertTrue(measures.containsMeasure("bugs"));
        Assert.assertEquals(measures.getMeasure("bugs").getValue(), "3");
    }

    @Test
    public void testInitMeasuresWithPeriod() throws Exception {
        // Must include at least one measure so addAllMeasuresFromDocument (which stores periods) is called
        org.sonarqube.ws.Measures.ComponentWsResponse response =
                org.sonarqube.ws.Measures.ComponentWsResponse.newBuilder()
                        .setComponent(org.sonarqube.ws.Measures.Component.newBuilder()
                                .setKey("my:project")
                                .addMeasures(org.sonarqube.ws.Measures.Measure.newBuilder()
                                        .setMetric("bugs").setValue("1").build())
                                .build())
                        .setMetrics(org.sonarqube.ws.Measures.Metrics.newBuilder()
                                .addMetrics(Common.Metric.newBuilder()
                                        .setKey("bugs").setName("Bugs")
                                        .setType("INT").setDomain("Reliability")
                                        .build())
                                .build())
                        .setPeriod(org.sonarqube.ws.Measures.Period.newBuilder()
                                .setIndex(1).setMode("previous_version")
                                .setDate("2024-01-01").setParameter("1.0")
                                .build())
                        .build();
        when(mockMeasuresService.component(any())).thenReturn(response);

        Measures measures = MeasuresBuilder.getInstance(mockWsClient)
                .initMeasuresByProjectKey("my:project", null, null);

        Assert.assertNotNull(measures.getPeriods());
        Assert.assertFalse(measures.getPeriods().isEmpty());
        Assert.assertEquals(measures.getPeriods().get(0).getMode(), "previous_version");
        Assert.assertEquals(measures.getPeriods().get(0).getDate(), "2024-01-01");
    }

    @Test
    public void testInitMeasuresNoPeriodProducesEmptyList() throws Exception {
        // Include a measure so addAllMeasuresFromDocument is called; it will set periods to empty list
        org.sonarqube.ws.Measures.ComponentWsResponse response =
                org.sonarqube.ws.Measures.ComponentWsResponse.newBuilder()
                        .setComponent(org.sonarqube.ws.Measures.Component.newBuilder()
                                .setKey("my:project")
                                .addMeasures(org.sonarqube.ws.Measures.Measure.newBuilder()
                                        .setMetric("bugs").setValue("0").build())
                                .build())
                        .setMetrics(org.sonarqube.ws.Measures.Metrics.newBuilder()
                                .addMetrics(Common.Metric.newBuilder()
                                        .setKey("bugs").setName("Bugs")
                                        .setType("INT").setDomain("Reliability")
                                        .build())
                                .build())
                        .build();
        when(mockMeasuresService.component(any())).thenReturn(response);

        Measures measures = MeasuresBuilder.getInstance(mockWsClient)
                .initMeasuresByProjectKey("my:project", null, null);

        Assert.assertNotNull(measures.getPeriods());
        Assert.assertTrue(measures.getPeriods().isEmpty());
    }

    @Test
    public void testInitMeasuresWithBranch() throws Exception {
        when(mockMeasuresService.component(any())).thenReturn(emptyComponentResponse());

        Measures measures = MeasuresBuilder.getInstance(mockWsClient)
                .initMeasuresByProjectKey("my:project", null, "feature/branch");

        Assert.assertNotNull(measures);
    }
}
