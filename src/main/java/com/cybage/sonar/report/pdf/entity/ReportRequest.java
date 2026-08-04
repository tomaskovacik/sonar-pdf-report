package com.cybage.sonar.report.pdf.entity;

import java.util.List;
import java.util.Set;

/**
 * Bundles the parameters that describe what to report on (as opposed to where/how
 * to publish it), so they can be threaded through PDFPostJob -> PDFGenerator ->
 * PDFReporter as a single value instead of half a dozen individual arguments.
 */
public class ReportRequest {

    private final String                  projectKey;
    private final String                  projectVersion;
    private final List<String>            sonarLanguage;
    private final Set<String>             otherMetrics;
    private final Set<String>             typesOfIssue;
    private final LeakPeriodConfiguration leakPeriod;
    private final String                  branchName;

    public ReportRequest(String projectKey, String projectVersion, List<String> sonarLanguage,
                          Set<String> otherMetrics, Set<String> typesOfIssue, LeakPeriodConfiguration leakPeriod,
                          String branchName) {
        this.projectKey = projectKey;
        this.projectVersion = projectVersion;
        this.sonarLanguage = sonarLanguage;
        this.otherMetrics = otherMetrics;
        this.typesOfIssue = typesOfIssue;
        this.leakPeriod = leakPeriod;
        this.branchName = branchName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public List<String> getSonarLanguage() {
        return sonarLanguage;
    }

    public Set<String> getOtherMetrics() {
        return otherMetrics;
    }

    public Set<String> getTypesOfIssue() {
        return typesOfIssue;
    }

    public LeakPeriodConfiguration getLeakPeriod() {
        return leakPeriod;
    }

    public String getBranchName() {
        return branchName;
    }
}
