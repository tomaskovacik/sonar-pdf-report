package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.QualityProfileBuilder;
import com.cybage.sonar.report.pdf.entity.QualityProfile;
import org.sonarqube.ws.Qualityprofiles;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.qualityprofiles.QualityprofilesService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class QualityProfileBuilderTest {

    private WsClient               mockWsClient;
    private QualityprofilesService mockQpService;

    @BeforeMethod
    public void setUp() throws Exception {
        mockWsClient = mock(WsClient.class);
        mockQpService = mock(QualityprofilesService.class);
        when(mockWsClient.qualityprofiles()).thenReturn(mockQpService);

        Field f = QualityProfileBuilder.class.getDeclaredField("builder");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    public void testGetInstanceReturnsNewBuilder() {
        Assert.assertNotNull(QualityProfileBuilder.getInstance(mockWsClient));
    }

    @Test
    public void testInitReturnsEmptyListWhenNoProfiles() {
        when(mockQpService.search(any())).thenReturn(
                Qualityprofiles.SearchWsResponse.newBuilder().build());

        List<QualityProfile> profiles = QualityProfileBuilder.getInstance(mockWsClient)
                .initProjectQualityProfilesByProjectKey("my:project");

        Assert.assertNotNull(profiles);
        Assert.assertTrue(profiles.isEmpty());
    }

    @Test
    public void testInitReturnsMappedProfile() {
        Qualityprofiles.SearchWsResponse response = Qualityprofiles.SearchWsResponse.newBuilder()
                .addProfiles(Qualityprofiles.SearchWsResponse.QualityProfile.newBuilder()
                        .setKey("AYq")
                        .setName("Sonar way")
                        .setLanguage("java")
                        .setLanguageName("Java")
                        .setIsInherited(false)
                        .setIsDefault(true)
                        .setActiveRuleCount(100L)
                        .setRulesUpdatedAt("2024-01-01")
                        .setProjectCount(5L)
                        .build())
                .build();
        when(mockQpService.search(any())).thenReturn(response);

        List<QualityProfile> profiles = QualityProfileBuilder.getInstance(mockWsClient)
                .initProjectQualityProfilesByProjectKey("my:project");

        Assert.assertEquals(profiles.size(), 1);
        QualityProfile qp = profiles.get(0);
        Assert.assertEquals(qp.getKey(), "AYq");
        Assert.assertEquals(qp.getName(), "Sonar way");
        Assert.assertEquals(qp.getLanguage(), "java");
        Assert.assertEquals(qp.getLanguageName(), "Java");
        Assert.assertTrue(qp.getIsDefault());
        Assert.assertFalse(qp.getIsInherited());
        Assert.assertEquals(qp.getActiveRuleCount(), Long.valueOf(100L));
    }

    @Test
    public void testInitReturnsMultipleProfiles() {
        Qualityprofiles.SearchWsResponse response = Qualityprofiles.SearchWsResponse.newBuilder()
                .addProfiles(Qualityprofiles.SearchWsResponse.QualityProfile.newBuilder()
                        .setKey("k1").setName("Profile1").setLanguage("java").build())
                .addProfiles(Qualityprofiles.SearchWsResponse.QualityProfile.newBuilder()
                        .setKey("k2").setName("Profile2").setLanguage("xml").build())
                .build();
        when(mockQpService.search(any())).thenReturn(response);

        List<QualityProfile> profiles = QualityProfileBuilder.getInstance(mockWsClient)
                .initProjectQualityProfilesByProjectKey("my:project");

        Assert.assertEquals(profiles.size(), 2);
    }
}
