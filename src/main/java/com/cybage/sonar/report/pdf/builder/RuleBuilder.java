package com.cybage.sonar.report.pdf.builder;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Common.FacetValue;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.client.WsClient;

import com.cybage.sonar.report.pdf.entity.Priority;
import com.cybage.sonar.report.pdf.entity.Rule;
import org.sonarqube.ws.client.issues.SearchRequest;

import static java.util.Collections.singletonList;

public class RuleBuilder {

    public static final  int         TOP_MAX_VIOLATED_RULES = 10;
    private static final Logger      LOGGER                 = LoggerFactory.getLogger(RuleBuilder.class);
    private static       RuleBuilder builder;

    private final WsClient wsClient;

    public RuleBuilder(final WsClient wsClient) {
        this.wsClient = wsClient;
    }

    public static RuleBuilder getInstance(final WsClient wsClient) {
        if (builder == null) {
            return new RuleBuilder(wsClient);
        }

        return builder;
    }

    public List<Rule> initProjectMostViolatedRulesByProjectKey(final String key) {

        String[]   priorities = Priority.getPrioritiesArray();
        List<Rule> rules      = new ArrayList<>();

        // Reverse iteration to get violations with upper level first
        for (int i = priorities.length - 1; i >= 0; i--) {
            SearchWsResponse searchWsRes = searchViolationsPerPriority(key, priorities[i]);

            final Common.Facet projectResources = searchWsRes.getFacets().getFacets(0);
            if (projectResources != null) {
                int limit = Math.min(projectResources.getValuesCount(), TOP_MAX_VIOLATED_RULES);
                for (int j = 0; j < limit; j++) {
                    FacetValue            facetValue   = projectResources.getValues(j);
                    Optional<Common.Rule> optionalRule = findRuleResult(searchWsRes, facetValue);
                    if (!optionalRule.isPresent()) {
                        continue;
                    }
                    final Common.Rule     rule         = optionalRule.orElseThrow();
                    final String          priority     = Priority.getPriority(priorities[i]);
                    rules.add(newRule(facetValue, rule, priority));
                }
            } else {
                LOGGER.debug("There are no violations with level {}", priorities[i]);
            }
        }

        return rules;

    }

    private SearchWsResponse searchViolationsPerPriority(final String key, final String priority1) {
        SearchRequest searchWsReq = new SearchRequest();
        searchWsReq.setComponentKeys(singletonList(key));
        searchWsReq.setAdditionalFields(singletonList("rules"));
        searchWsReq.setFacets(singletonList("rules"));
        searchWsReq.setSeverities(singletonList(priority1));
        return wsClient.issues().search(searchWsReq);
    }

    private Optional<Common.Rule> findRuleResult(final SearchWsResponse searchWsRes, final FacetValue facetValue) {
        final List<Common.Rule> rulesList = searchWsRes.getRules().getRulesList();
        return rulesList.stream()
                        .filter(r -> Objects.equals(r.getKey(), facetValue.getVal()))
                        .findFirst();
    }

    private Rule newRule(final FacetValue facetValue, final Common.Rule rule, final String priority) {
        return new Rule(facetValue.getVal(),
                rule.getName(),
                facetValue.getCount(),
                rule.getLangName(),
                priority);
    }
}
