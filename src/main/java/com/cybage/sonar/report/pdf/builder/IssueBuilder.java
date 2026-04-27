package com.cybage.sonar.report.pdf.builder;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.client.WsClient;

import com.cybage.sonar.report.pdf.entity.Issue;
import org.sonarqube.ws.client.issues.SearchRequest;

import static com.google.common.collect.ImmutableList.of;

public class IssueBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueBuilder.class);

    private static IssueBuilder builder;

    private final WsClient wsClient;

    public IssueBuilder(final WsClient wsClient) {
        this.wsClient = wsClient;
    }

    public static IssueBuilder getInstance(final WsClient wsClient) {
        if (builder == null) {
            return new IssueBuilder(wsClient);
        }
        return builder;
    }

    public List<Issue> initIssueDetailsByProjectKey(final String key, final Set<String> typesOfIssue) {

        // LOGGER.info("Retrieving issue details for " + key);

        List<Issue> issues     = new ArrayList<>();
        Integer     pageNumber = 1;
        Integer     pageSize   = 500;

        final List<String> TypeOfIssuesConvertedParam = convertTypes(typesOfIssue);

        while (true) {
            SearchWsResponse searchWsRes = searchForPaginatedIssues(key, pageNumber, pageSize, TypeOfIssuesConvertedParam);

            if (searchWsRes.getTotal() > 0) {
                for (int i = 0; i < searchWsRes.getIssuesCount(); i++) {
                    org.sonarqube.ws.Issues.Issue issue = searchWsRes.getIssues(i);

                    String component     = findComponent(searchWsRes, issue).orElseThrow(() -> new IllegalArgumentException("Component not found"));
                    String componentPath = findComponentPath(searchWsRes, issue).orElseThrow(() -> new IllegalArgumentException("Component path not found"));

                    issues.add(newIssue(issue, component, componentPath));
                }
                if (searchWsRes.getTotal() > (pageNumber * pageSize)) {
                    pageNumber++;
                } else {
                    break;
                }
            } else {
                LOGGER.debug("There are no issues in project : " + key);
                break;
            }
        }
        return issues;
    }

    private List<String> convertTypes(final Set<String> typesOfIssue) {
        return typesOfIssue.stream()
                           .map(String::toUpperCase)
                           .collect(Collectors.toList());
    }

    private SearchWsResponse searchForPaginatedIssues(final String key, final Integer pageNumber, final Integer pageSize, final List<String> typeOfIssuesConvertedParam) {
        SearchRequest searchWsReq = new SearchRequest();
        searchWsReq.setComponentKeys(of(key));
        searchWsReq.setP(String.valueOf(pageNumber));
        searchWsReq.setPs(String.valueOf(pageSize));
        searchWsReq.setStatuses(of("OPEN"));

        searchWsReq.setTypes(typeOfIssuesConvertedParam);
        SearchWsResponse searchWsRes = wsClient.issues().search(searchWsReq);
        return searchWsRes;
    }

    private Optional<String> findComponent(final SearchWsResponse searchWsRes, final org.sonarqube.ws.Issues.Issue issue) {
        return searchWsRes.getComponentsList().stream()
                          .filter(c -> c.getKey().equals(issue.getComponent()))
                          .map(Issues.Component::getName)
                          .findFirst();
    }

    private Optional<String> findComponentPath(final SearchWsResponse searchWsRes, final org.sonarqube.ws.Issues.Issue issue) {
        return searchWsRes.getComponentsList().stream()
                          .filter(c -> c.getKey().equals(issue.getComponent()))
                          .map(Issues.Component::getLongName)
                          .findFirst();
    }

    private Issue newIssue(final Issues.Issue issue, final String component, final String componentPath) {
        final String severityName = issue.getSeverity().name();
        final String typeName     = issue.getType().name();
        return new Issue(component,
                componentPath, severityName,
                issue.getLine(),
                issue.getStatus(),
                issue.getMessage().replace("\\\"", "\""),
                typeName,
                issue.getEffort());
    }
}
