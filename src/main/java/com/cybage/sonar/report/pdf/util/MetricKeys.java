package com.cybage.sonar.report.pdf.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;

public class MetricKeys {

    private MetricKeys() {
        // utility class
    }

    // Reliability Metric Keys
    public static final String BUGS                               = "bugs";
    public static final String NEW_BUGS                           = "new_bugs";
    public static final String RELIABILITY_RATING                 = "reliability_rating";
    public static final String RELIABILITY_REMEDIATION_EFFORT     = "reliability_remediation_effort";
    public static final String NEW_RELIABILITY_REMEDIATION_EFFORT = "new_reliability_remediation_effort";

    // SECURITY
    public static final String VULNERABILITIES                 = "vulnerabilities";
    public static final String NEW_VULNERABILITIES             = "new_vulnerabilities";
    public static final String SECURITY_RATING                 = "security_rating";
    public static final String SECURITY_REMEDIATION_EFFORT     = "security_remediation_effort";
    public static final String NEW_SECURITY_REMEDIATION_EFFORT = "new_security_remediation_effort";

    // Maintainability
    public static final String CODE_SMELLS                              = "code_smells";
    public static final String NEW_CODE_SMELLS                          = "new_code_smells";
    public static final String SQALE_RATING                             = "sqale_rating";
    public static final String SQALE_INDEX                              = "sqale_index";
    public static final String NEW_TECHNICAL_DEBT                       = "new_technical_debt";
    public static final String SQALE_DEBT_RATIO                         = "sqale_debt_ratio";
    public static final String NEW_SQALE_DEBT_RATIO                     = "new_sqale_debt_ratio";
    public static final String EFFORT_TO_REACH_MAINTAINABILITY_RATING_A = "effort_to_reach_maintainability_rating_a";


    // Coverage
    public static final String COVERAGE             = "coverage";
    public static final String UNITTESTS            = "tests";
    public static final String LINE_COVERAGE        = "line_coverage";
    public static final String BRANCH_COVERAGE      = "branch_coverage";
    public static final String UNCOVERED_LINES      = "uncovered_lines";
    public static final String UNCOVERED_CONDITIONS = "uncovered_conditions";
    public static final String LINES_TO_COVER       = "lines_to_cover";
    // test_errors test_failures
    // skipped_tests test_success_density

    // Duplications
    public static final String DUPLICATED_LINES_DENSITY = "duplicated_lines_density";
    public static final String DUPLICATED_BLOCKS        = "duplicated_blocks";
    public static final String DUPLICATED_LINES         = "duplicated_lines";
    public static final String DUPLICATED_FILES         = "duplicated_files";

    // SIZE
    public static final String NCLOC       = "ncloc";
    public static final String LINES       = "lines";
    public static final String STATEMENTS  = "statements";
    public static final String FUNCTIONS   = "functions";
    public static final String CLASSES     = "classes";
    public static final String FILES       = "files";
    public static final String DIRECTORIES = "directories";

    // Complexity
    public static final String COMPLEXITY          = "complexity";
    public static final String FUNCTION_COMPLEXITY = "function_complexity";
    public static final String FILE_COMPLEXITY     = "file_complexity";
    public static final String CLASS_COMPLEXITY    = "class_complexity";

    // Documentations
    public static final String COMMENT_LINES_DENSITY = "comment_lines_density";
    public static final String COMMENT_LINES         = "comment_lines";

    // Issues
    public static final String VIOLATIONS            = "violations";
    public static final String NEW_VIOLATIONS        = "new_violations";
    public static final String OPEN_ISSUES           = "open_issues";
    public static final String REOPENED_ISSUES       = "reopened_issues";
    public static final String CONFIRMED_ISSUES      = "confirmed_issues";
    public static final String FALSE_POSITIVE_ISSUES = "false_positive_issues";
    public static final String WONT_FIX_ISSUES       = "wont_fix_issues";

    // Others
    public static final String PROFILE = "profile";

    // Get all metric keys as list
    public static Set<String> getAllMetricKeys() {
        Set<String> allMetricKeys = new HashSet<>(Arrays.asList(BUGS, NEW_BUGS, RELIABILITY_RATING, NEW_RELIABILITY_REMEDIATION_EFFORT,
                RELIABILITY_REMEDIATION_EFFORT, VULNERABILITIES, NEW_VULNERABILITIES, SECURITY_RATING,
                SECURITY_REMEDIATION_EFFORT, NEW_SECURITY_REMEDIATION_EFFORT, CODE_SMELLS, NEW_CODE_SMELLS,
                SQALE_RATING, SQALE_INDEX, NEW_TECHNICAL_DEBT, SQALE_DEBT_RATIO, NEW_SQALE_DEBT_RATIO,
                EFFORT_TO_REACH_MAINTAINABILITY_RATING_A, DUPLICATED_LINES_DENSITY, DUPLICATED_BLOCKS, DUPLICATED_LINES,
                DUPLICATED_FILES, NCLOC, LINES, STATEMENTS, FUNCTIONS, CLASSES, FILES, DIRECTORIES, COMPLEXITY,
                FUNCTION_COMPLEXITY, FILE_COMPLEXITY, CLASS_COMPLEXITY, COMMENT_LINES_DENSITY, COMMENT_LINES,
                VIOLATIONS, NEW_VIOLATIONS, OPEN_ISSUES, REOPENED_ISSUES, CONFIRMED_ISSUES, FALSE_POSITIVE_ISSUES,
                COVERAGE, LINE_COVERAGE, BRANCH_COVERAGE, UNCOVERED_LINES,
                UNCOVERED_CONDITIONS, LINES_TO_COVER,
                WONT_FIX_ISSUES));

        return allMetricKeys;
    }

    // Get list of strings as single string separated by comma
    public static String getAllMetricKeysAsString(List<String> metricKeys) {
        return Joiner.on(",").join(metricKeys);
    }
}
