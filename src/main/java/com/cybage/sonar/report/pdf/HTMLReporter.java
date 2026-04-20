package com.cybage.sonar.report.pdf;

import static com.cybage.sonar.report.pdf.util.MetricKeys.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybage.sonar.report.pdf.entity.Condition;
import com.cybage.sonar.report.pdf.entity.FileInfo;
import com.cybage.sonar.report.pdf.entity.Issue;
import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import com.cybage.sonar.report.pdf.entity.Period;
import com.cybage.sonar.report.pdf.entity.Period_;
import com.cybage.sonar.report.pdf.entity.Project;
import com.cybage.sonar.report.pdf.entity.QualityProfile;
import com.cybage.sonar.report.pdf.entity.Rule;
import com.cybage.sonar.report.pdf.entity.StatusPeriod;
import com.cybage.sonar.report.pdf.entity.exception.ReportException;
import com.cybage.sonar.report.pdf.entity.Priority;
import com.cybage.sonar.report.pdf.util.Credentials;
import com.cybage.sonar.report.pdf.util.MetricDomains;
import com.cybage.sonar.report.pdf.util.MetricKeys;
import com.cybage.sonar.report.pdf.util.ProjectStatusKeys;
import com.cybage.sonar.report.pdf.util.Rating;
import com.cybage.sonar.report.pdf.util.SonarUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Generates a self-contained HTML report with the same sections as the PDF report.
 * The report type is "html".
 */
public class HTMLReporter extends PDFReporter {

    private static final Logger LOGGER           = LoggerFactory.getLogger(HTMLReporter.class);
    private static final String REPORT_TYPE_HTML = "html";

    private final URL                     logo;
    private final String                  projectKey;
    private final String                  projectVersion;
    private final List<String>            sonarLanguage;
    private final Set<String>             typesOfIssue;
    private final LeakPeriodConfiguration leakPeriod;
    private final Properties              configProperties;
    private final Properties              langProperties;
    private       Set<String>             otherMetrics;

    public HTMLReporter(final Credentials credentials,
                        final URL logo,
                        final String projectKey,
                        final String projectVersion,
                        final List<String> sonarLanguage,
                        final Set<String> otherMetrics,
                        final Set<String> typesOfIssue,
                        final LeakPeriodConfiguration leakPeriod,
                        final Properties configProperties,
                        final Properties langProperties) {
        super(credentials);
        this.logo             = logo;
        this.projectKey       = projectKey;
        this.projectVersion   = projectVersion;
        this.sonarLanguage    = sonarLanguage;
        this.otherMetrics     = otherMetrics;
        this.typesOfIssue     = typesOfIssue;
        this.leakPeriod       = leakPeriod;
        this.configProperties = configProperties;
        this.langProperties   = langProperties;
    }

    @Override
    public ByteArrayOutputStream getReport() throws DocumentException, IOException, ReportException {
        LOGGER.info("Generating HTML report...");
        Project project = super.getProject();

        StringBuilder html = new StringBuilder();
        appendHeader(html, project);
        appendFrontPage(html, project);
        appendQualityProfiles(html, project);
        appendQualityGate(html, project);
        appendMetricDashboard(html, project);
        appendViolationsAnalysis(html, project);
        if (!this.typesOfIssue.isEmpty()) {
            appendIssueDetails(html, project);
        }
        appendFooter(html);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(html.toString().getBytes("UTF-8"));
        return baos;
    }

    // -------------------------------------------------------------------------
    // HTML structure helpers
    // -------------------------------------------------------------------------

