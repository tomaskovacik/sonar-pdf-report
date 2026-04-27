package com.cybage.sonar.report.pdf.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.Common.Metric;

import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.HttpException;
import org.sonarqube.ws.client.WsClient;

import com.cybage.sonar.report.pdf.entity.Measure;
import com.cybage.sonar.report.pdf.entity.Period_;
import com.cybage.sonar.report.pdf.entity.exception.ReportException;
import com.cybage.sonar.report.pdf.util.MetricKeys;
import org.sonarqube.ws.client.measures.ComponentRequest;

import static com.cybage.sonar.report.pdf.builder.WSParameters.*;

public class MeasuresBuilder {

    private static final Logger          LOGGER              = LoggerFactory.getLogger(MeasuresBuilder.class);
    private static final Integer         DEFAULT_SPLIT_LIMIT = 20;
    private static       MeasuresBuilder builder;
    private              Set<String>     measuresKeys        = null;
    private final        WsClient        wsClient;

    public MeasuresBuilder(final WsClient wsClient) {
        this.wsClient = wsClient;
    }

    public static MeasuresBuilder getInstance(final WsClient wsClient) {
        if (builder == null) {
            return new MeasuresBuilder(wsClient);
        }

        return builder;
    }

    public com.cybage.sonar.report.pdf.entity.Measures initMeasuresByProjectKey(final String projectKey, final Set<String> otherMetrics)
            throws IOException, ReportException {

        com.cybage.sonar.report.pdf.entity.Measures measures = new com.cybage.sonar.report.pdf.entity.Measures();
        if (measuresKeys == null) {
            measuresKeys = MetricKeys.getAllMetricKeys();
            if (otherMetrics != null) {
                measuresKeys.addAll(otherMetrics);
            }
        }

        // Avoid "Post too large"
        if (measuresKeys.size() > DEFAULT_SPLIT_LIMIT) {
            initMeasuresSplittingRequests(measures, projectKey);
        } else {
            this.addMeasures(measures, measuresKeys, projectKey);
        }

        return measures;

    }

    /**
     * This method does the required requests to get all measures from Sonar,
     * but taking care to avoid too large requests (measures are taken by 20).
     *
     * @throws ReportException
     */
    private void initMeasuresSplittingRequests(final com.cybage.sonar.report.pdf.entity.Measures measures, final String projectKey)
            throws IOException, ReportException {
        Iterator<String> it = new ArrayList<>(measuresKeys).iterator();
        // LOGGER.debug("Getting " + measuresKeys.size() + " metric measures from Sonar by splitting requests");
        Set<String> twentyMeasures = new HashSet<String>(20);
        int         i              = 0;
        while (it.hasNext()) {
            twentyMeasures.add(it.next());
            i++;
            if (i % DEFAULT_SPLIT_LIMIT == 0) {
                // LOGGER.debug("Split request for: " + twentyMeasures);
                addMeasures(measures, twentyMeasures, projectKey);
                i = 0;
                twentyMeasures.clear();
            }
        }
        if (i != 0) {
            // LOGGER.debug("Split request for remain metric measures: " + twentyMeasures);
            addMeasures(measures, twentyMeasures, projectKey);
        }
    }

    /**
     * Add measures to this. If the server rejects metric keys as unsupported, those keys are
     * removed and the request is retried. The retry depth is bounded by the number of distinct
     * unsupported keys, so the recursion always terminates.
     *
     * @throws ReportException
     */
    private void addMeasures(final com.cybage.sonar.report.pdf.entity.Measures measures,
                             final Set<String> measuresAsString,
                             final String projectKey)
            throws ReportException {
        LOGGER.info("Adding measures for the metrics {} and project {}", measuresAsString, projectKey);
        ComponentRequest compWsReq = new ComponentRequest();
        compWsReq.setComponent(projectKey);
        compWsReq.setAdditionalFields(Arrays.asList(METRICS, PERIOD));
        compWsReq.setMetricKeys(new ArrayList<>(measuresAsString));

        try {
            org.sonarqube.ws.Measures.ComponentWsResponse compWsRes = wsClient.measures().component(compWsReq);

            if (compWsRes.getComponent().getMeasuresCount() != 0) {
                this.addAllMeasuresFromDocument(measures, compWsRes);
            } else {
                LOGGER.debug("Empty response when looking for measures: " + measuresAsString.toString());
            }
        } catch (HttpException e) {
            retryAfterRemovingUnsupportedKeys(measures, measuresAsString, projectKey, e);
        }
    }

