package com.cybage.sonar.report.pdf.util;

import static com.cybage.sonar.report.pdf.util.MetricKeys.BUGS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.CLASSES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.CLASS_COMPLEXITY;
import static com.cybage.sonar.report.pdf.util.MetricKeys.CODE_SMELLS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.COMMENT_LINES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.COMMENT_LINES_DENSITY;
import static com.cybage.sonar.report.pdf.util.MetricKeys.CONFIRMED_ISSUES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.DIRECTORIES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.DUPLICATED_BLOCKS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.DUPLICATED_FILES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.DUPLICATED_LINES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.DUPLICATED_LINES_DENSITY;
import static com.cybage.sonar.report.pdf.util.MetricKeys.EFFORT_TO_REACH_MAINTAINABILITY_RATING_A;
import static com.cybage.sonar.report.pdf.util.MetricKeys.FALSE_POSITIVE_ISSUES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.FILES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.FILE_COMPLEXITY;
import static com.cybage.sonar.report.pdf.util.MetricKeys.FUNCTIONS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.FUNCTION_COMPLEXITY;
import static com.cybage.sonar.report.pdf.util.MetricKeys.LINES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NCLOC;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_BUGS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_CODE_SMELLS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_RELIABILITY_REMEDIATION_EFFORT;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_SECURITY_REMEDIATION_EFFORT;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_SQALE_DEBT_RATIO;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_TECHNICAL_DEBT;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_VIOLATIONS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.NEW_VULNERABILITIES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.OPEN_ISSUES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.RELIABILITY_RATING;
import static com.cybage.sonar.report.pdf.util.MetricKeys.RELIABILITY_REMEDIATION_EFFORT;
import static com.cybage.sonar.report.pdf.util.MetricKeys.REOPENED_ISSUES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.SECURITY_RATING;
import static com.cybage.sonar.report.pdf.util.MetricKeys.SECURITY_REMEDIATION_EFFORT;
import static com.cybage.sonar.report.pdf.util.MetricKeys.SQALE_DEBT_RATIO;
import static com.cybage.sonar.report.pdf.util.MetricKeys.SQALE_INDEX;
import static com.cybage.sonar.report.pdf.util.MetricKeys.SQALE_RATING;
import static com.cybage.sonar.report.pdf.util.MetricKeys.STATEMENTS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.VIOLATIONS;
import static com.cybage.sonar.report.pdf.util.MetricKeys.VULNERABILITIES;
import static com.cybage.sonar.report.pdf.util.MetricKeys.WONT_FIX_ISSUES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetricDomains {

    private MetricDomains() {
        // utility class
    }

    public static final String RELIABILITY     = "Reliability";
    public static final String SECURITY        = "Security";
    public static final String MAINTAINABILITY = "Maintainability";
    public static final String COVERAGE        = "Coverage";
    public static final String DUPLICATIONS    = "Duplications";
    public static final String SIZE            = "Size";
    public static final String COMPLEXITY      = "Complexity";
    public static final String DOCUMENTATION   = "Documentation";
    public static final String ISSUES          = "Issues";

    private static final Map<String, List<String>> metricMap;

    static {
        metricMap = new HashMap<>();
        metricMap.put(RELIABILITY, Arrays.asList(BUGS, NEW_BUGS, RELIABILITY_RATING, RELIABILITY_REMEDIATION_EFFORT,
                NEW_RELIABILITY_REMEDIATION_EFFORT));
        metricMap.put(SECURITY, Arrays.asList(VULNERABILITIES, NEW_VULNERABILITIES, SECURITY_RATING,
                SECURITY_REMEDIATION_EFFORT, NEW_SECURITY_REMEDIATION_EFFORT));
        metricMap.put(MAINTAINABILITY, Arrays.asList(CODE_SMELLS, NEW_CODE_SMELLS, SQALE_RATING, SQALE_INDEX,
                NEW_TECHNICAL_DEBT, SQALE_DEBT_RATIO, NEW_SQALE_DEBT_RATIO, EFFORT_TO_REACH_MAINTAINABILITY_RATING_A));
        metricMap.put(COVERAGE, Arrays.asList(MetricKeys.COVERAGE,
                MetricKeys.LINE_COVERAGE, MetricKeys.BRANCH_COVERAGE, MetricKeys.UNCOVERED_LINES,
                MetricKeys.UNCOVERED_CONDITIONS, MetricKeys.LINES_TO_COVER));
        metricMap.put(DUPLICATIONS,
                Arrays.asList(DUPLICATED_LINES_DENSITY, DUPLICATED_BLOCKS, DUPLICATED_LINES, DUPLICATED_FILES));
        metricMap.put(SIZE, Arrays.asList(NCLOC, LINES, STATEMENTS, FUNCTIONS, CLASSES, FILES, DIRECTORIES));
        metricMap.put(COMPLEXITY, Arrays.asList(COMPLEXITY, FUNCTION_COMPLEXITY, FILE_COMPLEXITY, CLASS_COMPLEXITY));
        metricMap.put(DOCUMENTATION, Arrays.asList(COMMENT_LINES_DENSITY, COMMENT_LINES));
        metricMap.put(ISSUES, Arrays.asList(VIOLATIONS, NEW_VIOLATIONS, OPEN_ISSUES, REOPENED_ISSUES, CONFIRMED_ISSUES,
                FALSE_POSITIVE_ISSUES, WONT_FIX_ISSUES));
    }

    public static List<String> getMetricKeys(String domain) {
        return metricMap.get(domain);
    }

    public static Set<String> getDomains() {
        // COVERAGE
        return new HashSet<>(Arrays.asList(RELIABILITY, SECURITY, MAINTAINABILITY, DUPLICATIONS, SIZE, COMPLEXITY,
                DOCUMENTATION, ISSUES));
    }

}