    private void appendHeader(StringBuilder html, Project project) {
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n<head>\n")
            .append("<meta charset=\"UTF-8\">\n")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("<title>SonarQube Report – ").append(escape(project.getName())).append("</title>\n")
            .append("<style>\n")
            .append("body{font-family:Arial,sans-serif;margin:0;padding:0;background:#f5f5f5;color:#333}\n")
            .append(".container{max-width:1100px;margin:0 auto;padding:20px}\n")
            .append("h1{background:#4b9fd5;color:#fff;padding:20px 30px;margin:0}\n")
            .append("h2{color:#4b9fd5;border-bottom:2px solid #4b9fd5;padding-bottom:6px;margin-top:30px}\n")
            .append("h3{color:#555;margin-top:20px}\n")
            .append(".front-page{background:#fff;padding:30px;margin-bottom:20px;border-radius:4px;box-shadow:0 1px 3px rgba(0,0,0,.15)}\n")
            .append(".front-page .project-name{font-size:2em;font-weight:bold;color:#4b9fd5}\n")
            .append(".front-page .meta{color:#777;margin-top:8px}\n")
            .append(".section{background:#fff;padding:20px 30px;margin-bottom:20px;border-radius:4px;box-shadow:0 1px 3px rgba(0,0,0,.15)}\n")
            .append("table{border-collapse:collapse;width:100%;margin-top:10px}\n")
            .append("th{background:#4b9fd5;color:#fff;padding:8px 12px;text-align:left}\n")
            .append("td{padding:7px 12px;border-bottom:1px solid #ddd}\n")
            .append("tr:nth-child(even) td{background:#f9f9f9}\n")
            .append(".badge{display:inline-block;padding:4px 10px;border-radius:3px;font-weight:bold;color:#fff}\n")
            .append(".badge-ok{background:#4caf50}.badge-error{background:#f44336}.badge-none{background:#9e9e9e}\n")
            .append(".rating-a{color:#4caf50;font-weight:bold}.rating-b{color:#8bc34a;font-weight:bold}\n")
            .append(".rating-c{color:#ff9800;font-weight:bold}.rating-d{color:#ff5722;font-weight:bold}\n")
            .append(".rating-e{color:#f44336;font-weight:bold}\n")
            .append(".highlight{background:#fff9c4}\n")
            .append(".metric-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:12px;margin-top:12px}\n")
            .append(".metric-card{background:#f9f9f9;border:1px solid #ddd;border-radius:4px;padding:14px;text-align:center}\n")
            .append(".metric-card .value{font-size:1.8em;font-weight:bold;color:#4b9fd5}\n")
            .append(".metric-card .label{font-size:.82em;color:#777;margin-top:4px}\n")
            .append(".metric-card.new-code{background:#fff9c4;border-color:#f0e68c}\n")
            .append("</style>\n</head>\n<body>\n")
            .append("<h1>SonarQube Report</h1>\n")
            .append("<div class=\"container\">\n");
    }

