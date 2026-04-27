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
    private static final String METRICS_PREFIX         = "metrics.";
    private static final String TD_OPEN                = "<td>";
    private static final String TD_CLOSE               = "</td>";
    private static final String TH_OPEN                = "<th>";
    private static final String TH_CLOSE               = "</th>";
    private static final String TR_OPEN                = "<tr>";
    private static final String TR_CLOSE               = "</tr>\n";
    private static final String H2_OPEN                = "<h2>";
    private static final String H2_CLOSE               = "</h2>\n";
    private static final String H3_OPEN                = "<h3>";
    private static final String H3_CLOSE               = "</h3>\n";
    private static final String DIV_CLOSE              = "</div>\n";
    private static final String DIV_SECTION_OPEN       = "<div class=\"section\">\n";
    private static final String TABLE_THEAD_TR_OPEN    = "<table>\n<thead><tr>";
    private static final String THEAD_TR_CLOSE_TBODY   = "</tr></thead>\n<tbody>\n";
    private static final String TABLE_TBODY_OPEN       = "<table>\n<tbody>\n";
    private static final String TABLE_CLOSE            = "</tbody>\n</table>\n";
    private static final String H3_CLOSE_METRIC_GRID   = "</h3>\n<div class=\"metric-grid\">\n";
    private static final String H3_CLOSE_TABLE_TBODY   = "</h3>\n<table>\n<tbody>\n";
    private static final String P_EM_OPEN              = "<p><em>";
    private static final String P_EM_CLOSE             = "</em></p>\n";
    private static final String LANG_FILE_NAME         = "genaral.file_name";
    private static final String LANG_FILE_PATH         = "general.file_path";


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
            .append("<div class=\"project-name\">").append(escape(project.getName())).append(DIV_CLOSE)
            .append("<div class=\"meta\">Version: ").append(escape(project.getVersion())).append(DIV_CLOSE);
        if (project.getDescription() != null && !project.getDescription().isEmpty()) {
            html.append("<div class=\"meta\">").append(escape(project.getDescription())).append(DIV_CLOSE);
        }
        html.append("<div class=\"meta\">Generated: ").append(date).append(DIV_CLOSE)
            .append(DIV_CLOSE);
    }

    private void appendQualityProfiles(StringBuilder html, Project project) {
        html.append(DIV_SECTION_OPEN)
            .append(H2_OPEN).append(escape(getTextProperty("general.quality_profile"))).append(H2_CLOSE)
            .append(TABLE_THEAD_TR_OPEN)
            .append(TH_OPEN).append(escape(getTextProperty("general.profile_name"))).append(TH_CLOSE)
            .append(TH_OPEN).append(escape(getTextProperty("general.language"))).append(TH_CLOSE)
            .append(TH_OPEN).append(escape(getTextProperty("general.active_rules_count"))).append(TH_CLOSE)
            .append(THEAD_TR_CLOSE_TBODY);

        List<QualityProfile> profiles = project.getQualityProfiles();
        if (profiles != null && !profiles.isEmpty()) {
            for (QualityProfile qp : profiles) {
                html.append(TR_OPEN)
                    .append(TD_OPEN).append(escape(qp.getName())).append(TD_CLOSE)
                    .append(TD_OPEN).append(escape(qp.getLanguageName())).append(TD_CLOSE)
                    .append(TD_OPEN).append(qp.getActiveRuleCount()).append(TD_CLOSE)
                    .append(TR_CLOSE);
            }
        }
        html.append("</tbody>\n</table>\n</div>\n");
    }

    private void appendQualityGate(StringBuilder html, Project project) {
        String status = project.getProjectStatus().getStatus();
        String badgeCss = getStatusBadgeCss(status);
        String statusLabel = ProjectStatusKeys.getStatusAsString(status);

        html.append(DIV_SECTION_OPEN)
            .append(H2_OPEN).append(escape(getTextProperty("general.quality_gate"))).append(H2_CLOSE)
            .append("<p><span class=\"badge ").append(badgeCss).append("\">").append(escape(statusLabel))
            .append("</span></p>\n");

        if (ProjectStatusKeys.STATUS_ERROR.equals(status)) {
            appendFailedConditions(html, project);
        }
        html.append(DIV_CLOSE);
    }

    private String getStatusBadgeCss(String status) {
        if (ProjectStatusKeys.STATUS_OK.equals(status)) {
            return "badge-ok";
        }
        if (ProjectStatusKeys.STATUS_ERROR.equals(status)) {
            return "badge-error";
        }
        return "badge-none";
    }

    private void appendFailedConditions(StringBuilder html, Project project) {
        Map<Integer, StatusPeriod> mapStatusPeriod = project.getProjectStatus().getStatusPeriods()
                                                            .stream()
                                                            .collect(Collectors.toMap(StatusPeriod::getIndex, Function.identity()));
        StatusPeriod defaultPeriod = mapStatusPeriod.isEmpty() ? null : mapStatusPeriod.values().iterator().next();

        List<Condition> failedConditions = project.getProjectStatus().getConditions().stream()
                                                  .filter(c -> ProjectStatusKeys.STATUS_ERROR.equals(c.getStatus()))
                                                  .collect(Collectors.toList());

        if (!failedConditions.isEmpty()) {
            html.append(TABLE_THEAD_TR_OPEN)
                .append("<th>Metric</th><th>Actual / Threshold</th><th>Status</th>")
                .append(THEAD_TR_CLOSE_TBODY);

            for (Condition cond : failedConditions) {
                appendFailedConditionRow(html, cond, mapStatusPeriod, defaultPeriod);
            }
            html.append(TABLE_CLOSE);
        }
    }

    private void appendFailedConditionRow(StringBuilder html, Condition cond,
                                          Map<Integer, StatusPeriod> mapStatusPeriod,
                                          StatusPeriod defaultPeriod) {
        StatusPeriod condPeriod = cond.getPeriodIndex() != null
                ? mapStatusPeriod.get(cond.getPeriodIndex())
                : defaultPeriod;
        String metricLabel = StringUtils.capitalize(cond.getMetricKey().replace("_", " "));
        if (condPeriod != null) {
            metricLabel += " (since " + condPeriod.getMode().replace("_", " ") + ")";
        }
        html.append(TR_OPEN)
            .append(TD_OPEN).append(escape(metricLabel)).append(TD_CLOSE)
            .append(TD_OPEN).append(escape(cond.getActualValue())).append(" ")
            .append(escape(ProjectStatusKeys.getComparatorAsString(cond.getComparator()))).append(" ")
            .append(escape(cond.getErrorThreshold())).append(TD_CLOSE)
            .append("<td><span class=\"badge badge-error\">Failed</span></td>")
            .append(TR_CLOSE);
    }

    private void appendMetricDashboard(StringBuilder html, Project project) {
        html.append(DIV_SECTION_OPEN)
            .append(H2_OPEN).append(escape(getTextProperty("general.metric_dashboard"))).append(H2_CLOSE);

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

        html.append(DIV_CLOSE);
    }

    private void appendReliabilitySection(StringBuilder html, Project project, Period_ period) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.RELIABILITY.toLowerCase())))
            .append(H3_CLOSE_METRIC_GRID);

        appendMetricCard(html, project.getMeasure(BUGS).getValue(),
                getTextProperty(METRICS_PREFIX + BUGS), false);

        if (project.getMeasures().containsMeasure(NEW_BUGS)) {
            Optional<Period> p = project.getMeasure(NEW_BUGS).getPeriods().stream()
                                        .filter(x -> x.getIndex().equals(period.getIndex())).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty(METRICS_PREFIX + NEW_BUGS), true);
            }
        }

        String rating = Rating.getRating(project.getMeasure(RELIABILITY_RATING).getValue());
        appendRatingCard(html, rating, getTextProperty(METRICS_PREFIX + RELIABILITY_RATING));
        html.append(DIV_CLOSE);

        html.append(TABLE_TBODY_OPEN);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + RELIABILITY_REMEDIATION_EFFORT),
                SonarUtil.getWorkDurConversion(
                        Integer.parseInt(project.getMeasure(RELIABILITY_REMEDIATION_EFFORT).getValue())));
        if (project.getMeasures().containsMeasure(NEW_RELIABILITY_REMEDIATION_EFFORT)) {
            Optional<Period> p = project.getMeasure(NEW_RELIABILITY_REMEDIATION_EFFORT).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricRow(html, getTextProperty(METRICS_PREFIX + NEW_RELIABILITY_REMEDIATION_EFFORT),
                        SonarUtil.getWorkDurConversion(Integer.parseInt(p.get().getValue())));
            }
        }
        html.append(TABLE_CLOSE);
    }

    private void appendSecuritySection(StringBuilder html, Project project, Period_ period) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.SECURITY.toLowerCase())))
            .append(H3_CLOSE_METRIC_GRID);

        appendMetricCard(html, project.getMeasure(VULNERABILITIES).getValue(),
                getTextProperty(METRICS_PREFIX + VULNERABILITIES), false);

        if (project.getMeasures().containsMeasure(NEW_VULNERABILITIES)) {
            Optional<Period> p = project.getMeasure(NEW_VULNERABILITIES).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty(METRICS_PREFIX + NEW_VULNERABILITIES), true);
            }
        }

        String rating = Rating.getRating(project.getMeasure(SECURITY_RATING).getValue());
        appendRatingCard(html, rating, getTextProperty(METRICS_PREFIX + SECURITY_RATING));
        html.append(DIV_CLOSE);

        html.append(TABLE_TBODY_OPEN);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + SECURITY_REMEDIATION_EFFORT),
                SonarUtil.getWorkDurConversion(
                        Integer.parseInt(project.getMeasure(SECURITY_REMEDIATION_EFFORT).getValue())));
        if (project.getMeasures().containsMeasure(NEW_SECURITY_REMEDIATION_EFFORT)) {
            Optional<Period> p = project.getMeasure(NEW_SECURITY_REMEDIATION_EFFORT).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricRow(html, getTextProperty(METRICS_PREFIX + NEW_SECURITY_REMEDIATION_EFFORT),
                        SonarUtil.getWorkDurConversion(Integer.parseInt(p.get().getValue())));
            }
        }
        html.append(TABLE_CLOSE);
    }

    private void appendMaintainabilitySection(StringBuilder html, Project project, Period_ period) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.MAINTAINABILITY.toLowerCase())))
            .append(H3_CLOSE_METRIC_GRID);

        appendMetricCard(html, project.getMeasure(CODE_SMELLS).getValue(),
                getTextProperty(METRICS_PREFIX + CODE_SMELLS), false);

        if (project.getMeasures().containsMeasure(NEW_CODE_SMELLS)) {
            Optional<Period> p = project.getMeasure(NEW_CODE_SMELLS).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty(METRICS_PREFIX + NEW_CODE_SMELLS), true);
            }
        }

        String rating = Rating.getRating(project.getMeasure(SQALE_RATING).getValue());
        appendRatingCard(html, rating, getTextProperty(METRICS_PREFIX + SQALE_RATING));
        html.append(DIV_CLOSE);

        html.append(TABLE_TBODY_OPEN);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + SQALE_INDEX),
                SonarUtil.getWorkDurConversion(Integer.parseInt(project.getMeasure(SQALE_INDEX).getValue())));
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + SQALE_DEBT_RATIO),
                project.getMeasure(SQALE_DEBT_RATIO).getValue() + "%");
        if (project.getMeasures().containsMeasure(NEW_TECHNICAL_DEBT)) {
            Optional<Period> p = project.getMeasure(NEW_TECHNICAL_DEBT).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricRow(html, getTextProperty(METRICS_PREFIX + NEW_TECHNICAL_DEBT),
                        SonarUtil.getWorkDurConversion(Integer.parseInt(p.get().getValue())));
            }
        }
        if (project.getMeasures().containsMeasure(EFFORT_TO_REACH_MAINTAINABILITY_RATING_A)) {
            appendMetricRow(html, getTextProperty(METRICS_PREFIX + EFFORT_TO_REACH_MAINTAINABILITY_RATING_A),
                    SonarUtil.getWorkDurConversion(
                            Integer.parseInt(project.getMeasure(EFFORT_TO_REACH_MAINTAINABILITY_RATING_A).getValue())));
        }
        html.append(TABLE_CLOSE);
    }

    private void appendCoverageSection(StringBuilder html, Project project) {
        if (!project.getMeasures().containsMeasure(MetricKeys.COVERAGE)) {
            return;
        }
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.COVERAGE.toLowerCase())))
            .append(H3_CLOSE_METRIC_GRID);
        appendMetricCard(html, project.getMeasure(MetricKeys.COVERAGE).getValue() + "%",
                getTextProperty(METRICS_PREFIX + MetricKeys.COVERAGE), false);
        html.append(DIV_CLOSE);

        html.append(TABLE_TBODY_OPEN);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + MetricKeys.LINE_COVERAGE),
                project.getMeasure(MetricKeys.LINE_COVERAGE).getValue() + "%");
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + MetricKeys.BRANCH_COVERAGE),
                project.getMeasure(MetricKeys.BRANCH_COVERAGE).getValue() + "%");
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + MetricKeys.UNCOVERED_LINES),
                project.getMeasure(MetricKeys.UNCOVERED_LINES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + MetricKeys.UNCOVERED_CONDITIONS),
                project.getMeasure(MetricKeys.UNCOVERED_CONDITIONS).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + MetricKeys.LINES_TO_COVER),
                project.getMeasure(MetricKeys.LINES_TO_COVER).getValue());
        html.append(TABLE_CLOSE);
    }

    private void appendDuplicationsSection(StringBuilder html, Project project) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.DUPLICATIONS.toLowerCase())))
            .append(H3_CLOSE_METRIC_GRID);
        appendMetricCard(html, project.getMeasure(DUPLICATED_LINES_DENSITY).getValue() + "%",
                getTextProperty(METRICS_PREFIX + DUPLICATED_LINES_DENSITY), false);
        html.append(DIV_CLOSE);

        html.append(TABLE_TBODY_OPEN);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + DUPLICATED_LINES),
                project.getMeasure(DUPLICATED_LINES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + DUPLICATED_BLOCKS),
                project.getMeasure(DUPLICATED_BLOCKS).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + DUPLICATED_FILES),
                project.getMeasure(DUPLICATED_FILES).getValue());
        html.append(TABLE_CLOSE);
    }

    private void appendSizeSection(StringBuilder html, Project project) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.SIZE.toLowerCase())))
            .append(H3_CLOSE_TABLE_TBODY);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + NCLOC), project.getMeasure(NCLOC).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + LINES), project.getMeasure(LINES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + STATEMENTS), project.getMeasure(STATEMENTS).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + FUNCTIONS), project.getMeasure(FUNCTIONS).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + CLASSES), project.getMeasure(CLASSES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + FILES), project.getMeasure(FILES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + DIRECTORIES), project.getMeasure(DIRECTORIES).getValue());
        html.append(TABLE_CLOSE);
    }

    private void appendComplexitySection(StringBuilder html, Project project) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.COMPLEXITY.toLowerCase())))
            .append(H3_CLOSE_TABLE_TBODY);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + CLASS_COMPLEXITY),
                project.getMeasure(CLASS_COMPLEXITY).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + FUNCTION_COMPLEXITY),
                project.getMeasure(FUNCTION_COMPLEXITY).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + FILE_COMPLEXITY),
                project.getMeasure(FILE_COMPLEXITY).getValue());
        html.append(TABLE_CLOSE);
    }

    private void appendDocumentationSection(StringBuilder html, Project project) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.DOCUMENTATION.toLowerCase())))
            .append(H3_CLOSE_TABLE_TBODY);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + COMMENT_LINES),
                project.getMeasure(COMMENT_LINES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + COMMENT_LINES_DENSITY),
                project.getMeasure(COMMENT_LINES_DENSITY).getValue() + "%");
        html.append(TABLE_CLOSE);
    }

    private void appendIssuesSection(StringBuilder html, Project project, Period_ period) {
        html.append(H3_OPEN).append(escape(getTextProperty(METRICS_PREFIX + MetricDomains.ISSUES.toLowerCase())))
            .append(H3_CLOSE_METRIC_GRID);
        appendMetricCard(html, project.getMeasure(VIOLATIONS).getValue(),
                getTextProperty(METRICS_PREFIX + VIOLATIONS), false);
        if (project.getMeasures().containsMeasure(NEW_VIOLATIONS)) {
            Optional<Period> p = project.getMeasure(NEW_VIOLATIONS).getPeriods().stream()
                                        .filter(x -> x.getIndex() == period.getIndex()).findFirst();
            if (p.isPresent()) {
                appendMetricCard(html, p.get().getValue(), getTextProperty(METRICS_PREFIX + NEW_VIOLATIONS), true);
            }
        }
        html.append(DIV_CLOSE);

        html.append(TABLE_TBODY_OPEN);
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + OPEN_ISSUES),
                project.getMeasure(OPEN_ISSUES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + REOPENED_ISSUES),
                project.getMeasure(REOPENED_ISSUES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + CONFIRMED_ISSUES),
                project.getMeasure(CONFIRMED_ISSUES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + FALSE_POSITIVE_ISSUES),
                project.getMeasure(FALSE_POSITIVE_ISSUES).getValue());
        appendMetricRow(html, getTextProperty(METRICS_PREFIX + WONT_FIX_ISSUES),
                project.getMeasure(WONT_FIX_ISSUES).getValue());
        html.append(TABLE_CLOSE);
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
        html.append(TABLE_CLOSE);
    }

    private void appendViolationsAnalysis(StringBuilder html, Project project) {
        html.append(DIV_SECTION_OPEN)
            .append(H2_OPEN).append(escape(getTextProperty("general.violations_analysis"))).append(H2_CLOSE);

        appendMostViolatedRules(html, project);
        appendMostViolatedFiles(html, project);
        appendMostComplexFiles(html, project);
        appendMostDuplicatedFiles(html, project);

        html.append(DIV_CLOSE);
    }

    private void appendMostViolatedRules(StringBuilder html, Project project) {
        html.append(H3_OPEN).append(escape(getTextProperty("general.most_violated_rules"))).append(H3_CLOSE);
        List<Rule> rules = project.getMostViolatedRules();
        String[] priorities = Priority.getPrioritiesArray();
        for (String priority : priorities) {
            List<Rule> filtered = rules.stream()
                                       .filter(r -> r.getSeverity().equals(Priority.getPriority(priority)))
                                       .collect(Collectors.toList());
            html.append("<p><strong>Severity: ").append(escape(Priority.getPriority(priority))).append("</strong></p>\n");
            if (filtered.isEmpty()) {
                html.append(P_EM_OPEN).append(escape(getTextProperty("general.no_violated_rules")))
                    .append(" of Severity ").append(escape(Priority.getPriority(priority))).append(P_EM_CLOSE);
            } else {
                html.append(TABLE_THEAD_TR_OPEN)
                    .append(TH_OPEN).append(escape(getTextProperty("genaral.rule_name"))).append(TH_CLOSE)
                    .append(TH_OPEN).append(escape(getTextProperty("general.language_name"))).append(TH_CLOSE)
                    .append(TH_OPEN).append(escape(getTextProperty("general.rule_count"))).append(TH_CLOSE)
                    .append(THEAD_TR_CLOSE_TBODY);
                for (Rule rule : filtered) {
                    html.append(TR_OPEN)
                        .append(TD_OPEN).append(escape(rule.getName())).append(TD_CLOSE)
                        .append(TD_OPEN).append(escape(rule.getLanguageName())).append(TD_CLOSE)
                        .append(TD_OPEN).append(rule.getCount()).append(TD_CLOSE)
                        .append(TR_CLOSE);
                }
                html.append(TABLE_CLOSE);
            }
        }
    }

    private void appendMostViolatedFiles(StringBuilder html, Project project) {
        List<FileInfo> files = project.getMostViolatedFiles().stream()
                                      .filter(f -> f.isContentSet(FileInfo.VIOLATIONS_CONTENT))
                                      .collect(Collectors.toList());
        html.append(H3_OPEN).append(escape(getTextProperty("general.most_violated_files"))).append(H3_CLOSE);
        if (files.isEmpty()) {
            html.append(P_EM_OPEN).append(escape(getTextProperty("general.no_violated_files"))).append(P_EM_CLOSE);
        } else {
            html.append(TABLE_THEAD_TR_OPEN)
                .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_NAME))).append(TH_CLOSE)
                .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_PATH))).append(TH_CLOSE)
                .append(TH_OPEN).append(escape(getTextProperty("general.file_violations"))).append(TH_CLOSE)
                .append(THEAD_TR_CLOSE_TBODY);
            for (FileInfo f : files) {
                html.append(TR_OPEN)
                    .append(TD_OPEN).append(escape(f.getName())).append(TD_CLOSE)
                    .append(TD_OPEN).append(escape(f.getPath())).append(TD_CLOSE)
                    .append(TD_OPEN).append(escape(f.getViolations())).append(TD_CLOSE)
                    .append(TR_CLOSE);
            }
            html.append(TABLE_CLOSE);
        }
    }

    private void appendMostComplexFiles(StringBuilder html, Project project) {
        List<FileInfo> files = project.getMostComplexFiles().stream()
                                      .filter(f -> f.isContentSet(FileInfo.CCN_CONTENT))
                                      .collect(Collectors.toList());
        html.append(H3_OPEN).append(escape(getTextProperty("general.most_complex_files"))).append(H3_CLOSE);
        if (files.isEmpty()) {
            html.append(P_EM_OPEN).append(escape(getTextProperty("general.no_complex_files"))).append(P_EM_CLOSE);
        } else {
            html.append(TABLE_THEAD_TR_OPEN)
                .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_NAME))).append(TH_CLOSE)
                .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_PATH))).append(TH_CLOSE)
                .append(TH_OPEN).append(escape(getTextProperty("general.file_complexity"))).append(TH_CLOSE)
                .append(THEAD_TR_CLOSE_TBODY);
            for (FileInfo f : files) {
                html.append(TR_OPEN)
                    .append(TD_OPEN).append(escape(f.getName())).append(TD_CLOSE)
                    .append(TD_OPEN).append(escape(f.getPath())).append(TD_CLOSE)
                    .append(TD_OPEN).append(escape(f.getComplexity())).append(TD_CLOSE)
                    .append(TR_CLOSE);
            }
            html.append(TABLE_CLOSE);
        }
    }

    private void appendMostDuplicatedFiles(StringBuilder html, Project project) {
        List<FileInfo> files = project.getMostDuplicatedFiles().stream()
                                      .filter(f -> f.isContentSet(FileInfo.DUPLICATIONS_CONTENT))
                                      .collect(Collectors.toList());
        html.append(H3_OPEN).append(escape(getTextProperty("general.most_duplicated_files"))).append(H3_CLOSE);
        if (files.isEmpty()) {
            html.append(P_EM_OPEN).append(escape(getTextProperty("general.no_duplicated_files"))).append(P_EM_CLOSE);
        } else {
            html.append(TABLE_THEAD_TR_OPEN)
                .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_NAME))).append(TH_CLOSE)
                .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_PATH))).append(TH_CLOSE)
                .append(TH_OPEN).append(escape(getTextProperty("general.file_duplicated_lines"))).append(TH_CLOSE)
                .append(THEAD_TR_CLOSE_TBODY);
            for (FileInfo f : files) {
                html.append(TR_OPEN)
                    .append(TD_OPEN).append(escape(f.getName())).append(TD_CLOSE)
                    .append(TD_OPEN).append(escape(f.getPath())).append(TD_CLOSE)
                    .append(TD_OPEN).append(escape(f.getDuplicatedLines())).append(TD_CLOSE)
                    .append(TR_CLOSE);
            }
            html.append(TABLE_CLOSE);
        }
    }

    private void appendIssueDetails(StringBuilder html, Project project) {
        html.append(DIV_SECTION_OPEN)
            .append(H2_OPEN).append(escape(getTextProperty("general.violations_details"))).append(H2_CLOSE);
        for (String type : typesOfIssue) {
            html.append(H3_OPEN).append(escape(StringUtils.capitalize(type))).append(H3_CLOSE);
            List<Issue> issues = project.getIssues().stream()
                                        .filter(i -> i.getType().toUpperCase().replace("_", "").replace(" ", "")
                                                      .contains(type.toUpperCase().replace(" ", "").replace("_", "")))
                                        .collect(Collectors.toList());
            if (issues.isEmpty()) {
                html.append(P_EM_OPEN).append(escape(getTextProperty("general.no_violations"))).append(P_EM_CLOSE);
            } else {
                html.append(TABLE_THEAD_TR_OPEN)
                    .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_NAME))).append(TH_CLOSE)
                    .append(TH_OPEN).append(escape(getTextProperty(LANG_FILE_PATH))).append(TH_CLOSE)
                    .append("<th>Severity</th>")
                    .append("<th>Line</th>")
                    .append("<th>Message</th>")
                    .append(THEAD_TR_CLOSE_TBODY);
                for (Issue issue : issues) {
                    html.append(TR_OPEN)
                        .append(TD_OPEN).append(escape(issue.getComponent())).append(TD_CLOSE)
                        .append(TD_OPEN).append(escape(issue.getComponentPath())).append(TD_CLOSE)
                        .append(TD_OPEN).append(escape(issue.getSeverity())).append(TD_CLOSE)
                        .append(TD_OPEN).append(issue.getLine() == null || issue.getLine() == 0 ? "N/A"
                                : issue.getLine().toString()).append(TD_CLOSE)
                        .append(TD_OPEN).append(escape(issue.getMessage())).append(TD_CLOSE)
                        .append(TR_CLOSE);
                }
                html.append(TABLE_CLOSE);
            }
        }
        html.append(DIV_CLOSE);
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
            .append("<div class=\"value\">").append(escape(value != null ? value : "–")).append(DIV_CLOSE)
            .append("<div class=\"label\">").append(escape(label)).append(DIV_CLOSE)
            .append(DIV_CLOSE);
    }

    private void appendRatingCard(StringBuilder html, String rating, String label) {
        String css = rating == null ? "" : "rating-" + rating.toLowerCase();
        html.append("<div class=\"metric-card\">\n")
            .append("<div class=\"value ").append(css).append("\">").append(escape(rating != null ? rating : "–"))
            .append(DIV_CLOSE)
            .append("<div class=\"label\">").append(escape(label)).append(DIV_CLOSE)
            .append(DIV_CLOSE);
    }

    private void appendMetricRow(StringBuilder html, String label, String value) {
        html.append("<tr><td>").append(escape(label != null ? label : "")).append(TD_CLOSE)
            .append(TD_OPEN).append(escape(value != null ? value : "–")).append("</td></tr>\n");
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