    /**
     * Handles a 404 HttpException from the measures API by removing unsupported metric keys and
     * retrying the request. Rethrows the exception if it is not a 404 or if no unsupported keys
     * can be parsed.
     */
    private void retryAfterRemovingUnsupportedKeys(final com.cybage.sonar.report.pdf.entity.Measures measures,
                                                   final Set<String> measuresAsString,
                                                   final String projectKey,
                                                   final HttpException e) throws ReportException {
        if (e.code() != 404) {
            throw e;
        }
        Set<String> unsupportedKeys = parseUnsupportedMetricKeys(e.content());
        if (unsupportedKeys.isEmpty()) {
            LOGGER.warn("Received 404 from server but could not parse unsupported metric keys from: {}", e.content());
            throw e;
        }
        LOGGER.warn("Removing unsupported metric keys from server: {}", unsupportedKeys);
        measuresAsString.removeAll(unsupportedKeys);
        if (measuresKeys != null) {
            measuresKeys.removeAll(unsupportedKeys);
        }
        if (!measuresAsString.isEmpty()) {
            addMeasures(measures, measuresAsString, projectKey);
        }
    }

    /**
     * Parses metric keys that the server reported as not found from a 404 error response body.
     * Expected format: {"errors":[{"msg":"The following metric keys are not found: key1, key2"}]}
     *
     * @param errorContent the HTTP response body from SonarQube (may be null)
     * @return a set of unsupported metric key names, or an empty set if none could be parsed
     */
    private Set<String> parseUnsupportedMetricKeys(final String errorContent) {
        Set<String> keys = new HashSet<>();
        if (errorContent != null) {
            String prefix = "The following metric keys are not found: ";
            int idx = errorContent.indexOf(prefix);
            if (idx >= 0) {
                String keysPart = errorContent.substring(idx + prefix.length());
                int end = keysPart.indexOf('"');
                if (end >= 0) {
                    keysPart = keysPart.substring(0, end);
                }
                for (String k : keysPart.split(",")) {
                    String trimmed = k.trim();
                    if (!trimmed.isEmpty()) {
                        keys.add(trimmed);
                    }
                }
            }
        }
        return keys;
    }

    private void addAllMeasuresFromDocument(final com.cybage.sonar.report.pdf.entity.Measures measures,
                                            final org.sonarqube.ws.Measures.ComponentWsResponse compWsRes) throws ReportException {
        List<Measures.Measure> allNodes = compWsRes.getComponent().getMeasuresList();
        Measures.Metrics       metrics  = compWsRes.getMetrics();

        // SonarQube 10.x+ uses a single new-code period instead of a list of periods.
        List<Period_> periods;
        if (compWsRes.hasPeriod()) {
            Measures.Period p = compWsRes.getPeriod();
            periods = Collections.singletonList(new Period_(p.getIndex(), p.getMode(), p.getDate(), p.getParameter()));
        } else {
            periods = Collections.emptyList();
        }
        measures.setPeriods(periods);

        LOGGER.info("Found {} measures", allNodes.size());
        LOGGER.info("Found {} periods", periods.size());
        for (Measures.Measure measure : allNodes) {
            Optional<Metric> optionalMetric = metrics.getMetricsList().stream().filter(m -> m.getKey().equals(measure.getMetric())).findFirst();
            if (!optionalMetric.isPresent()) {
                throw new IllegalArgumentException("Measure was not found :" + measure.getMetric());
            }
            addMeasureFromNode(measures, measure, optionalMetric.get());
        }

    }

    private void addMeasureFromNode(final com.cybage.sonar.report.pdf.entity.Measures measures, final Measures.Measure measureNode,
                                    Metric metric) {
        Measure measure = MeasureBuilder.initFromNode(measureNode, measures.getPeriods(), metric);
        measures.addMeasure(measure.getMetric(), measure);
    }
}