    private void appendFrontPage(StringBuilder html, Project project) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        html.append("<div class=\"front-page\">\n")
            .append("<div class=\"project-name\">").append(escape(project.getName())).append("</div>\n")
            .append("<div class=\"meta\">Version: ").append(escape(project.getVersion())).append("</div>\n");
        if (project.getDescription() != null && !project.getDescription().isEmpty()) {
            html.append("<div class=\"meta\">").append(escape(project.getDescription())).append("</div>\n");
        }
        html.append("<div class=\"meta\">Generated: ").append(date).append("</div>\n")
            .append("</div>\n");
    }

    private void appendQualityProfiles(StringBuilder html, Project project) {
        html.append("<div class=\"section\">\n")
            .append("<h2>").append(escape(getTextProperty("general.quality_profile"))).append("</h2>\n")
            .append("<table>\n<thead><tr>")
            .append("<th>").append(escape(getTextProperty("general.profile_name"))).append("</th>")
            .append("<th>").append(escape(getTextProperty("general.language"))).append("</th>")
            .append("<th>").append(escape(getTextProperty("general.active_rules_count"))).append("</th>")
            .append("</tr></thead>\n<tbody>\n");

        List<QualityProfile> profiles = project.getQualityProfiles();
        if (profiles != null && !profiles.isEmpty()) {
            for (QualityProfile qp : profiles) {
                html.append("<tr>")
                    .append("<td>").append(escape(qp.getName())).append("</td>")
                    .append("<td>").append(escape(qp.getLanguageName())).append("</td>")
                    .append("<td>").append(qp.getActiveRuleCount()).append("</td>")
                    .append("</tr>\n");
            }
        }
        html.append("</tbody>\n</table>\n</div>\n");
    }

    private void appendQualityGate(StringBuilder html, Project project) {
        String status = project.getProjectStatus().getStatus();
        String badgeCss = ProjectStatusKeys.STATUS_OK.equals(status) ? "badge-ok"
                : ProjectStatusKeys.STATUS_ERROR.equals(status) ? "badge-error" : "badge-none";
        String statusLabel = ProjectStatusKeys.getStatusAsString(status);

        html.append("<div class=\"section\">\n")
            .append("<h2>").append(escape(getTextProperty("general.quality_gate"))).append("</h2>\n")
            .append("<p><span class=\"badge ").append(badgeCss).append("\">").append(escape(statusLabel))
            .append("</span></p>\n");

        if (ProjectStatusKeys.STATUS_ERROR.equals(status)) {
            Map<Integer, StatusPeriod> mapStatusPeriod = project.getProjectStatus().getStatusPeriods()
                                                                .stream()
                                                                .collect(Collectors.toMap(StatusPeriod::getIndex, Function.identity()));
            StatusPeriod defaultPeriod = mapStatusPeriod.isEmpty() ? null : mapStatusPeriod.values().iterator().next();

            List<Condition> failedConditions = project.getProjectStatus().getConditions().stream()
                                                      .filter(c -> ProjectStatusKeys.STATUS_ERROR.equals(c.getStatus()))
                                                      .collect(Collectors.toList());

            if (!failedConditions.isEmpty()) {
                html.append("<table>\n<thead><tr>")
                    .append("<th>Metric</th><th>Actual / Threshold</th><th>Status</th>")
                    .append("</tr></thead>\n<tbody>\n");

                for (Condition cond : failedConditions) {
                    StatusPeriod condPeriod = cond.getPeriodIndex() != null
                            ? mapStatusPeriod.get(cond.getPeriodIndex())
                            : defaultPeriod;
                    String metricLabel = StringUtils.capitalize(cond.getMetricKey().replace("_", " "));
                    if (condPeriod != null) {
                        metricLabel += " (since " + condPeriod.getMode().replace("_", " ") + ")";
                    }
                    html.append("<tr>")
                        .append("<td>").append(escape(metricLabel)).append("</td>")
                        .append("<td>").append(escape(cond.getActualValue())).append(" ")
                        .append(escape(ProjectStatusKeys.getComparatorAsString(cond.getComparator()))).append(" ")
                        .append(escape(cond.getErrorThreshold())).append("</td>")
                        .append("<td><span class=\"badge badge-error\">Failed</span></td>")
                        .append("</tr>\n");
                }
                html.append("</tbody>\n</table>\n");
            }
        }
        html.append("</div>\n");
    }

    private void appendMetricDashboard(StringBuilder html, Project project) {
        html.append("<div class=\"section\">\n")
            .append("<h2>").append(escape(getTextProperty("general.metric_dashboard"))).append("</h2>\n");

        Period_ period = getCurrentPeriod(project);
        String periodLabel = getTextProperty("general.period." + period.getMode());
        html.append("<p><strong>Leak Period:</strong> ").append(escape(periodLabel)).append("</p>\n");

        appendReliabilitySection(html, project, period);
        appendSecuritySection(html, project, period);
        appendMaintainabilitySection(html, project, period);
        appendCoverageSection(html, project);
        appendDuplicationsSection(html, project);
        appendSizeSection(html, project);
        appendComplexitySection(html, project);
        appendDocumentationSection(html, project);
        appendIssuesSection(html, project, period);

        if (otherMetrics != null) {
            Set<String> extras = otherMetrics.stream()
                                             .filter(om -> !MetricKeys.getAllMetricKeys().contains(om)
                                                     && project.getMeasures().containsMeasure(om)
                                                     && !MetricDomains.getDomains().contains(project.getMeasure(om).getDomain()))
                                             .collect(Collectors.toSet());
            if (!extras.isEmpty()) {
                appendOtherMetricsSection(html, project, extras);
            }
        }

        html.append("</div>\n");
    }

    private void appendReliabilitySection(StringBuilder html, Project project, Period_ period) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.RELIABILITY.toLowerCase())))
            .append("</h3>\n<div class=\"metric-grid\">\n");

        appendMetricCard(html, project.getMeasure(BUGS).getValue(),
                getTextProperty("metrics." + BUGS), false);

        if (project.getMeasures().containsMeasure(NEW_BUGS)) {
            Optional<Period> p = project.getMeasure(NEW_BUGS).getPeriods().stream()
                                        .filter(x -> x.getIndex().equals(period.getIndex())).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty("metrics." + NEW_BUGS), true);
            }
        }

        String rating = Rating.getRating(project.getMeasure(RELIABILITY_RATING).getValue());
        appendRatingCard(html, rating, getTextProperty("metrics." + RELIABILITY_RATING));
        html.append("</div>\n");

        html.append("<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + RELIABILITY_REMEDIATION_EFFORT),
                SonarUtil.getWorkDurConversion(
                        Integer.parseInt(project.getMeasure(RELIABILITY_REMEDIATION_EFFORT).getValue())));
        if (project.getMeasures().containsMeasure(NEW_RELIABILITY_REMEDIATION_EFFORT)) {
            Optional<Period> p = project.getMeasure(NEW_RELIABILITY_REMEDIATION_EFFORT).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricRow(html, getTextProperty("metrics." + NEW_RELIABILITY_REMEDIATION_EFFORT),
                        SonarUtil.getWorkDurConversion(Integer.parseInt(p.get().getValue())));
            }
        }
        html.append("</tbody>\n</table>\n");
    }

    private void appendSecuritySection(StringBuilder html, Project project, Period_ period) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.SECURITY.toLowerCase())))
            .append("</h3>\n<div class=\"metric-grid\">\n");

        appendMetricCard(html, project.getMeasure(VULNERABILITIES).getValue(),
                getTextProperty("metrics." + VULNERABILITIES), false);

        if (project.getMeasures().containsMeasure(NEW_VULNERABILITIES)) {
            Optional<Period> p = project.getMeasure(NEW_VULNERABILITIES).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty("metrics." + NEW_VULNERABILITIES), true);
            }
        }

        String rating = Rating.getRating(project.getMeasure(SECURITY_RATING).getValue());
        appendRatingCard(html, rating, getTextProperty("metrics." + SECURITY_RATING));
        html.append("</div>\n");

        html.append("<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + SECURITY_REMEDIATION_EFFORT),
                SonarUtil.getWorkDurConversion(
                        Integer.parseInt(project.getMeasure(SECURITY_REMEDIATION_EFFORT).getValue())));
        if (project.getMeasures().containsMeasure(NEW_SECURITY_REMEDIATION_EFFORT)) {
            Optional<Period> p = project.getMeasure(NEW_SECURITY_REMEDIATION_EFFORT).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricRow(html, getTextProperty("metrics." + NEW_SECURITY_REMEDIATION_EFFORT),
                        SonarUtil.getWorkDurConversion(Integer.parseInt(p.get().getValue())));
            }
        }
        html.append("</tbody>\n</table>\n");
    }

    private void appendMaintainabilitySection(StringBuilder html, Project project, Period_ period) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.MAINTAINABILITY.toLowerCase())))
            .append("</h3>\n<div class=\"metric-grid\">\n");

        appendMetricCard(html, project.getMeasure(CODE_SMELLS).getValue(),
                getTextProperty("metrics." + CODE_SMELLS), false);

        if (project.getMeasures().containsMeasure(NEW_CODE_SMELLS)) {
            Optional<Period> p = project.getMeasure(NEW_CODE_SMELLS).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty("metrics." + NEW_CODE_SMELLS), true);
            }
        }

        String rating = Rating.getRating(project.getMeasure(SQALE_RATING).getValue());
        appendRatingCard(html, rating, getTextProperty("metrics." + SQALE_RATING));
        html.append("</div>\n");

        html.append("<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + SQALE_INDEX),
                SonarUtil.getWorkDurConversion(Integer.parseInt(project.getMeasure(SQALE_INDEX).getValue())));
        appendMetricRow(html, getTextProperty("metrics." + SQALE_DEBT_RATIO),
                project.getMeasure(SQALE_DEBT_RATIO).getValue() + "%");
        if (project.getMeasures().containsMeasure(NEW_TECHNICAL_DEBT)) {
            Optional<Period> p = project.getMeasure(NEW_TECHNICAL_DEBT).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricRow(html, getTextProperty("metrics." + NEW_TECHNICAL_DEBT),
                        SonarUtil.getWorkDurConversion(Integer.parseInt(p.get().getValue())));
            }
        }
        if (project.getMeasures().containsMeasure(EFFORT_TO_REACH_MAINTAINABILITY_RATING_A)) {
            appendMetricRow(html, getTextProperty("metrics." + EFFORT_TO_REACH_MAINTAINABILITY_RATING_A),
                    SonarUtil.getWorkDurConversion(
                            Integer.parseInt(project.getMeasure(EFFORT_TO_REACH_MAINTAINABILITY_RATING_A).getValue())));
        }
        html.append("</tbody>\n</table>\n");
    }

    private void appendCoverageSection(StringBuilder html, Project project) {
        if (!project.getMeasures().containsMeasure(MetricKeys.COVERAGE)) {
            return;
        }
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.COVERAGE.toLowerCase())))
            .append("</h3>\n<div class=\"metric-grid\">\n");
        appendMetricCard(html, project.getMeasure(MetricKeys.COVERAGE).getValue() + "%",
                getTextProperty("metrics." + MetricKeys.COVERAGE), false);
        html.append("</div>\n");

        html.append("<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + MetricKeys.LINE_COVERAGE),
                project.getMeasure(MetricKeys.LINE_COVERAGE).getValue() + "%");
        appendMetricRow(html, getTextProperty("metrics." + MetricKeys.BRANCH_COVERAGE),
                project.getMeasure(MetricKeys.BRANCH_COVERAGE).getValue() + "%");
        appendMetricRow(html, getTextProperty("metrics." + MetricKeys.UNCOVERED_LINES),
                project.getMeasure(MetricKeys.UNCOVERED_LINES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + MetricKeys.UNCOVERED_CONDITIONS),
                project.getMeasure(MetricKeys.UNCOVERED_CONDITIONS).getValue());
        appendMetricRow(html, getTextProperty("metrics." + MetricKeys.LINES_TO_COVER),
                project.getMeasure(MetricKeys.LINES_TO_COVER).getValue());
        html.append("</tbody>\n</table>\n");
    }

    private void appendDuplicationsSection(StringBuilder html, Project project) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.DUPLICATIONS.toLowerCase())))
            .append("</h3>\n<div class=\"metric-grid\">\n");
        appendMetricCard(html, project.getMeasure(DUPLICATED_LINES_DENSITY).getValue() + "%",
                getTextProperty("metrics." + DUPLICATED_LINES_DENSITY), false);
        html.append("</div>\n");

        html.append("<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + DUPLICATED_LINES),
                project.getMeasure(DUPLICATED_LINES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + DUPLICATED_BLOCKS),
                project.getMeasure(DUPLICATED_BLOCKS).getValue());
        appendMetricRow(html, getTextProperty("metrics." + DUPLICATED_FILES),
                project.getMeasure(DUPLICATED_FILES).getValue());
        html.append("</tbody>\n</table>\n");
    }

    private void appendSizeSection(StringBuilder html, Project project) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.SIZE.toLowerCase())))
            .append("</h3>\n<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + NCLOC), project.getMeasure(NCLOC).getValue());
        appendMetricRow(html, getTextProperty("metrics." + LINES), project.getMeasure(LINES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + STATEMENTS), project.getMeasure(STATEMENTS).getValue());
        appendMetricRow(html, getTextProperty("metrics." + FUNCTIONS), project.getMeasure(FUNCTIONS).getValue());
        appendMetricRow(html, getTextProperty("metrics." + CLASSES), project.getMeasure(CLASSES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + FILES), project.getMeasure(FILES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + DIRECTORIES), project.getMeasure(DIRECTORIES).getValue());
        html.append("</tbody>\n</table>\n");
    }

    private void appendComplexitySection(StringBuilder html, Project project) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.COMPLEXITY.toLowerCase())))
            .append("</h3>\n<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + CLASS_COMPLEXITY),
                project.getMeasure(CLASS_COMPLEXITY).getValue());
        appendMetricRow(html, getTextProperty("metrics." + FUNCTION_COMPLEXITY),
                project.getMeasure(FUNCTION_COMPLEXITY).getValue());
        appendMetricRow(html, getTextProperty("metrics." + FILE_COMPLEXITY),
                project.getMeasure(FILE_COMPLEXITY).getValue());
        html.append("</tbody>\n</table>\n");
    }

    private void appendDocumentationSection(StringBuilder html, Project project) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.DOCUMENTATION.toLowerCase())))
            .append("</h3>\n<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + COMMENT_LINES),
                project.getMeasure(COMMENT_LINES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + COMMENT_LINES_DENSITY),
                project.getMeasure(COMMENT_LINES_DENSITY).getValue() + "%");
        html.append("</tbody>\n</table>\n");
    }

    private void appendIssuesSection(StringBuilder html, Project project, Period_ period) {
        html.append("<h3>").append(escape(getTextProperty("metrics." + MetricDomains.ISSUES.toLowerCase())))
            .append("</h3>\n<div class=\"metric-grid\">\n");
        appendMetricCard(html, project.getMeasure(VIOLATIONS).getValue(),
                getTextProperty("metrics." + VIOLATIONS), false);
        if (project.getMeasures().containsMeasure(NEW_VIOLATIONS)) {
            Optional<Period> p = project.getMeasure(NEW_VIOLATIONS).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty("metrics." + NEW_VIOLATIONS), true);
            }
        }
        html.append("</div>\n");

        html.append("<table>\n<tbody>\n");
        appendMetricRow(html, getTextProperty("metrics." + OPEN_ISSUES),
                project.getMeasure(OPEN_ISSUES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + REOPENED_ISSUES),
                project.getMeasure(REOPENED_ISSUES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + CONFIRMED_ISSUES),
                project.getMeasure(CONFIRMED_ISSUES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + FALSE_POSITIVE_ISSUES),
                project.getMeasure(FALSE_POSITIVE_ISSUES).getValue());
        appendMetricRow(html, getTextProperty("metrics." + WONT_FIX_ISSUES),
                project.getMeasure(WONT_FIX_ISSUES).getValue());
        html.append("</tbody>\n</table>\n");
    }

    private void appendOtherMetricsSection(StringBuilder html, Project project, Set<String> extras) {
        html.append("<h3>Other Metrics</h3>\n<table>\n<tbody>\n");
        for (String metricName : extras) {
            String value = project.getMeasure(metricName).getValue();
            if (value != null && !value.trim().isEmpty()) {
                appendMetricRow(html,
                        project.getMeasure(metricName).getMetricTitle(),
                        SonarUtil.getFormattedValue(value, project.getMeasure(metricName).getDataType()));
            }
        }
        html.append("</tbody>\n</table>\n");
    }

    private void appendViolationsAnalysis(StringBuilder html, Project project) {
        html.append("<div class=\"section\">\n")
            .append("<h2>").append(escape(getTextProperty("general.violations_analysis"))).append("</h2>\n");

        appendMostViolatedRules(html, project);
        appendMostViolatedFiles(html, project);
        appendMostComplexFiles(html, project);
        appendMostDuplicatedFiles(html, project);

        html.append("</div>\n");
    }

    private void appendMostViolatedRules(StringBuilder html, Project project) {
        html.append("<h3>").append(escape(getTextProperty("general.most_violated_rules"))).append("</h3>\n");
        List<Rule> rules = project.getMostViolatedRules();
        String[] priorities = Priority.getPrioritiesArray();
        for (String priority : priorities) {
            List<Rule> filtered = rules.stream()
                                       .filter(r -> r.getSeverity().equals(Priority.getPriority(priority)))
                                       .collect(Collectors.toList());
            html.append("<p><strong>Severity: ").append(escape(Priority.getPriority(priority))).append("</strong></p>\n");
            if (filtered.isEmpty()) {
                html.append("<p><em>").append(escape(getTextProperty("general.no_violated_rules")))
                    .append(" of Severity ").append(escape(Priority.getPriority(priority))).append("</em></p>\n");
            } else {
                html.append("<table>\n<thead><tr>")
                    .append("<th>").append(escape(getTextProperty("genaral.rule_name"))).append("</th>")
                    .append("<th>").append(escape(getTextProperty("general.language_name"))).append("</th>")
                    .append("<th>").append(escape(getTextProperty("general.rule_count"))).append("</th>")
                    .append("</tr></thead>\n<tbody>\n");
                for (Rule rule : filtered) {
                    html.append("<tr>")
                        .append("<td>").append(escape(rule.getName())).append("</td>")
                        .append("<td>").append(escape(rule.getLanguageName())).append("</td>")
                        .append("<td>").append(rule.getCount()).append("</td>")
                        .append("</tr>\n");
                }
                html.append("</tbody>\n</table>\n");
            }
        }
    }

    private void appendMostViolatedFiles(StringBuilder html, Project project) {
        List<FileInfo> files = project.getMostViolatedFiles().stream()
                                      .filter(f -> f.isContentSet(FileInfo.VIOLATIONS_CONTENT))
                                      .collect(Collectors.toList());
        html.append("<h3>").append(escape(getTextProperty("general.most_violated_files"))).append("</h3>\n");
        if (files.isEmpty()) {
            html.append("<p><em>").append(escape(getTextProperty("general.no_violated_files"))).append("</em></p>\n");
        } else {
            html.append("<table>\n<thead><tr>")
                .append("<th>").append(escape(getTextProperty("genaral.file_name"))).append("</th>")
                .append("<th>").append(escape(getTextProperty("general.file_path"))).append("</th>")
                .append("<th>").append(escape(getTextProperty("general.file_violations"))).append("</th>")
                .append("</tr></thead>\n<tbody>\n");
            for (FileInfo f : files) {
                html.append("<tr>")
                    .append("<td>").append(escape(f.getName())).append("</td>")
                    .append("<td>").append(escape(f.getPath())).append("</td>")
                    .append("<td>").append(escape(f.getViolations())).append("</td>")
                    .append("</tr>\n");
            }
            html.append("</tbody>\n</table>\n");
        }
    }

    private void appendMostComplexFiles(StringBuilder html, Project project) {
        List<FileInfo> files = project.getMostComplexFiles().stream()
                                      .filter(f -> f.isContentSet(FileInfo.CCN_CONTENT))
                                      .collect(Collectors.toList());
        html.append("<h3>").append(escape(getTextProperty("general.most_complex_files"))).append("</h3>\n");
        if (files.isEmpty()) {
            html.append("<p><em>").append(escape(getTextProperty("general.no_complex_files"))).append("</em></p>\n");
        } else {
            html.append("<table>\n<thead><tr>")
                .append("<th>").append(escape(getTextProperty("genaral.file_name"))).append("</th>")
                .append("<th>").append(escape(getTextProperty("general.file_path"))).append("</th>")
                .append("<th>").append(escape(getTextProperty("general.file_complexity"))).append("</th>")
                .append("</tr></thead>\n<tbody>\n");
            for (FileInfo f : files) {
                html.append("<tr>")
                    .append("<td>").append(escape(f.getName())).append("</td>")
                    .append("<td>").append(escape(f.getPath())).append("</td>")
                    .append("<td>").append(escape(f.getComplexity())).append("</td>")
                    .append("</tr>\n");
            }
            html.append("</tbody>\n</table>\n");
        }
    }

    private void appendMostDuplicatedFiles(StringBuilder html, Project project) {
        List<FileInfo> files = project.getMostDuplicatedFiles().stream()
                                      .filter(f -> f.isContentSet(FileInfo.DUPLICATIONS_CONTENT))
                                      .collect(Collectors.toList());
        html.append("<h3>").append(escape(getTextProperty("general.most_duplicated_files"))).append("</h3>\n");
        if (files.isEmpty()) {
            html.append("<p><em>").append(escape(getTextProperty("general.no_duplicated_files"))).append("</em></p>\n");
        } else {
            html.append("<table>\n<thead><tr>")
                .append("<th>").append(escape(getTextProperty("genaral.file_name"))).append("</th>")
                .append("<th>").append(escape(getTextProperty("general.file_path"))).append("</th>")
                .append("<th>").append(escape(getTextProperty("general.file_duplicated_lines"))).append("</th>")
                .append("</tr></thead>\n<tbody>\n");
            for (FileInfo f : files) {
                html.append("<tr>")
                    .append("<td>").append(escape(f.getName())).append("</td>")
                    .append("<td>").append(escape(f.getPath())).append("</td>")
                    .append("<td>").append(escape(f.getDuplicatedLines())).append("</td>")
                    .append("</tr>\n");
            }
            html.append("</tbody>\n</table>\n");
        }
    }

    private void appendIssueDetails(StringBuilder html, Project project) {
        html.append("<div class=\"section\">\n")
            .append("<h2>").append(escape(getTextProperty("general.violations_details"))).append("</h2>\n");
        for (String type : typesOfIssue) {
            html.append("<h3>").append(escape(StringUtils.capitalize(type))).append("</h3>\n");
            List<Issue> issues = project.getIssues().stream()
                                        .filter(i -> i.getType().toUpperCase().replace("_", "").replace(" ", "")
                                                      .contains(type.toUpperCase().replace(" ", "").replace("_", "")))
                                        .collect(Collectors.toList());
            if (issues.isEmpty()) {
                html.append("<p><em>").append(escape(getTextProperty("general.no_violations"))).append("</em></p>\n");
            } else {
                html.append("<table>\n<thead><tr>")
                    .append("<th>").append(escape(getTextProperty("genaral.file_name"))).append("</th>")
                    .append("<th>").append(escape(getTextProperty("general.file_path"))).append("</th>")
                    .append("<th>Severity</th>")
                    .append("<th>Line</th>")
                    .append("<th>Message</th>")
                    .append("</tr></thead>\n<tbody>\n");
                for (Issue issue : issues) {
                    html.append("<tr>")
                        .append("<td>").append(escape(issue.getComponent())).append("</td>")
                        .append("<td>").append(escape(issue.getComponentPath())).append("</td>")
                        .append("<td>").append(escape(issue.getSeverity())).append("</td>")
                        .append("<td>").append(issue.getLine() == null || issue.getLine() == 0 ? "N/A"
                                : issue.getLine().toString()).append("</td>")
                        .append("<td>").append(escape(issue.getMessage())).append("</td>")
                        .append("</tr>\n");
                }
                html.append("</tbody>\n</table>\n");
            }
        }
        html.append("</div>\n");
    }

    private void appendFooter(StringBuilder html) {
        html.append("</div>\n</body>\n</html>\n");
    }

    // -------------------------------------------------------------------------
    // Small rendering helpers
    // -------------------------------------------------------------------------

    private void appendMetricCard(StringBuilder html, String value, String label, boolean newCode) {
        String extra = newCode ? " new-code" : "";
        html.append("<div class=\"metric-card").append(extra).append("\">\n")
            .append("<div class=\"value\">").append(escape(value != null ? value : "–")).append("</div>\n")
            .append("<div class=\"label\">").append(escape(label)).append("</div>\n")
            .append("</div>\n");
    }

    private void appendRatingCard(StringBuilder html, String rating, String label) {
        String css = rating == null ? "" : "rating-" + rating.toLowerCase();
        html.append("<div class=\"metric-card\">\n")
            .append("<div class=\"value ").append(css).append("\">").append(escape(rating != null ? rating : "–"))
            .append("</div>\n")
            .append("<div class=\"label\">").append(escape(label)).append("</div>\n")
            .append("</div>\n");
    }

    private void appendMetricRow(StringBuilder html, String label, String value) {
        html.append("<tr><td>").append(escape(label != null ? label : "")).append("</td>")
            .append("<td>").append(escape(value != null ? value : "–")).append("</td></tr>\n");
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    private Period_ getCurrentPeriod(Project project) {
        Optional<Period_> period = this.leakPeriod.getPeriod(project.getMeasures());
        return period.orElseThrow(() -> new IllegalArgumentException("Cannot find the current period"));
    }

    // -------------------------------------------------------------------------
    // Abstract method stubs (not called; getReport() is overridden above)
    // -------------------------------------------------------------------------

    @Override
    protected URL getLogo() {
        return this.logo;
    }

    @Override
    protected void printFrontPage(Document frontPageDocument, PdfWriter frontPageWriter) throws ReportException {
        // Not used for HTML output
    }

    @Override
    protected void printTocTitle(Toc tocDocument) throws DocumentException, IOException {
        // Not used for HTML output
    }

    @Override
    protected void printPdfBody(Document document) throws DocumentException, IOException, ReportException {
        // Not used for HTML output
    }

    @Override
    protected String getProjectKey() {
        return this.projectKey;
    }

    @Override
    public String getProjectVersion() {
        return this.projectVersion;
    }

    @Override
    protected List<String> getSonarLanguage() {
        return this.sonarLanguage;
    }

    @Override
    protected Set<String> getOtherMetrics() {
        return this.otherMetrics;
    }

    @Override
    protected Set<String> getTypesOfIssue() {
        return this.typesOfIssue;
    }

    @Override
    protected LeakPeriodConfiguration getLeakPeriod() {
        return this.leakPeriod;
    }

    @Override
    protected Properties getLangProperties() {
        return this.langProperties;
    }

    @Override
    protected Properties getReportProperties() {
        return this.configProperties;
    }

    @Override
    public String getReportType() {
        return REPORT_TYPE_HTML;
    }
}
