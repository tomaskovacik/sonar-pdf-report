package com.cybage.sonar.report.pdf.builder;

import com.cybage.sonar.report.pdf.entity.FileInfo;
import com.cybage.sonar.report.pdf.util.MetricKeys;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Common.FacetValue;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.sonarqube.ws.client.measures.ComponentTreeRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import static java.util.Collections.singletonList;

public class FileInfoBuilder {

    public static final  String          FACET_FILES                      = "files";
    public static final  int             NUMBER_ISSUES_PER_PAGE           = 500;
    public static final  int             LIMIT                            = 10;
    public static final  String          S_METRIC                         = "metric";
    public static final  String          S_METRIC_PERIOD                  = "metricPeriod";
    public static final  String          S_NAME                           = "name";
    public static final  String          S_PATH                           = "path";
    public static final  String          S_QUALIFIER                      = "qualifier";
    public static final  String          S_METRIC_SORT_WITH_MEASURES_ONLY = "withMeasuresOnly";
    public static final  String          S_QUALIFIER_FIL                  = "FIL";
    private static final Logger          LOGGER                           = LoggerFactory.getLogger(FileInfoBuilder.class);
    private static       FileInfoBuilder builder;

    private final WsClient wsClient;

    public FileInfoBuilder(final WsClient wsClient) {
        this.wsClient = wsClient;
    }

    public static FileInfoBuilder getInstance(final WsClient wsClient) {
        if (builder == null) {
            return new FileInfoBuilder(wsClient);
        }

        return builder;
    }

    public List<FileInfo> initProjectMostViolatedFilesByProjectKey(final String key) {

        LOGGER.info("Retrieving most violated files info for {}", key);
        List<FileInfo> files = new ArrayList<>();

        SearchWsResponse searchWsRes = searchForIssues(key);
        // Facets is the list of components or resources.
        final Common.Facet projectResourceFacets = searchWsRes.getFacets().getFacets(0);
        if (projectResourceFacets != null) {
            int                   limit          = getLowerBound(LIMIT, projectResourceFacets.getValuesCount());
            FacetValue            facetValue     = projectResourceFacets.getValues(0);
            final List<Component> componentsList = searchWsRes.getComponentsList();
            LOGGER.info("Components to scan {}", componentsList.size());

            final List<FileInfo> topFileInfo
                    = componentsList.stream()
                                    .filter(retrievingFileComponent(facetValue))
                                    .limit(limit)
                                    .map(newFileInfo(facetValue))
                                    .toList();
            files.addAll(topFileInfo);
        } else {
            LOGGER.debug("There are no violated files");
        }
        return files;
    }

    private SearchWsResponse searchForIssues(final String key) {
        SearchRequest searchWsReq = new SearchRequest();
        searchWsReq.setComponentKeys(singletonList(key));
        searchWsReq.setFacets(singletonList(FACET_FILES));
        searchWsReq.setPs("" + NUMBER_ISSUES_PER_PAGE);
        return wsClient.issues().search(searchWsReq);
    }

    private int getLowerBound(final int limit, final int valuesCount) {
        return Math.min(valuesCount, limit);
    }

    private Predicate<Component> retrievingFileComponent(final FacetValue facetValue) {

        return c -> (c.getPath().equals(facetValue.getVal()) && c.getQualifier().equals(S_QUALIFIER_FIL));
    }

    private Function<Component, FileInfo> newFileInfo(final FacetValue facetValue) {
        return component -> {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setKey(facetValue.getVal());
            fileInfo.setName(component.getName());
            fileInfo.setPath(component.getPath());
            fileInfo.setViolations(String.valueOf(facetValue.getCount()));
            fileInfo.setComplexity("0");
            fileInfo.setDuplicatedLines("0");
            return fileInfo;
        };
    }

    public List<FileInfo> initProjectMostComplexFilesByProjectKey(final String key) {

        List<FileInfo> files = new ArrayList<>();

        int limit = LIMIT;

        Measures.ComponentTreeWsResponse componentTreeWsRes = searchForMeasures(key, MetricKeys.COMPLEXITY, Lists.newArrayList(S_METRIC));

        if (componentTreeWsRes.getComponentsList() != null) {
            final int componentsCount = componentTreeWsRes.getComponentsCount();
            limit = getLowerBound(limit, componentsCount);
            for (int j = componentsCount - 1; j >= componentsCount - limit; j--) {
                Measures.Component component = componentTreeWsRes.getComponents(j);
                files.add(newComplexityFileInfo(component));
            }
        } else {
            LOGGER.debug("There are no complex files");
        }
        return files;
    }

    private Measures.ComponentTreeWsResponse searchForMeasures(final String key, final String complexity, final List<String> strings) {
        ComponentTreeRequest compTreeWsReq = new ComponentTreeRequest();
        compTreeWsReq.setComponent(key);
        compTreeWsReq.setMetricKeys(singletonList(complexity));
        compTreeWsReq.setMetricSort(complexity);
        compTreeWsReq.setS(strings);
        compTreeWsReq.setMetricSortFilter(S_METRIC_SORT_WITH_MEASURES_ONLY);
        compTreeWsReq.setQualifiers(singletonList(S_QUALIFIER_FIL));
        return wsClient.measures().componentTree(compTreeWsReq);
    }

    private FileInfo newComplexityFileInfo(final Measures.Component component) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setKey(component.getKey());
        fileInfo.setName(component.getName());
        fileInfo.setPath(component.getPath());
        fileInfo.setViolations("0");
        fileInfo.setComplexity(String.valueOf(component.getMeasures(0).getValue()));
        fileInfo.setDuplicatedLines("0");
        return fileInfo;
    }

    public List<FileInfo> initProjectMostDuplicatedFilesByProjectKey(final String key) {

        List<FileInfo> files = new ArrayList<>();

        Measures.ComponentTreeWsResponse componentTreeWsRes = searchForMeasures(key, MetricKeys.DUPLICATED_LINES, singletonList(S_METRIC));

        if (componentTreeWsRes.getComponentsList() != null) {
            int limit = getLowerBound(LIMIT, componentTreeWsRes.getComponentsCount());
            for (int j = componentTreeWsRes.getComponentsCount() - 1; j >= componentTreeWsRes.getComponentsCount() - limit; j--) {
                Measures.Component component = componentTreeWsRes.getComponents(j);
                files.add(newDuplicationFileInfo(component));
            }
        } else {
            LOGGER.debug("There are no duplicated files");
        }
        return files;

    }

    private FileInfo newDuplicationFileInfo(final Measures.Component component) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setKey(component.getKey());
        fileInfo.setName(component.getName());
        fileInfo.setPath(component.getPath());
        fileInfo.setViolations("0");
        fileInfo.setComplexity("0");
        fileInfo.setDuplicatedLines(String.valueOf(component.getMeasures(0).getValue()));
        return fileInfo;
    }

}
