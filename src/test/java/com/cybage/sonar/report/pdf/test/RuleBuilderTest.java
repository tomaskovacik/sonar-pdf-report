package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.RuleBuilder;
import com.cybage.sonar.report.pdf.entity.Rule;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.issues.IssuesService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class RuleBuilderTest {

    private WsClient       mockWsClient;
    private IssuesService  mockIssuesService;

    @BeforeMethod
    public void setUp() throws Exception {
        mockWsClient       = mock(WsClient.class);
        mockIssuesService  = mock(IssuesService.class);
        when(mockWsClient.issues()).thenReturn(mockIssuesService);

        // Reset static field so getInstance() always starts fresh
        Field f = RuleBuilder.class.getDeclaredField("builder");
        f.setAccessible(true);
        f.set(null, null);
    }

    // ---- constants ----

    @Test
    public void testTopMaxViolatedRulesConstant() {
        Assert.assertEquals(RuleBuilder.TOP_MAX_VIOLATED_RULES, 10);
    }

    // ---- getInstance() ----

    @Test
    public void testGetInstanceReturnsNonNull() {
        RuleBuilder instance = RuleBuilder.getInstance(mockWsClient);
        Assert.assertNotNull(instance);
    }

    // ---- initProjectMostViolatedRulesByProjectKey() – no violations ----

    @Test
    public void testInitRulesReturnsEmptyListWhenFacetHasNoValues() {
        // SonarQube always returns a facet entry; it just has no values when there are no violations
        Common.Facet emptyFacet = Common.Facet.newBuilder()
                .setProperty("rules")
                .build(); // no values added

        Issues.SearchWsResponse emptyResponse = Issues.SearchWsResponse.newBuilder()
                .setFacets(Common.Facets.newBuilder().addFacets(emptyFacet).build())
                .setRules(Common.Rules.newBuilder().build())
                .build();
        when(mockIssuesService.search(any())).thenReturn(emptyResponse);

        RuleBuilder rb    = new RuleBuilder(mockWsClient);
        List<Rule>  rules = rb.initProjectMostViolatedRulesByProjectKey("test:project", null);

        Assert.assertNotNull(rules);
        Assert.assertTrue(rules.isEmpty(), "should return empty list when facet has no values");
    }

    // ---- initProjectMostViolatedRulesByProjectKey() – one rule per priority ----

    @Test
    public void testInitRulesReturnsMappedRule() {
        // Build a facet with one entry matching a rule key
        Common.FacetValue facetValue = Common.FacetValue.newBuilder()
                .setVal("java:S1234")
                .setCount(5)
                .build();

        Common.Facet facet = Common.Facet.newBuilder()
                .setProperty("rules")
                .addValues(facetValue)
                .build();

        Common.Rule protoRule = Common.Rule.newBuilder()
                .setKey("java:S1234")
                .setName("My Rule")
                .setLangName("Java")
                .build();

        Issues.SearchWsResponse response = Issues.SearchWsResponse.newBuilder()
                .setFacets(Common.Facets.newBuilder().addFacets(facet).build())
                .setRules(Common.Rules.newBuilder().addRules(protoRule).build())
                .build();

        when(mockIssuesService.search(any())).thenReturn(response);

        RuleBuilder rb    = new RuleBuilder(mockWsClient);
        List<Rule>  rules = rb.initProjectMostViolatedRulesByProjectKey("test:project", null);

        Assert.assertFalse(rules.isEmpty(), "should find at least one rule");
        Rule rule = rules.get(0);
        Assert.assertEquals(rule.getKey(), "java:S1234");
        Assert.assertEquals(rule.getName(), "My Rule");
        Assert.assertEquals(rule.getCount().longValue(), 5L);
        Assert.assertEquals(rule.getLanguageName(), "Java");
    }

    // ---- initProjectMostViolatedRulesByProjectKey() – rule key not in rules list ----

    @Test
    public void testInitRulesSkipsWhenRuleNotFoundInResponse() {
        // Facet references a key that has no matching rule in the rules list
        Common.FacetValue facetValue = Common.FacetValue.newBuilder()
                .setVal("java:S9999")
                .setCount(3)
                .build();

        Common.Facet facet = Common.Facet.newBuilder()
                .setProperty("rules")
                .addValues(facetValue)
                .build();

        // Rules list is empty — no match for java:S9999
        Issues.SearchWsResponse response = Issues.SearchWsResponse.newBuilder()
                .setFacets(Common.Facets.newBuilder().addFacets(facet).build())
                .setRules(Common.Rules.newBuilder().build())
                .build();

        when(mockIssuesService.search(any())).thenReturn(response);

        RuleBuilder rb    = new RuleBuilder(mockWsClient);
        List<Rule>  rules = rb.initProjectMostViolatedRulesByProjectKey("test:project", null);

        Assert.assertTrue(rules.isEmpty(), "unmatched facet entry should be skipped");
    }
}
