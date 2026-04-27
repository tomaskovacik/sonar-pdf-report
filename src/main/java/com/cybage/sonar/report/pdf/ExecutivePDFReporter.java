package com.cybage.sonar.report.pdf;

import static com.cybage.sonar.report.pdf.util.MetricDomains.DOCUMENTATION;
import static com.cybage.sonar.report.pdf.util.MetricDomains.DUPLICATIONS;
import static com.cybage.sonar.report.pdf.util.MetricDomains.ISSUES;
import static com.cybage.sonar.report.pdf.util.MetricDomains.MAINTAINABILITY;
import static com.cybage.sonar.report.pdf.util.MetricDomains.RELIABILITY;
import static com.cybage.sonar.report.pdf.util.MetricDomains.SECURITY;
import static com.cybage.sonar.report.pdf.util.MetricDomains.SIZE;
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
import static com.cybage.sonar.report.pdf.util.MetricKeys.PROFILE;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cybage.sonar.report.pdf.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybage.sonar.report.pdf.design.CustomCellTitle;
import com.cybage.sonar.report.pdf.design.CustomCellValue;
import com.cybage.sonar.report.pdf.design.CustomMainTable;
import com.cybage.sonar.report.pdf.design.CustomTable;
import com.cybage.sonar.report.pdf.entity.exception.ReportException;
import com.cybage.sonar.report.pdf.util.Credentials;
import com.cybage.sonar.report.pdf.util.MetricDomains;
import com.cybage.sonar.report.pdf.util.MetricKeys;
import com.cybage.sonar.report.pdf.util.ProjectStatusKeys;
import com.cybage.sonar.report.pdf.util.Rating;
import com.cybage.sonar.report.pdf.util.SonarUtil;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.ChapterAutoNumber;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ExecutivePDFReporter extends PDFReporter {

    public static final  String                  LOGO_PROPS      = "front.page.logo";
    private static final Logger                  LOGGER          = LoggerFactory.getLogger(ExecutivePDFReporter.class);
    private static final String                  REPORT_TYPE_PDF = "pdf";
    private static final String METRICS_PREFIX = "metrics.";
    private static final String LANG_FILE_NAME = "general.file_name";
    private static final String LANG_FILE_PATH = "general.file_path";

    private final        URL                     logo;
    private final        String                  projectKey;
    private final        String                  projectVersion;
    private final        List<String>            sonarLanguage;
    private final        Set<String>             typesOfIssue;
    private final        LeakPeriodConfiguration leakPeriod;
    private final        Properties              configProperties;
    private final        Properties              langProperties;
    private              Set<String>             otherMetrics;

    public ExecutivePDFReporter(final Credentials credentials,
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
        this.logo = logo;
        this.projectKey = projectKey;
        this.projectVersion = projectVersion;
        this.sonarLanguage = sonarLanguage;
        this.otherMetrics = otherMetrics;
        this.typesOfIssue = typesOfIssue;
        this.leakPeriod = leakPeriod;
        this.configProperties = configProperties;
        this.langProperties = langProperties;
    }

    @Override
    protected void printPdfBody(final Document document) {
        try {
            Project project = super.getProject();
            // Chapter 1: Report Overview (Parent project)
            ChapterAutoNumber chapter1 = new ChapterAutoNumber(new Paragraph(project.getName(), Style.CHAPTER_FONT));
            chapter1.add(new Paragraph(getTextProperty("main.text.misc.overview"), Style.NORMAL_FONT));

            chapter1.add(new Paragraph(" ", new Font(FontFamily.COURIER, 8)));
            Section section11 = chapter1
                    .addSection(new Paragraph(getTextProperty("general.quality_profile"), Style.TITLE_FONT));
            printQualityProfileInfo(project, section11);

            chapter1.add(new Paragraph(" ", new Font(FontFamily.COURIER, 8)));
            Section section12 = chapter1
                    .addSection(new Paragraph(getTextProperty("general.quality_gate"), Style.TITLE_FONT));
            printQualityGateInfo(project, section12);

            chapter1.add(new Paragraph(" ", new Font(FontFamily.COURIER, 8)));
            Section section13 = chapter1
                    .addSection(new Paragraph(getTextProperty("general.metric_dashboard"), Style.TITLE_FONT));
            printDashboard(project, section13);

            chapter1.add(new Paragraph(" ", new Font(FontFamily.COURIER, 8)));
            Section section14 = chapter1
                    .addSection(new Paragraph(getTextProperty("general.violations_analysis"), Style.TITLE_FONT));
            printMostViolatedRules(project, section14);
            printMostViolatedFiles(project, section14);
            printMostComplexFiles(project, section14);
            printMostDuplicatedFiles(project, section14);

            if (this.typesOfIssue.size() > 0) {
                chapter1.add(new Paragraph(" ", new Font(FontFamily.COURIER, 8)));
                Section section15 = chapter1
                        .addSection(new Paragraph(getTextProperty("general.violations_details"), Style.TITLE_FONT));
                printIssuesDetails(project, section15);
            }

            document.add(chapter1);

            /*
             * Iterator<Project> it = project.getSubprojects().iterator(); while
             * (it.hasNext()) { Project subproject = it.next();
             * ChapterAutoNumber chapterN = new ChapterAutoNumber(new Paragraph(
             * subproject.getName(), Style.CHAPTER_FONT));
             *
             * Section sectionN1 = chapterN.addSection(new Paragraph(
             * getTextProperty("general.report_overview"), Style.TITLE_FONT));
             * printDashboard(subproject, sectionN1);
             *
             * Section sectionN2 = chapterN.addSection(new Paragraph(
             * getTextProperty("general.violations_analysis"),
             * Style.TITLE_FONT)); printMostViolatedRules(subproject,
             * sectionN2); printMostViolatedFiles(subproject, sectionN2);
             * printMostComplexFiles(subproject, sectionN2);
             * printMostDuplicatedFiles(subproject, sectionN2);
             * document.add(chapterN); }
             */
        } catch (Exception e) {
            LOGGER.error("Error in printPdfBody..", e);
        }
    }

    @Override
    protected void printTocTitle(final Toc tocDocument) throws DocumentException {
        Paragraph tocTitle = new Paragraph(super.getTextProperty("main.table.of.contents"), Style.TOC_TITLE_FONT);
        tocTitle.setAlignment(Element.ALIGN_CENTER);
        tocDocument.getTocDocument().add(tocTitle);
        tocDocument.getTocDocument().add(Chunk.NEWLINE);
    }

    @Override
    protected URL getLogo() {
        return this.logo;
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
    protected void printFrontPage(final Document frontPageDocument, final PdfWriter frontPageWriter)
            throws ReportException {
        try {
            Rectangle pageSize = frontPageDocument.getPageSize();

            // ---- Dark banner spanning the upper third of the page ----
            float bannerHeight = pageSize.getHeight() * 0.42f;
            PdfPTable banner = new PdfPTable(1);
            banner.setTotalWidth(pageSize.getWidth());
            PdfPCell bannerCell = new PdfPCell();
            bannerCell.setBackgroundColor(Style.COLOR_DARK_NAVY);
            bannerCell.setBorder(Rectangle.NO_BORDER);
            bannerCell.setFixedHeight(bannerHeight);
            banner.addCell(bannerCell);
            banner.writeSelectedRows(0, -1, 0, pageSize.getHeight(), frontPageWriter.getDirectContent());

            // ---- Accent stripe below the banner ----
            PdfPTable stripe = new PdfPTable(1);
            stripe.setTotalWidth(pageSize.getWidth());
            PdfPCell stripeCell = new PdfPCell();
            stripeCell.setBackgroundColor(Style.COLOR_ACCENT_BLUE);
            stripeCell.setBorder(Rectangle.NO_BORDER);
            stripeCell.setFixedHeight(6f);
            stripe.addCell(stripeCell);
            stripe.writeSelectedRows(0, -1, 0, pageSize.getHeight() - bannerHeight,
                    frontPageWriter.getDirectContent());

            // ---- Logo inside the banner ----
            URL   largeLogo = loadLargeLogo();
            Image logoImage = Image.getInstance(largeLogo);
            logoImage.scaleToFit(220, 110);
            logoImage.setAbsolutePosition(
                    (pageSize.getWidth() - logoImage.getScaledWidth()) / 2f,
                    pageSize.getHeight() - bannerHeight * 0.45f);
            frontPageDocument.add(logoImage);

            // ---- Text block inside the banner ----
            String projectRow    = super.getProject().getName();
            String versionRow    = "Version " + super.getProject().getVersion();
            String descriptionRow = super.getProject().getDescription();
            String profileRow    = super.getProject().getMeasure(PROFILE).getValue();
            SimpleDateFormat df  = new SimpleDateFormat("yyyy-MM-dd");
            String dateRow       = df.format(new Date());

            PdfPTable title = new PdfPTable(1);
            title.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            title.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            title.getDefaultCell().setBackgroundColor(Style.COLOR_DARK_NAVY);
            title.getDefaultCell().setPaddingBottom(6f);

            title.addCell(new Phrase(projectRow, Style.FRONTPAGE_FONT_1));
            title.addCell(new Phrase(versionRow, Style.FRONTPAGE_FONT_2));
            if (descriptionRow != null && !descriptionRow.isEmpty()) {
                title.addCell(new Phrase(descriptionRow, Style.FRONTPAGE_FONT_2));
            }
            title.addCell(new Phrase(profileRow, Style.FRONTPAGE_FONT_3));
            title.addCell(new Phrase(dateRow, Style.FRONTPAGE_FONT_3));

            title.setTotalWidth(pageSize.getWidth() - frontPageDocument.leftMargin() - frontPageDocument.rightMargin());
            title.writeSelectedRows(0, -1, frontPageDocument.leftMargin(),
                    pageSize.getHeight() - bannerHeight + 160f,
                    frontPageWriter.getDirectContent());

        } catch (IOException | DocumentException e) {
            LOGGER.error("Can not generate front page", e);
        }
    }

    private URL loadLargeLogo() throws MalformedURLException {
        URL largeLogo;
        if (super.getConfigProperty(LOGO_PROPS).startsWith("http://")) {
            largeLogo = new URL(super.getConfigProperty(LOGO_PROPS));
        } else {
            largeLogo = this.getClass().getClassLoader().getResource(super.getConfigProperty(LOGO_PROPS));
        }
        return largeLogo;
    }

    @Override
    protected Properties getReportProperties() {
        return configProperties;
    }

    @Override
    protected Properties getLangProperties() {
        return langProperties;
    }

    @Override
    public String getReportType() {
        return REPORT_TYPE_PDF;
    }

    /**
     * Creates a table column-header cell with dark background and white bold text.
     */
    private CustomCellTitle tableHeader(final String text) {
        CustomCellTitle cell = new CustomCellTitle(new Phrase(text, Style.TABLE_HEADER_FONT));
        cell.setBackgroundColor(Style.TABLE_HEADER_BACKGROUND_COLOR);
        cell.setBorderColor(Style.TABLE_HEADER_BACKGROUND_COLOR);
        return cell;
    }

    protected void printQualityProfileInfo(final Project project, final Section section) throws DocumentException {

        // Quality Profile Information
        Paragraph qualityProfileTitle = new Paragraph(getTextProperty("general.profiles"), Style.UNDERLINED_FONT);

        // Quality Profiles Table
        CustomTable tableQualityProfiles = new CustomTable(3);
        tableQualityProfiles.setWidths(new int[]{5, 3, 3});

        // Quality Profiles Table Header
        tableQualityProfiles.addCell(tableHeader(getTextProperty("general.profile_name")));
        tableQualityProfiles.addCell(tableHeader(getTextProperty("general.language")));
        tableQualityProfiles.addCell(tableHeader(getTextProperty("general.active_rules_count")));

        // Quality Profiles List
        if (project.getLanguages() != null) {
            for (String language : project.getLanguages()) {
                CustomCellTitle profileName = new CustomCellTitle(new Phrase(
                        project.getQualityProfileByLanguage(language).get().getName(), Style.DASHBOARD_DATA_FONT_2));
                tableQualityProfiles.addCell(profileName);

                CustomCellTitle languageName = new CustomCellTitle(
                        new Phrase(project.getQualityProfileByLanguage(language).get().getLanguageName(),
                                Style.DASHBOARD_DATA_FONT_2));
                tableQualityProfiles.addCell(languageName);

                CustomCellValue rulesCount = new CustomCellValue(
                        new Phrase(project.getQualityProfileByLanguage(language).get().getActiveRuleCount().toString(),
                                Style.DASHBOARD_DATA_FONT_2));
                tableQualityProfiles.addCell(rulesCount);
            }
        } else {
            for (QualityProfile qualityProfile : project.getQualityProfiles()) {
                CustomCellTitle profileName = new CustomCellTitle(
                        new Phrase(qualityProfile.getName(), Style.DASHBOARD_DATA_FONT_2));
                tableQualityProfiles.addCell(profileName);

                CustomCellTitle languageName = new CustomCellTitle(
                        new Phrase(qualityProfile.getLanguageName(), Style.DASHBOARD_DATA_FONT_2));
                tableQualityProfiles.addCell(languageName);

                CustomCellValue rulesCount = new CustomCellValue(
                        new Phrase(qualityProfile.getActiveRuleCount().toString(), Style.DASHBOARD_DATA_FONT_2));
                tableQualityProfiles.addCell(rulesCount);
            }
        }
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(new Paragraph(qualityProfileTitle));
        section.add(new Paragraph(" "));
        section.add(tableQualityProfiles);

    }

    protected void printQualityGateInfo(final Project project, final Section section) throws DocumentException {

        // Quality Gate Information
        Paragraph qualityGateTitle = new Paragraph(getTextProperty("general.project_status"), Style.UNDERLINED_FONT);

        CustomTable tableQualityGatesStatus = new CustomTable(2);
        tableQualityGatesStatus.setWidths(new int[]{1, 1});

        CustomCellTitle projectStatusTitle = new CustomCellTitle(
                new Phrase(getTextProperty("general.project_status"), Style.QUALITY_GATE_TITLE_FONT));
        projectStatusTitle.setBackgroundColor(Style.COLOR_ROW_ALT);
        tableQualityGatesStatus.addCell(projectStatusTitle);

        final String status = project.getProjectStatus().getStatus();
        if (status.equals(ProjectStatusKeys.STATUS_OK)) {
            CustomCellValue projectStatus = new CustomCellValue(
                    new Phrase(ProjectStatusKeys.getStatusAsString(status), Style.QUALITY_GATE_PASSED_FONT));
            projectStatus.setBackgroundColor(Style.QUALITY_GATE_PASSED_COLOR);
            tableQualityGatesStatus.addCell(projectStatus);
        } else if (status.equals(ProjectStatusKeys.STATUS_ERROR)) {
            CustomCellValue projectStatus = new CustomCellValue(
                    new Phrase(ProjectStatusKeys.getStatusAsString(status), Style.QUALITY_GATE_FAILED_FONT));
            projectStatus.setBackgroundColor(Style.QUALITY_GATE_FAILED_COLOR);
            tableQualityGatesStatus.addCell(projectStatus);
        }

        // Quality Gates Table
        CustomTable tableQualityGates = new CustomTable(3);
        tableQualityGates.setWidths(new int[]{15, 3, 2});

        if (status.equals(ProjectStatusKeys.STATUS_ERROR)) {
            addFailedConditionRows(project, tableQualityGates);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(new Paragraph(qualityGateTitle));
        section.add(new Paragraph(" "));
        section.add(tableQualityGatesStatus);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableQualityGates);
    }

    /**
     * Adds one table row per failed quality-gate condition into {@code table}.
     * Only called when the overall project status is ERROR.
     */
    private void addFailedConditionRows(final Project project, final CustomTable table) {
        // In SonarQube 10.x+, there is at most one new-code period (index 1).
        Map<Integer, StatusPeriod> mapStatusPeriod = project.getProjectStatus().getStatusPeriods()
                                                            .stream()
                                                            .collect(Collectors.toMap(StatusPeriod::getIndex, Function.identity()));
        // Fallback: first period available (used when condition has no period index)
        StatusPeriod defaultPeriod = mapStatusPeriod.isEmpty() ? null : mapStatusPeriod.values().iterator().next();

        for (Condition condition : project.getProjectStatus().getConditions()) {
            if (condition.getStatus().equals(ProjectStatusKeys.STATUS_ERROR)) {
                addFailedConditionRow(table, condition, mapStatusPeriod, defaultPeriod);
            }
        }
    }

    private void addFailedConditionRow(final CustomTable table,
                                       final Condition condition,
                                       final Map<Integer, StatusPeriod> mapStatusPeriod,
                                       final StatusPeriod defaultPeriod) {
        // In SonarQube 10.x+, conditions no longer carry a periodIndex.
        // Fall back to the new-code period info when available.
        StatusPeriod condPeriod = condition.getPeriodIndex() != null
                ? mapStatusPeriod.get(condition.getPeriodIndex())
                : defaultPeriod;
        String metricLabel = StringUtils.capitalize(condition.getMetricKey().replace("_", " "));
        if (condPeriod != null) {
            metricLabel += " (since " + condPeriod.getMode().replace("_", " ") + ")";
        }
        CustomCellTitle metricName = new CustomCellTitle(new Phrase(metricLabel, Style.DASHBOARD_TITLE_FONT));
        table.addCell(metricName);

        CustomCellTitle metricValue = new CustomCellTitle(new Phrase(condition.getActualValue() + " "
                + ProjectStatusKeys.getComparatorAsString(condition.getComparator()) + " "
                + condition.getErrorThreshold(), Style.DASHBOARD_DATA_FONT_2));
        table.addCell(metricValue);

        CustomCellValue metricStatus = new CustomCellValue(
                new Phrase(ProjectStatusKeys.getStatusAsString(condition.getStatus()),
                        Style.QUALITY_GATE_FAILED_FONT_2));
        metricStatus.setBackgroundColor(Style.QUALITY_GATE_FAILED_COLOR);
        table.addCell(metricStatus);
    }

    protected void printDashboard(final Project project, final Section section) throws DocumentException {
        section.add(new Phrase("", new Font(FontFamily.COURIER, 6)));
        final LeakPeriodConfiguration leakPeriod = this.getLeakPeriod();
        LOGGER.info("Leak period {}", leakPeriod);
        LOGGER.info("Periods {}", project.getMeasures().getPeriods());

        Period_ period       = getCurrentPeriod(project);
        String  textProperty = getTextProperty("general.period." + period.getMode());
        section.add(new Phrase(MessageFormat.format("Leak Period : {0}", textProperty), Style.NORMAL_HIGHLIGHTED_FONT));
        printReliabilityBoard(project, section);
        printSecurityBoard(project, section);
        printMaintainabilityBoard(project, section);
        printCoverageBoard(project, section);
        printDuplicationsBoard(project, section);
        printSizeBoard(project, section);
        printComplexityBoard(project, section);
        printDocumentationBoard(project, section);
        printIssuesBoard(project, section);

        if (otherMetrics != null) {
            this.otherMetrics.removeAll(MetricKeys.getAllMetricKeys());
            this.otherMetrics = filterOtherMetrics(project);

            if (!this.otherMetrics.isEmpty()) {
                printOtherMetricBoard(project, section);
            }
        }
    }

    private Set<String> filterOtherMetrics(final Project project) {
        return this.otherMetrics.stream()
                                .filter(om -> project.getMeasures().containsMeasure(om)
                                        && !MetricDomains.getDomains().contains(project.getMeasure(om).getDomain()))
                                .collect(Collectors.toSet());
    }

    protected void printMostViolatedRules(final Project project, final Section section) throws DocumentException {

        List<Rule> mostViolatedRules = project.getMostViolatedRules();
        Paragraph mostViolatedRulesTitle = new Paragraph(getTextProperty("general.most_violated_rules"),
                Style.UNDERLINED_FONT);

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(new Paragraph(mostViolatedRulesTitle));

        String[] priorities = Priority.getPrioritiesArray();
        for (String priority : priorities) {
            if (countViolationsPerPriority(mostViolatedRules, priority) > 0) {
                // Most Violated Rules Table
                CustomTable tableMostViolatesRules = new CustomTable(3);
                tableMostViolatesRules.setWidths(new int[]{30, 4, 3});

                // Most Violated Rules Header
                tableMostViolatesRules.addCell(tableHeader(getTextProperty("general.rule_name")));
                tableMostViolatesRules.addCell(tableHeader(getTextProperty("general.language_name")));
                tableMostViolatesRules.addCell(tableHeader(getTextProperty("general.rule_count")));

                // Most Violated Rules Values
                int rowIndex = 0;
                for (Rule rule : filterViolationsPerPriority(mostViolatedRules, priority)) {
                    BaseColor rowBg = (rowIndex % 2 == 0) ? BaseColor.WHITE : Style.TABLE_ROW_ALT_BACKGROUND_COLOR;

                    CustomCellTitle ruleNameValue = new CustomCellTitle(
                            new Phrase(rule.getName(), Style.DASHBOARD_DATA_FONT_2));
                    ruleNameValue.setBackgroundColor(rowBg);
                    tableMostViolatesRules.addCell(ruleNameValue);

                    CustomCellTitle languageNameValue = new CustomCellTitle(
                            new Phrase(rule.getLanguageName(), Style.DASHBOARD_DATA_FONT_2));
                    languageNameValue.setBackgroundColor(rowBg);
                    tableMostViolatesRules.addCell(languageNameValue);

                    CustomCellValue ruleCountValue = new CustomCellValue(
                            new Phrase(rule.getCount().toString(), Style.DASHBOARD_DATA_FONT_2));
                    ruleCountValue.setBackgroundColor(rowBg);
                    tableMostViolatesRules.addCell(ruleCountValue);
                    rowIndex++;
                }

                section.add(new Paragraph(" "));
                section.add(
                        new Paragraph("SEVERITY : " + Priority.getPriority(priority), Style.NORMAL_HIGHLIGHTED_FONT));
                section.add(new Paragraph(" "));
                section.add(tableMostViolatesRules);
            } else {
                CustomTable tableMostViolatesRules = new CustomTable(1);

                CustomCellTitle noViolatedRulesHeader = new CustomCellTitle(new Phrase(
                        getTextProperty("general.no_violated_rules") + "of Severity " + Priority.getPriority(priority),
                        Style.DASHBOARD_TITLE_FONT));

                tableMostViolatesRules.addCell(noViolatedRulesHeader);

                section.add(new Paragraph(" "));
                section.add(
                        new Paragraph("SEVERITY : " + Priority.getPriority(priority), Style.NORMAL_HIGHLIGHTED_FONT));
                section.add(new Paragraph(" "));
                section.add(tableMostViolatesRules);
            }
        }

    }

    private List<Rule> filterViolationsPerPriority(final List<Rule> mostViolatedRules, final String priority) {
        return mostViolatedRules.stream()
                                .filter(r -> r.getSeverity().equals(Priority.getPriority(priority)))
                                .collect(Collectors.toList());
    }

    private long countViolationsPerPriority(final List<Rule> mostViolatedRules, final String priority) {
        return mostViolatedRules.stream().filter(r -> r.getSeverity().equals(Priority.getPriority(priority))).count();
    }

    protected void printMostViolatedFiles(final Project project, final Section section) throws DocumentException {

        List<FileInfo> mostViolatedFiles = project.getMostViolatedFiles().stream()
                                                  .filter(f -> f.isContentSet(FileInfo.VIOLATIONS_CONTENT)).collect(Collectors.toList());
        Paragraph mostViolatedFilesTitle = new Paragraph(getTextProperty("general.most_violated_files"),
                Style.UNDERLINED_FONT);

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(new Paragraph(mostViolatedFilesTitle));

        if (mostViolatedFiles.size() > 0) {
            for (FileInfo fileInfo : mostViolatedFiles) {

                CustomTable tableMostViolatesFiles = new CustomTable(2);
                tableMostViolatesFiles.setWidths(new int[]{4, 25});

                // File Name Header
                CustomCellTitle fileNameHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_NAME), Style.DASHBOARD_TITLE_FONT));
                tableMostViolatesFiles.addCell(fileNameHeader);

                // File Name Value
                CustomCellTitle fileNameValue = new CustomCellTitle(
                        new Phrase(fileInfo.getName(), Style.DASHBOARD_DATA_FONT_2));
                tableMostViolatesFiles.addCell(fileNameValue);

                // File Path Header
                CustomCellTitle filePathHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_PATH), Style.DASHBOARD_TITLE_FONT));
                tableMostViolatesFiles.addCell(filePathHeader);

                // File Path Value
                CustomCellTitle filePathValue = new CustomCellTitle(
                        new Phrase(fileInfo.getPath(), Style.DASHBOARD_DATA_FILEPATH_FONT_2));
                tableMostViolatesFiles.addCell(filePathValue);

                // Violations Header
                CustomCellTitle violationsHeader = new CustomCellTitle(
                        new Phrase(getTextProperty("general.file_violations"), Style.DASHBOARD_TITLE_FONT));
                tableMostViolatesFiles.addCell(violationsHeader);

                // Name Value
                CustomCellTitle violationsValue = new CustomCellTitle(
                        new Phrase(fileInfo.getViolations(), Style.DASHBOARD_DATA_FONT_2));
                tableMostViolatesFiles.addCell(violationsValue);

                section.add(new Paragraph(" "));
                section.add(tableMostViolatesFiles);
            }

        } else {
            CustomTable tableMostViolatesFiles = new CustomTable(1);

            CustomCellTitle noViolatedFilesHeader = new CustomCellTitle(
                    new Phrase(getTextProperty("general.no_violated_files"), Style.DASHBOARD_TITLE_FONT));

            tableMostViolatesFiles.addCell(noViolatedFilesHeader);

            section.add(new Paragraph(" "));
            section.add(tableMostViolatesFiles);
        }
    }

    protected void printMostComplexFiles(final Project project, final Section section) throws DocumentException {

        List<FileInfo> mostComplexFiles = project.getMostComplexFiles().stream()
                                                 .filter(f -> f.isContentSet(FileInfo.CCN_CONTENT)).collect(Collectors.toList());
        Paragraph mostComplexFilesTitle = new Paragraph(getTextProperty("general.most_complex_files"),
                Style.UNDERLINED_FONT);

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(new Paragraph(mostComplexFilesTitle));

        if (mostComplexFiles.size() > 0) {
            for (FileInfo fileInfo : mostComplexFiles) {

                CustomTable tableMostComplexFiles = new CustomTable(2);
                tableMostComplexFiles.setWidths(new int[]{4, 25});

                // File Name Header
                CustomCellTitle fileNameHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_NAME), Style.DASHBOARD_TITLE_FONT));
                tableMostComplexFiles.addCell(fileNameHeader);

                // File Name Value
                CustomCellTitle fileNameValue = new CustomCellTitle(
                        new Phrase(fileInfo.getName(), Style.DASHBOARD_DATA_FONT_2));
                tableMostComplexFiles.addCell(fileNameValue);

                // File Path Header
                CustomCellTitle filePathHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_PATH), Style.DASHBOARD_TITLE_FONT));
                tableMostComplexFiles.addCell(filePathHeader);

                // File Path Value
                CustomCellTitle filePathValue = new CustomCellTitle(
                        new Phrase(fileInfo.getPath(), Style.DASHBOARD_DATA_FONT_2));
                tableMostComplexFiles.addCell(filePathValue);

                // Violations Header
                CustomCellTitle complexityHeader = new CustomCellTitle(
                        new Phrase(getTextProperty("general.file_complexity"), Style.DASHBOARD_TITLE_FONT));
                tableMostComplexFiles.addCell(complexityHeader);

                // Name Value
                CustomCellTitle complexityValue = new CustomCellTitle(
                        new Phrase(fileInfo.getComplexity(), Style.DASHBOARD_DATA_FONT_2));
                tableMostComplexFiles.addCell(complexityValue);

                section.add(new Paragraph(" "));
                section.add(tableMostComplexFiles);
            }

        } else {
            CustomTable tableMostComplexFiles = new CustomTable(1);

            CustomCellTitle noComplexityHeader = new CustomCellTitle(
                    new Phrase(getTextProperty("general.no_complex_files"), Style.DASHBOARD_TITLE_FONT));

            tableMostComplexFiles.addCell(noComplexityHeader);

            section.add(new Paragraph(" "));
            section.add(tableMostComplexFiles);
        }
    }

    protected void printMostDuplicatedFiles(final Project project, final Section section) throws DocumentException {

        List<FileInfo> mostDuplicatedFiles = project.getMostDuplicatedFiles().stream()
                                                    .filter(f -> f.isContentSet(FileInfo.DUPLICATIONS_CONTENT)).collect(Collectors.toList());
        // LOGGER.info("Size of duplicated lines : " +
        // String.valueOf(mostDuplicatedFiles.size()));

        Paragraph mostDuplicatedFilesTitle = new Paragraph(getTextProperty("general.most_duplicated_files"),
                Style.UNDERLINED_FONT);

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(new Paragraph(mostDuplicatedFilesTitle));

        if (mostDuplicatedFiles.size() > 0) {
            for (FileInfo fileInfo : mostDuplicatedFiles) {

                CustomTable tableMostDuplicatedFiles = new CustomTable(2);
                tableMostDuplicatedFiles.setWidths(new int[]{4, 25});

                // File Name Header
                CustomCellTitle fileNameHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_NAME), Style.DASHBOARD_TITLE_FONT));
                tableMostDuplicatedFiles.addCell(fileNameHeader);

                // File Name Value
                CustomCellTitle fileNameValue = new CustomCellTitle(
                        new Phrase(fileInfo.getName(), Style.DASHBOARD_DATA_FONT_2));
                tableMostDuplicatedFiles.addCell(fileNameValue);

                // File Path Header
                CustomCellTitle filePathHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_PATH), Style.DASHBOARD_TITLE_FONT));
                tableMostDuplicatedFiles.addCell(filePathHeader);

                // File Path Value
                CustomCellTitle filePathValue = new CustomCellTitle(
                        new Phrase(fileInfo.getPath(), Style.DASHBOARD_DATA_FONT_2));
                tableMostDuplicatedFiles.addCell(filePathValue);

                // Violations Header
                CustomCellTitle duplicationsHeader = new CustomCellTitle(
                        new Phrase(getTextProperty("general.file_duplicated_lines"), Style.DASHBOARD_TITLE_FONT));
                tableMostDuplicatedFiles.addCell(duplicationsHeader);

                // Name Value
                CustomCellTitle duplicationsValue = new CustomCellTitle(
                        new Phrase(fileInfo.getDuplicatedLines(), Style.DASHBOARD_DATA_FONT_2));
                tableMostDuplicatedFiles.addCell(duplicationsValue);

                section.add(new Paragraph(" "));
                section.add(tableMostDuplicatedFiles);
            }

        } else {
            CustomTable tableMostDuplicatedFiles = new CustomTable(1);

            CustomCellTitle noDuplicationsHeader = new CustomCellTitle(
                    new Phrase(getTextProperty("general.no_duplicated_files"), Style.DASHBOARD_TITLE_FONT));

            tableMostDuplicatedFiles.addCell(noDuplicationsHeader);

            section.add(new Paragraph(" "));
            section.add(tableMostDuplicatedFiles);
        }
    }

    protected void printIssuesDetails(final Project project, final Section section) throws DocumentException {


        for (String typeOfIssue : this.typesOfIssue) {
            printTableperIssueType(project, section, typeOfIssue);
        }
    }

    private void printTableperIssueType(final Project project, final Section section, final String typeOfIssue) throws DocumentException {
        Paragraph typesOfIssuesTitle = new Paragraph(StringUtils.capitalize(typeOfIssue),
                Style.UNDERLINED_FONT);

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(new Paragraph(typesOfIssuesTitle));

        List<Issue> issues = filterIssuesPerType(project, typeOfIssue);

        if (issues.size() > 0) {
            for (Issue issue : issues) {

                CustomTable tableIssueDetails = new CustomTable(2);
                tableIssueDetails.setWidths(new int[]{4, 25});

                // File Name Header
                CustomCellTitle fileNameHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_NAME), Style.DASHBOARD_TITLE_FONT));
                tableIssueDetails.addCell(fileNameHeader);

                // File Name Value
                CustomCellTitle fileNameValue = new CustomCellTitle(
                        new Phrase(issue.getComponent(), Style.DASHBOARD_DATA_FONT_2));
                tableIssueDetails.addCell(fileNameValue);

                // File Path Header
                CustomCellTitle filePathHeader = new CustomCellTitle(
                        new Phrase(getTextProperty(LANG_FILE_PATH), Style.DASHBOARD_TITLE_FONT));
                tableIssueDetails.addCell(filePathHeader);

                // File Path Value
                CustomCellTitle filePathValue = new CustomCellTitle(
                        new Phrase(issue.getComponentPath(), Style.DASHBOARD_DATA_FONT_2));
                tableIssueDetails.addCell(filePathValue);

                // Severity Header
                CustomCellTitle issueSeverityHeader = new CustomCellTitle(
                        new Phrase(getTextProperty("general.severity"), Style.DASHBOARD_TITLE_FONT));
                tableIssueDetails.addCell(issueSeverityHeader);

                // Severity Value
                CustomCellTitle issueSeverityValue = new CustomCellTitle(
                        new Phrase(issue.getSeverity(), Style.DASHBOARD_DATA_FONT_2));
                tableIssueDetails.addCell(issueSeverityValue);

                // Issue Line Number Header
                CustomCellTitle issueLineHeader = new CustomCellTitle(
                        new Phrase(getTextProperty("general.line"), Style.DASHBOARD_TITLE_FONT));
                tableIssueDetails.addCell(issueLineHeader);

                // Issue Line Number Value
                CustomCellTitle issueLineValue = new CustomCellTitle(
                        new Phrase(issue.getLine().equals(0) ? "NA" : issue.getLine().toString(),
                                Style.DASHBOARD_DATA_FONT_2));
                tableIssueDetails.addCell(issueLineValue);

                // Issue Line Number Header
                CustomCellTitle issueMessageHeader = new CustomCellTitle(
                        new Phrase(getTextProperty("general.message"), Style.DASHBOARD_TITLE_FONT));
                tableIssueDetails.addCell(issueMessageHeader);

                // Issue Line Number Value
                CustomCellTitle issueMessageValue = new CustomCellTitle(
                        new Phrase(issue.getMessage(), Style.DASHBOARD_DATA_FONT_2));
                tableIssueDetails.addCell(issueMessageValue);

                section.add(new Paragraph(" "));
                section.add(tableIssueDetails);
            }

        } else {
            CustomTable tableMostViolatesFiles = new CustomTable(1);

            CustomCellTitle noViolatedFilesHeader = new CustomCellTitle(
                    new Phrase(getTextProperty("general.no_violations"), Style.DASHBOARD_TITLE_FONT));

            tableMostViolatesFiles.addCell(noViolatedFilesHeader);

            section.add(new Paragraph(" "));
            section.add(tableMostViolatesFiles);
        }
    }

    private List<Issue> filterIssuesPerType(final Project project, final String typeOfIssue) {
        return project.getIssues().stream()
                      .filter(i -> i.getType().toUpperCase().replace("_", "").replace(" ", "")
                                    .contains(typeOfIssue.toUpperCase().replace(" ", "").replace("_", "")))
                      .collect(Collectors.toList());
    }

    protected void printReliabilityBoard(final Project project, final Section section) throws DocumentException {

        // Reliability Title
        Paragraph reliabilityTitle = new Paragraph(getTextProperty(METRICS_PREFIX + RELIABILITY.toLowerCase()),
                Style.UNDERLINED_FONT);

        // Reliability Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Reliability Metric Table
        CustomTable tableReliability = null;
        if (project.getMeasures().containsMeasure(NEW_BUGS)) {
            tableReliability = new CustomTable(3);
            tableReliability.setWidths(new int[]{1, 1, 1});
        } else {
            tableReliability = new CustomTable(2);
            tableReliability.setWidths(new int[]{1, 1});
        }

        // Bugs Value
        CustomCellValue bugsValue = new CustomCellValue(
                new Phrase(project.getMeasure(BUGS).getValue(), Style.DASHBOARD_DATA_FONT));
        bugsValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableReliability.addCell(bugsValue);

        // New Bugs Value
        Period_ currentPeriod = getCurrentPeriod(project);
        if (project.getMeasures().containsMeasure(NEW_BUGS)) {
            List<Period> periods = project.getMeasure(NEW_BUGS).getPeriods();
            LOGGER.info("Periods found are {} and we are looking for {}", periods, currentPeriod);
            Optional<Period> period = periods
                    .stream()
                    .filter(p -> p.getIndex().equals(currentPeriod.getIndex()))
                    .findFirst();
            Validate.isTrue(period.isPresent());

            CustomCellValue newBugsValue = new CustomCellValue(
                    new Phrase(period.get().getValue(),
                            Style.DASHBOARD_DATA_FONT));
            newBugsValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            newBugsValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableReliability.addCell(newBugsValue);
        }

        // Reliability Rating Value
        CustomCellValue reliabilityRatingValue = new CustomCellValue(
                new Phrase(Rating.getRating(project.getMeasure(RELIABILITY_RATING).getValue()),
                        Rating.getRatingStyle(project.getMeasure(RELIABILITY_RATING).getValue())));
        reliabilityRatingValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableReliability.addCell(reliabilityRatingValue);

        // Bugs Title
        CustomCellTitle bugs = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + BUGS), Style.DASHBOARD_TITLE_FONT));
        bugs.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableReliability.addCell(bugs);

        // New Bugs Title
        if (project.getMeasures().containsMeasure(NEW_BUGS)) {
            CustomCellTitle newBugs = new CustomCellTitle(
                    new Phrase(getTextProperty(METRICS_PREFIX + NEW_BUGS), Style.DASHBOARD_TITLE_FONT));
            newBugs.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableReliability.addCell(newBugs);
        }

        // Reliability Rating Title
        CustomCellTitle reliabilityRating = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + RELIABILITY_RATING), Style.DASHBOARD_TITLE_FONT));
        reliabilityRating.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableReliability.addCell(reliabilityRating);

        mainTable.addCell(tableReliability);

        // Reliability Other Metrics Table
        CustomTable tableReliabilityOther = new CustomTable(2);
        tableReliabilityOther.setWidths(new int[]{8, 2});

        // Reliability Remediation Effort Title
        CustomCellTitle reliabilityRemediationEffort = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + RELIABILITY_REMEDIATION_EFFORT), Style.DASHBOARD_TITLE_FONT));
        tableReliabilityOther.addCell(reliabilityRemediationEffort);

        // Reliability Remediation Effort Value
        CustomCellValue reliabilityRemediationEffortValue = new CustomCellValue(new Phrase(
                SonarUtil.getWorkDurConversion(
                        Integer.parseInt(project.getMeasure(RELIABILITY_REMEDIATION_EFFORT).getValue())),
                Style.DASHBOARD_DATA_FONT_2));
        tableReliabilityOther.addCell(reliabilityRemediationEffortValue);

        // Reliability Remediation Effort On New Code
        if (project.getMeasures().containsMeasure(NEW_RELIABILITY_REMEDIATION_EFFORT)) {
            // Reliability Remediation Effort On New Code Title
            CustomCellTitle reliabilityRemediationEffortNew = new CustomCellTitle(new Phrase(
                    getTextProperty(METRICS_PREFIX + NEW_RELIABILITY_REMEDIATION_EFFORT), Style.DASHBOARD_TITLE_FONT));
            tableReliabilityOther.addCell(reliabilityRemediationEffortNew);

            // Reliability Remediation Effort On New Code Value
            CustomCellValue reliabilityRemediationEffortNewValue = new CustomCellValue(new Phrase(SonarUtil
                    .getWorkDurConversion(Integer.parseInt(project.getMeasure(NEW_RELIABILITY_REMEDIATION_EFFORT)
                                                                  .getPeriods().stream().filter(p -> p.getIndex() == currentPeriod.getIndex())
                                                                  .findFirst().get().getValue())),
                    Style.DASHBOARD_DATA_FONT_2));
            reliabilityRemediationEffortNewValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableReliabilityOther.addCell(reliabilityRemediationEffortNewValue);
        }

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.RELIABILITY, tableReliabilityOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(reliabilityTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableReliabilityOther);

    }

    private Period_ getCurrentPeriod(Project project) {
        LOGGER.info("Leak period name is {}", leakPeriod);
        LOGGER.info("Periods are {}", project.getMeasures().getPeriods());
        //return Optional.ofNullable(project.getMeasures().getPeriods().get(0));
        Optional<Period_> period = this.leakPeriod.getPeriod(project.getMeasures());
        LOGGER.info("Period chosen is {}", period.orElse(null));
        return period.orElseThrow(() -> new IllegalArgumentException("Cannot find the current period"));
    }

    protected void printSecurityBoard(final Project project, final Section section) throws DocumentException {

        // Security Title
        Paragraph securityTitle = new Paragraph(getTextProperty(METRICS_PREFIX + SECURITY.toLowerCase()),
                Style.UNDERLINED_FONT);

        // Security Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Security Metric Table
        CustomTable tableSecurity = null;
        if (project.getMeasures().containsMeasure(NEW_VULNERABILITIES)) {
            tableSecurity = new CustomTable(3);
            tableSecurity.setWidths(new int[]{2, 2, 2});
        } else {
            tableSecurity = new CustomTable(2);
            tableSecurity.setWidths(new int[]{1, 1});
        }

        // Vulnerabilities Value
        CustomCellValue vulnerabilitiesValue = new CustomCellValue(
                new Phrase(project.getMeasure(VULNERABILITIES).getValue(), Style.DASHBOARD_DATA_FONT));
        vulnerabilitiesValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableSecurity.addCell(vulnerabilitiesValue);

        // New Vulnerabilities Value
        final Period_ period = getCurrentPeriod(project);
        if (project.getMeasures().containsMeasure(NEW_VULNERABILITIES)) {
            final Optional<Period> optionalPeriod = project.getMeasure(NEW_VULNERABILITIES).getPeriods()
                                                           .stream().filter(p -> p.getIndex() == period.getIndex())
                                                           .findFirst();
            final Period period1 = optionalPeriod.get();
            CustomCellValue newVulnerabilitiesValue = new CustomCellValue(
                    new Phrase(period1.getValue(), Style.DASHBOARD_DATA_FONT));
            newVulnerabilitiesValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            newVulnerabilitiesValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableSecurity.addCell(newVulnerabilitiesValue);
        }

        // Security Rating Value
        CustomCellValue securityRatingValue = new CustomCellValue(
                new Phrase(Rating.getRating(project.getMeasure(SECURITY_RATING).getValue()),
                        Rating.getRatingStyle(project.getMeasure(SECURITY_RATING).getValue())));
        securityRatingValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableSecurity.addCell(securityRatingValue);

        // Vulnerabilities Title
        CustomCellTitle vulnerabilities = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + VULNERABILITIES), Style.DASHBOARD_TITLE_FONT));
        vulnerabilities.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableSecurity.addCell(vulnerabilities);

        // New Vulnerabilities Title
        if (project.getMeasures().containsMeasure(NEW_VULNERABILITIES)) {
            CustomCellTitle newVulnerabilities = new CustomCellTitle(
                    new Phrase(getTextProperty(METRICS_PREFIX + NEW_VULNERABILITIES), Style.DASHBOARD_TITLE_FONT));
            newVulnerabilities.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableSecurity.addCell(newVulnerabilities);
        }

        // Security Rating Title
        CustomCellTitle securityRating = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + SECURITY_RATING), Style.DASHBOARD_TITLE_FONT));
        securityRating.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableSecurity.addCell(securityRating);

        mainTable.addCell(tableSecurity);

        // Security Other Metrics Table
        CustomTable tableSecurityOther = new CustomTable(2);
        tableSecurityOther.setWidths(new int[]{8, 2});

        // Security Remediation Effort Title
        CustomCellTitle securityRemediationEffort = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + SECURITY_REMEDIATION_EFFORT), Style.DASHBOARD_TITLE_FONT));
        tableSecurityOther.addCell(securityRemediationEffort);

        // Security Remediation Effort Value
        CustomCellValue securityRemediationEffortValue = new CustomCellValue(new Phrase(
                SonarUtil.getWorkDurConversion(
                        Integer.parseInt(project.getMeasure(SECURITY_REMEDIATION_EFFORT).getValue())),
                Style.DASHBOARD_DATA_FONT_2));
        tableSecurityOther.addCell(securityRemediationEffortValue);

        // Security Remediation Effort on New Code
        if (project.getMeasures().containsMeasure(NEW_SECURITY_REMEDIATION_EFFORT)) {
            // Security Remediation Effort on New Code Title
            CustomCellTitle securityRemediationEffortNew = new CustomCellTitle(new Phrase(
                    getTextProperty(METRICS_PREFIX + NEW_SECURITY_REMEDIATION_EFFORT), Style.DASHBOARD_TITLE_FONT));
            tableSecurityOther.addCell(securityRemediationEffortNew);

            // Security Remediation Effort on New Code Value
            CustomCellValue securityRemediationEffortNewValue = new CustomCellValue(new Phrase(
                    SonarUtil.getWorkDurConversion(Integer.parseInt(project.getMeasure(NEW_SECURITY_REMEDIATION_EFFORT)
                                                                           .getPeriods().stream().filter(p -> p.getIndex() == period.getIndex())
                                                                           .findFirst().get().getValue())),
                    Style.DASHBOARD_DATA_FONT_2));
            securityRemediationEffortNewValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableSecurityOther.addCell(securityRemediationEffortNewValue);
        }

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.SECURITY, tableSecurityOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(securityTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableSecurityOther);
    }


    protected void printCoverageBoard(final Project project, final Section section) throws DocumentException {
        if (!project.getMeasures().containsMeasure(MetricKeys.COVERAGE)) {
            LOGGER.warn("No coverage data");
            return;
        }

        // Coverage Title
        Paragraph coverageTitle = new Paragraph(getTextProperty(METRICS_PREFIX +
                MetricDomains.COVERAGE.toLowerCase()), Style.UNDERLINED_FONT);

        //Coverage Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        CustomTable tableCoverage = null;
        if (project.getMeasures().containsMeasure(MetricKeys.COVERAGE) &&
                project.getMeasures().containsMeasure(MetricKeys.COVERAGE)) {
            tableCoverage = new CustomTable(3);
            tableCoverage.setWidths(new int[]{
                    1, 1, 1});
        } else {
            tableCoverage = new CustomTable(2);
            tableCoverage.setWidths(new int[]{1, 1});
        }

        // Coverage Metric Table
        //CustomTable tableCoverage = new CustomTable(1);

        // Coverage Density Value
        CustomCellValue coverageDensityValue = new CustomCellValue(new Phrase(project.getMeasure(MetricKeys.COVERAGE).getValue() + "%",
                Style.DASHBOARD_DATA_FONT));
        coverageDensityValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableCoverage.addCell(coverageDensityValue);

        // Coverage Density Title
        CustomCellTitle coverageDensity = new CustomCellTitle(new Phrase(getTextProperty(METRICS_PREFIX +
                MetricKeys.COVERAGE), Style.DASHBOARD_TITLE_FONT));
        coverageDensity.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableCoverage.addCell(coverageDensity);

        mainTable.addCell(tableCoverage);

        // Coverage Other Metrics Table
        CustomTable tableCoverageOther = new CustomTable(2);
        tableCoverageOther.setWidths(new int[]{8, 2});

        // Line Coverage Title
        CustomCellTitle lineCoverage = new CustomCellTitle(new Phrase(getTextProperty(METRICS_PREFIX + MetricKeys.LINE_COVERAGE),
                Style.DASHBOARD_TITLE_FONT));
        tableCoverageOther.addCell(lineCoverage);

        // Line Coverage Value
        CustomCellValue lineCoverageValue = new CustomCellValue(new Phrase(project.getMeasure(MetricKeys.LINE_COVERAGE).getValue()
                + "%", Style.DASHBOARD_DATA_FONT_2));
        tableCoverageOther.addCell(lineCoverageValue);

        // Branch Coverage Title
        CustomCellTitle branchCoverage = new CustomCellTitle(new Phrase(getTextProperty(METRICS_PREFIX +
                MetricKeys.BRANCH_COVERAGE), Style.DASHBOARD_TITLE_FONT));
        tableCoverageOther.addCell(branchCoverage);

        // Branch Coverage Value
        CustomCellValue branchCoverageValue = new CustomCellValue(new
                Phrase(project.getMeasure(MetricKeys.BRANCH_COVERAGE).getValue() + "%",
                Style.DASHBOARD_DATA_FONT_2));
        tableCoverageOther.addCell(branchCoverageValue);

        // Uncovered Lines Title
        CustomCellTitle uncoveredLines = new CustomCellTitle(new Phrase(getTextProperty(METRICS_PREFIX +
                MetricKeys.UNCOVERED_LINES), Style.DASHBOARD_TITLE_FONT));
        tableCoverageOther.addCell(uncoveredLines);

        // Uncovered Lines Value
        CustomCellValue uncoveredLinesValue = new CustomCellValue(new
                Phrase(project.getMeasure(MetricKeys.UNCOVERED_LINES).getValue(),
                Style.DASHBOARD_DATA_FONT_2));
        tableCoverageOther.addCell(uncoveredLinesValue);
        // Uncovered Conditions Title
        //
        CustomCellTitle uncoveredConditions = new CustomCellTitle(new Phrase(getTextProperty(METRICS_PREFIX +
                MetricKeys.UNCOVERED_CONDITIONS), Style.DASHBOARD_TITLE_FONT));
        tableCoverageOther.addCell(uncoveredConditions);

        // Uncovered Conditions Value
        //
        CustomCellValue uncoveredConditionsValue = new CustomCellValue(new
                Phrase(project.getMeasure(MetricKeys.UNCOVERED_CONDITIONS).getValue(),
                Style.DASHBOARD_DATA_FONT_2));
        tableCoverageOther.addCell(uncoveredConditionsValue);

        // Lines To Cover Title
        CustomCellTitle linesToCover = new CustomCellTitle(new Phrase(getTextProperty(METRICS_PREFIX + MetricKeys.LINES_TO_COVER),
                Style.DASHBOARD_TITLE_FONT));
        tableCoverageOther.addCell(linesToCover);

        // Lines To Cover Value
        CustomCellValue linesToCoverValue = new CustomCellValue(new
                Phrase(project.getMeasure(MetricKeys.LINES_TO_COVER).getValue(),
                Style.DASHBOARD_DATA_FONT_2));
        tableCoverageOther.addCell(linesToCoverValue);


        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project,
                    MetricDomains.COVERAGE, tableCoverageOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(coverageTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new
                Font(FontFamily.COURIER, 3)));
        section.add(tableCoverageOther);

    }


    protected void printMaintainabilityBoard(final Project project, final Section section) throws DocumentException {

        // Maintainability Title
        Paragraph maintainabilityTitle = new Paragraph(getTextProperty(METRICS_PREFIX + MAINTAINABILITY.toLowerCase()),
                Style.UNDERLINED_FONT);

        // Maintainability Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Maintainability Metric Table
        CustomTable tableMaintainability = null;
        if (project.getMeasures().containsMeasure(NEW_CODE_SMELLS)) {
            tableMaintainability = new CustomTable(3);
            tableMaintainability.setWidths(new int[]{1, 1, 1});
        } else {
            tableMaintainability = new CustomTable(2);
            tableMaintainability.setWidths(new int[]{1, 1});
        }

        // Code Smells Value
        CustomCellValue codeSmellsValue = new CustomCellValue(
                new Phrase(project.getMeasure(CODE_SMELLS).getValue(), Style.DASHBOARD_DATA_FONT));
        codeSmellsValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableMaintainability.addCell(codeSmellsValue);

        // New Code Smells Value
        final Period_ period_ = getCurrentPeriod(project);
        if (project.getMeasures().containsMeasure(NEW_CODE_SMELLS)) {
            final Optional<Period> optionalPeriod = project.getMeasure(NEW_CODE_SMELLS).getPeriods()
                                                           .stream().filter(p -> p.getIndex() == period_.getIndex())
                                                           .findFirst();
            final Period period = optionalPeriod.get();
            CustomCellValue newCodeSmellsValue = new CustomCellValue(
                    new Phrase(period.getValue(), Style.DASHBOARD_DATA_FONT));
            newCodeSmellsValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            newCodeSmellsValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableMaintainability.addCell(newCodeSmellsValue);
        }

        // Maintainability Rating Value
        CustomCellValue maintainabilityRatingValue = new CustomCellValue(
                new Phrase(Rating.getRating(project.getMeasure(SQALE_RATING).getValue()),
                        Rating.getRatingStyle(project.getMeasure(SQALE_RATING).getValue())));
        maintainabilityRatingValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableMaintainability.addCell(maintainabilityRatingValue);

        // Code Smells Title
        CustomCellTitle codeSmells = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + CODE_SMELLS), Style.DASHBOARD_TITLE_FONT));
        codeSmells.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableMaintainability.addCell(codeSmells);

        // New Code Smells Title
        if (project.getMeasures().containsMeasure(NEW_CODE_SMELLS)) {
            CustomCellTitle newCodeSmells = new CustomCellTitle(
                    new Phrase(getTextProperty(METRICS_PREFIX + NEW_CODE_SMELLS), Style.DASHBOARD_TITLE_FONT));
            newCodeSmells.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableMaintainability.addCell(newCodeSmells);
        }

        // Maintainability Rating Title
        CustomCellTitle maintainabilityRating = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + SQALE_RATING), Style.DASHBOARD_TITLE_FONT));
        maintainabilityRating.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableMaintainability.addCell(maintainabilityRating);

        mainTable.addCell(tableMaintainability);

        // Maintainability Other Metrics Table
        CustomTable tableMaintainabilityOther = new CustomTable(2);
        tableMaintainabilityOther.setWidths(new int[]{8, 2});

        // Technical Debt Title
        CustomCellTitle technicalDebt = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + SQALE_INDEX), Style.DASHBOARD_TITLE_FONT));
        technicalDebt.setExtraParagraphSpace(5);
        tableMaintainabilityOther.addCell(technicalDebt);

        // Technical Debt Value
        CustomCellValue technicalDebtValue = new CustomCellValue(
                new Phrase(SonarUtil.getWorkDurConversion(Integer.parseInt(project.getMeasure(SQALE_INDEX).getValue())),
                        Style.DASHBOARD_DATA_FONT_2));
        tableMaintainabilityOther.addCell(technicalDebtValue);

        // Added Technical Debt
        if (project.getMeasures().containsMeasure(NEW_TECHNICAL_DEBT)) {
            // Added Technical Debt Title
            CustomCellTitle technicalDebtNew = new CustomCellTitle(
                    new Phrase(getTextProperty(METRICS_PREFIX + NEW_TECHNICAL_DEBT), Style.DASHBOARD_TITLE_FONT));
            tableMaintainabilityOther.addCell(technicalDebtNew);

            // Added Technical Debt Value
            final Optional<Period> optionalPeriod = project.getMeasure(NEW_TECHNICAL_DEBT)
                                                           .getPeriods().stream().filter(p -> p.getIndex() == period_.getIndex())
                                                           .findFirst();
            final Period period = optionalPeriod.get();
            CustomCellValue technicalDebtNewValue = new CustomCellValue(new Phrase(
                    SonarUtil.getWorkDurConversion(Integer.parseInt(period.getValue())),
                    Style.DASHBOARD_DATA_FONT_2));
            technicalDebtNewValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableMaintainabilityOther.addCell(technicalDebtNewValue);
        }

        // Technical Debt Ratio Title
        CustomCellTitle technicalDebtRatio = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + SQALE_DEBT_RATIO), Style.DASHBOARD_TITLE_FONT));
        tableMaintainabilityOther.addCell(technicalDebtRatio);

        // Technical Debt Ratio Value
        CustomCellValue technicalDebtRatioValue = new CustomCellValue(
                new Phrase(project.getMeasure(SQALE_DEBT_RATIO).getValue() + "%", Style.DASHBOARD_DATA_FONT_2));
        tableMaintainabilityOther.addCell(technicalDebtRatioValue);

        // Technical Debt Ratio on New Code
        if (project.getMeasures().containsMeasure(NEW_SQALE_DEBT_RATIO)) {
            // Technical Debt Ratio on New Code Title
            CustomCellTitle technicalDebtRatioNew = new CustomCellTitle(
                    new Phrase(getTextProperty(METRICS_PREFIX + NEW_SQALE_DEBT_RATIO), Style.DASHBOARD_TITLE_FONT));
            tableMaintainabilityOther.addCell(technicalDebtRatioNew);

            // Technical Debt Ratio on New Code Value
            CustomCellValue technicalDebtRatioNewValue = new CustomCellValue(
                    new Phrase(project.getMeasure(NEW_SQALE_DEBT_RATIO).getPeriods().get(0).getValue() + "%",
                            Style.DASHBOARD_DATA_FONT_2));
            technicalDebtRatioNewValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableMaintainabilityOther.addCell(technicalDebtRatioNewValue);
        }

        // Effort To Reach Maintainability Rating A Title
        CustomCellTitle effortToReachMaintainabilityRatingA = new CustomCellTitle(new Phrase(
                getTextProperty(METRICS_PREFIX + EFFORT_TO_REACH_MAINTAINABILITY_RATING_A), Style.DASHBOARD_TITLE_FONT));
        tableMaintainabilityOther.addCell(effortToReachMaintainabilityRatingA);

        // Effort To Reach Maintainability Rating A Value
        CustomCellValue effortToReachMaintainabilityRatingAValue = new CustomCellValue(new Phrase(
                SonarUtil.getWorkDurConversion(
                        Integer.parseInt(project.getMeasure(EFFORT_TO_REACH_MAINTAINABILITY_RATING_A).getValue())),
                Style.DASHBOARD_DATA_FONT_2));
        tableMaintainabilityOther.addCell(effortToReachMaintainabilityRatingAValue);

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.MAINTAINABILITY, tableMaintainabilityOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(maintainabilityTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableMaintainabilityOther);

    }

    protected void printDuplicationsBoard(final Project project, final Section section) throws DocumentException {

        // Duplications Title
        Paragraph duplicationsTitle = new Paragraph(getTextProperty(METRICS_PREFIX + DUPLICATIONS.toLowerCase()),
                Style.UNDERLINED_FONT);

        // Duplications Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Duplications Metric Table
        CustomTable tableDuplications = new CustomTable(1);

        // Duplicated Lines Density Value
        CustomCellValue duplicatedLinesDensityValue = new CustomCellValue(
                new Phrase(project.getMeasure(DUPLICATED_LINES_DENSITY).getValue() + "%", Style.DASHBOARD_DATA_FONT));
        duplicatedLinesDensityValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableDuplications.addCell(duplicatedLinesDensityValue);

        // Duplicated Lines Density Title
        CustomCellTitle duplicatedLinesDensity = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + DUPLICATED_LINES_DENSITY), Style.DASHBOARD_TITLE_FONT));
        duplicatedLinesDensity.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableDuplications.addCell(duplicatedLinesDensity);

        mainTable.addCell(tableDuplications);

        // Duplications Other Metrics Table
        CustomTable tableDuplicationsOther = new CustomTable(2);
        tableDuplicationsOther.setWidths(new int[]{8, 2});

        // Duplicated Blocks Title
        CustomCellTitle duplicatedBlocks = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + DUPLICATED_BLOCKS), Style.DASHBOARD_TITLE_FONT));
        tableDuplicationsOther.addCell(duplicatedBlocks);

        // Duplicated Blocks Value
        CustomCellValue duplicatedBlocksValue = new CustomCellValue(
                new Phrase(project.getMeasure(DUPLICATED_BLOCKS).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableDuplicationsOther.addCell(duplicatedBlocksValue);

        // Duplicated Lines Title
        CustomCellTitle duplicatedLines = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + DUPLICATED_LINES), Style.DASHBOARD_TITLE_FONT));
        tableDuplicationsOther.addCell(duplicatedLines);

        // Duplicated Lines Value
        CustomCellValue duplicatedLinesValue = new CustomCellValue(
                new Phrase(project.getMeasure(DUPLICATED_LINES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableDuplicationsOther.addCell(duplicatedLinesValue);

        // Duplicated Files Title
        CustomCellTitle duplicatedFiles = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + DUPLICATED_FILES), Style.DASHBOARD_TITLE_FONT));
        tableDuplicationsOther.addCell(duplicatedFiles);

        // Duplicated Files Value
        CustomCellValue duplicatedFilesValue = new CustomCellValue(
                new Phrase(project.getMeasure(DUPLICATED_FILES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableDuplicationsOther.addCell(duplicatedFilesValue);

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.DUPLICATIONS, tableDuplicationsOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(duplicationsTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableDuplicationsOther);
    }

    protected void printSizeBoard(final Project project, final Section section) throws DocumentException {

        // Size Title
        Paragraph sizeTitle = new Paragraph(getTextProperty(METRICS_PREFIX + SIZE.toLowerCase()), Style.UNDERLINED_FONT);

        // Size Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Size Metric Table
        CustomTable tableSize = new CustomTable(1);

        // Lines of Code Value
        CustomCellValue linesOfCodeValue = new CustomCellValue(
                new Phrase(project.getMeasure(NCLOC).getValue(), Style.DASHBOARD_DATA_FONT));
        linesOfCodeValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableSize.addCell(linesOfCodeValue);

        // Lines of Code Title
        CustomCellTitle linesOfCode = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + NCLOC), Style.DASHBOARD_TITLE_FONT));
        linesOfCode.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableSize.addCell(linesOfCode);

        mainTable.addCell(tableSize);

        // Size Other Metrics Table
        CustomTable tableSizeOther = new CustomTable(2);
        tableSizeOther.setWidths(new int[]{8, 2});

        // Lines Title
        CustomCellTitle lines = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + LINES), Style.DASHBOARD_TITLE_FONT));
        tableSizeOther.addCell(lines);

        // Lines Value
        CustomCellValue linesValue = new CustomCellValue(
                new Phrase(project.getMeasure(LINES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableSizeOther.addCell(linesValue);

        // Statements Title
        CustomCellTitle statements = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + STATEMENTS), Style.DASHBOARD_TITLE_FONT));
        tableSizeOther.addCell(statements);

        // Statements Value
        CustomCellValue statementsValue = new CustomCellValue(
                new Phrase(project.getMeasure(STATEMENTS).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableSizeOther.addCell(statementsValue);

        // Functions Title
        CustomCellTitle functions = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + FUNCTIONS), Style.DASHBOARD_TITLE_FONT));
        tableSizeOther.addCell(functions);

        // Functions Value
        CustomCellValue functionsValue = new CustomCellValue(
                new Phrase(project.getMeasure(FUNCTIONS).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableSizeOther.addCell(functionsValue);

        // Classes Title
        CustomCellTitle classes = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + CLASSES), Style.DASHBOARD_TITLE_FONT));
        tableSizeOther.addCell(classes);

        // Classes Value
        CustomCellValue classesValue = new CustomCellValue(
                new Phrase(project.getMeasure(CLASSES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableSizeOther.addCell(classesValue);

        // Files Title
        CustomCellTitle files = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + FILES), Style.DASHBOARD_TITLE_FONT));
        tableSizeOther.addCell(files);

        // Files Value
        CustomCellValue filesValue = new CustomCellValue(
                new Phrase(project.getMeasure(FILES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableSizeOther.addCell(filesValue);

        // Directories Title
        CustomCellTitle directories = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + DIRECTORIES), Style.DASHBOARD_TITLE_FONT));
        tableSizeOther.addCell(directories);

        // Directories Value
        CustomCellValue directoriesValue = new CustomCellValue(
                new Phrase(project.getMeasure(DIRECTORIES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableSizeOther.addCell(directoriesValue);

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.SIZE, tableSizeOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(sizeTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableSizeOther);
    }

    protected void printComplexityBoard(final Project project, final Section section) throws DocumentException {

        // Complexity Title
        Paragraph complexityTitle = new Paragraph(getTextProperty(METRICS_PREFIX + MetricDomains.COMPLEXITY.toLowerCase()),
                Style.UNDERLINED_FONT);

        // Complexity Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Complexity Metric Table
        CustomTable tableComplexity = new CustomTable(1);

        // Total Complexity Value
        CustomCellValue complexityTotalValue = new CustomCellValue(
                new Phrase(project.getMeasure(MetricKeys.COMPLEXITY).getValue(), Style.DASHBOARD_DATA_FONT));
        complexityTotalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableComplexity.addCell(complexityTotalValue);

        // Total Complexity Title
        CustomCellTitle complexityTotal = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + MetricKeys.COMPLEXITY), Style.DASHBOARD_TITLE_FONT));
        complexityTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableComplexity.addCell(complexityTotal);

        mainTable.addCell(tableComplexity);

        // Complexity Other Metrics Table
        CustomTable tableComplexityOther = new CustomTable(2);
        tableComplexityOther.setWidths(new int[]{8, 2});

        // Function Complexity Title
        CustomCellTitle functionComplexity = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + FUNCTION_COMPLEXITY), Style.DASHBOARD_TITLE_FONT));
        tableComplexityOther.addCell(functionComplexity);

        // Function Complexity Value
        CustomCellValue duplicatedBlocksValue = new CustomCellValue(
                new Phrase(project.getMeasure(FUNCTION_COMPLEXITY).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableComplexityOther.addCell(duplicatedBlocksValue);

        // File Complexity Title
        CustomCellTitle fileComplexity = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + FILE_COMPLEXITY), Style.DASHBOARD_TITLE_FONT));
        tableComplexityOther.addCell(fileComplexity);

        // File Complexity Value
        CustomCellValue fileComplexityValue = new CustomCellValue(
                new Phrase(project.getMeasure(FILE_COMPLEXITY).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableComplexityOther.addCell(fileComplexityValue);

        // Class Complexity Title
        CustomCellTitle classComplexity = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + CLASS_COMPLEXITY), Style.DASHBOARD_TITLE_FONT));
        tableComplexityOther.addCell(classComplexity);

        // Class Complexity Title
        CustomCellValue classComplexityValue = new CustomCellValue(
                new Phrase(project.getMeasure(CLASS_COMPLEXITY).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableComplexityOther.addCell(classComplexityValue);

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.COMPLEXITY, tableComplexityOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(complexityTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableComplexityOther);
    }

    protected void printDocumentationBoard(final Project project, final Section section) throws DocumentException {

        // Documentations Title
        Paragraph documentationTitle = new Paragraph(getTextProperty(METRICS_PREFIX + DOCUMENTATION.toLowerCase()),
                Style.UNDERLINED_FONT);

        // Documentations Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Documentations Metric Table
        CustomTable tableDocumentation = new CustomTable(1);

        // Comment Lines Density Value
        CustomCellValue commentLinesDensityValue = new CustomCellValue(
                new Phrase(project.getMeasure(COMMENT_LINES_DENSITY).getValue() + "%", Style.DASHBOARD_DATA_FONT));
        commentLinesDensityValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableDocumentation.addCell(commentLinesDensityValue);

        // Comment Lines Density Title
        CustomCellTitle commentLinesDensity = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + COMMENT_LINES_DENSITY), Style.DASHBOARD_TITLE_FONT));
        commentLinesDensity.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableDocumentation.addCell(commentLinesDensity);

        mainTable.addCell(tableDocumentation);

        // Documentaions Other Metrics Table
        CustomTable tableDocumentationOther = new CustomTable(2);
        tableDocumentationOther.setWidths(new int[]{8, 2});

        // Comment Lines Title
        CustomCellTitle commentLines = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + COMMENT_LINES), Style.DASHBOARD_TITLE_FONT));
        tableDocumentationOther.addCell(commentLines);

        // Comment Lines Value
        CustomCellValue commentLinesValue = new CustomCellValue(
                new Phrase(project.getMeasure(COMMENT_LINES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableDocumentationOther.addCell(commentLinesValue);

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.DOCUMENTATION, tableDocumentationOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(documentationTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableDocumentationOther);
    }

    protected void printIssuesBoard(final Project project, final Section section) throws DocumentException {

        // Issues Title
        Paragraph issuesTitle = new Paragraph(getTextProperty(METRICS_PREFIX + ISSUES.toLowerCase()),
                Style.UNDERLINED_FONT);

        // Issues Main Table
        CustomMainTable mainTable = new CustomMainTable(1);

        // Issues Metric Table
        CustomTable tableIssues;
        if (project.getMeasures().containsMeasure(NEW_VIOLATIONS)) {
            tableIssues = new CustomTable(2);
            tableIssues.setWidths(new int[]{1, 1});
        } else {
            tableIssues = new CustomTable(1);
        }

        // Issues Value
        CustomCellValue violationsValue = new CustomCellValue(
                new Phrase(project.getMeasure(VIOLATIONS).getValue(), Style.DASHBOARD_DATA_FONT));
        violationsValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableIssues.addCell(violationsValue);

        final Period_ currentPeriod = getCurrentPeriod(project);
        // New Issues Value
        if (project.getMeasures().containsMeasure(NEW_VIOLATIONS)) {
            CustomCellValue newViolationsValue = new CustomCellValue(
                    new Phrase(
                            project.getMeasure(NEW_VIOLATIONS).getPeriods()
                                   .stream().filter(p -> {
                                return p.getIndex() == currentPeriod.getIndex();
                            })
                                   .findFirst().get().getValue(),
                            Style.DASHBOARD_DATA_FONT));
            newViolationsValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            newViolationsValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
            tableIssues.addCell(newViolationsValue);
        }

        // Issues Title
        CustomCellTitle violations = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + VIOLATIONS), Style.DASHBOARD_TITLE_FONT));
        violations.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableIssues.addCell(violations);

        // New Issues Title
        if (project.getMeasures().containsMeasure(NEW_VIOLATIONS)) {
            CustomCellTitle newViolations = new CustomCellTitle(
                    new Phrase(getTextProperty(METRICS_PREFIX + NEW_VIOLATIONS), Style.DASHBOARD_TITLE_FONT));
            newViolations.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableIssues.addCell(newViolations);
        }

        mainTable.addCell(tableIssues);

        // Issues Other Metrics Table
        CustomTable tableIssuesOther = new CustomTable(2);
        tableIssuesOther.setWidths(new int[]{8, 2});

        // Open Issues Title
        CustomCellTitle openIssues = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + OPEN_ISSUES), Style.DASHBOARD_TITLE_FONT));
        tableIssuesOther.addCell(openIssues);

        // Open Issues Value
        CustomCellValue openIssuesValue = new CustomCellValue(
                new Phrase(project.getMeasure(OPEN_ISSUES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableIssuesOther.addCell(openIssuesValue);

        // Reopened Issues Title
        CustomCellTitle reopenedIssues = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + REOPENED_ISSUES), Style.DASHBOARD_TITLE_FONT));
        tableIssuesOther.addCell(reopenedIssues);

        // Reopened Issues Value
        CustomCellValue reopenedIssuesValue = new CustomCellValue(
                new Phrase(project.getMeasure(REOPENED_ISSUES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableIssuesOther.addCell(reopenedIssuesValue);

        // Confirmed Issues Title
        CustomCellTitle confirmedIssues = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + CONFIRMED_ISSUES), Style.DASHBOARD_TITLE_FONT));
        tableIssuesOther.addCell(confirmedIssues);

        // Confirmed Issues Value
        CustomCellValue confirmedIssuesValue = new CustomCellValue(
                new Phrase(project.getMeasure(CONFIRMED_ISSUES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableIssuesOther.addCell(confirmedIssuesValue);

        // False Positive Issues Title
        CustomCellTitle falsePositiveIssues = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + FALSE_POSITIVE_ISSUES), Style.DASHBOARD_TITLE_FONT));
        tableIssuesOther.addCell(falsePositiveIssues);

        // False Positive Issues Value
        CustomCellValue falsePositiveIssuesValue = new CustomCellValue(
                new Phrase(project.getMeasure(FALSE_POSITIVE_ISSUES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableIssuesOther.addCell(falsePositiveIssuesValue);

        // Won't Fix Issues Title
        CustomCellTitle wontFixIssues = new CustomCellTitle(
                new Phrase(getTextProperty(METRICS_PREFIX + WONT_FIX_ISSUES), Style.DASHBOARD_TITLE_FONT));
        tableIssuesOther.addCell(wontFixIssues);

        // Won't Fix Issues Value
        CustomCellValue wontFixIssuesValue = new CustomCellValue(
                new Phrase(project.getMeasure(WONT_FIX_ISSUES).getValue(), Style.DASHBOARD_DATA_FONT_2));
        tableIssuesOther.addCell(wontFixIssuesValue);

        if (this.otherMetrics != null) {
            printOtherMetricsOfDomain(project, MetricDomains.ISSUES, tableIssuesOther);
        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(issuesTitle);
        section.add(new Paragraph(" "));
        section.add(mainTable);
        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 3)));
        section.add(tableIssuesOther);
    }

    protected void printOtherMetricBoard(final Project project, final Section section) throws DocumentException {
        // LOGGER.info("In other metric board function");
        // Request Metric Title
        Paragraph otherMetricsTitle = new Paragraph("Other Metrics", Style.UNDERLINED_FONT);

        // Requested Metrics Table
        CustomTable tableOtherMetrics = new CustomTable(2);
        tableOtherMetrics.setWidths(new int[]{8, 2});
        // LOGGER.info("Other Metrics List : " + otherMetrics);
        for (String metricName : otherMetrics) {
            // Other Metric Title
            // LOGGER.info("Metric Name : " + metricName);
            CustomCellTitle otherMetric = new CustomCellTitle(
                    new Phrase(project.getMeasure(metricName).getMetricTitle(), Style.DASHBOARD_TITLE_FONT));
            tableOtherMetrics.addCell(otherMetric);

            // Other Metric Value
            // LOGGER.info("Metric Information : " +
            // project.getMeasure(metricName).toString());
            if (hasProjectGivenMeasure(project, metricName)) {
                // LOGGER.info("Metric have value : " +
                // project.getMeasure(metricName).getValue());
                // LOGGER.info("Metric have value length: " +
                // project.getMeasure(metricName).getValue().length());
                CustomCellValue otherMetricValue = new CustomCellValue(
                        new Phrase(SonarUtil.getFormattedValue(project.getMeasure(metricName).getValue(),
                                project.getMeasure(metricName).getDataType()), Style.DASHBOARD_DATA_FONT_2));
                if (metricName.contains("new")) {
                    otherMetricValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
                }
                tableOtherMetrics.addCell(otherMetricValue);
            } else {
                // LOGGER.info("Metric have period value : " +
                // project.getMeasure(metricName).getPeriods().stream()
                // .filter(p -> p.getIndex() ==
                // project.getMeasures().getPeriod_(this.leakPeriod).get().getIndex())
                // .findFirst().get().getValue());
                final Period_ currentPeriod = getCurrentPeriod(project);
                final Optional<Period> optionalPeriod = project.getMeasure(metricName).getPeriods().stream()
                                                               .filter(p -> p.getIndex() == currentPeriod.getIndex())
                                                               .findFirst();
                CustomCellValue otherMetricValue = new CustomCellValue(
                        new Phrase(
                                SonarUtil
                                        .getFormattedValue(
                                                optionalPeriod.get().getValue(),
                                                project.getMeasure(metricName).getDataType()),
                                Style.DASHBOARD_DATA_FONT_2));
                if (metricName.contains("new")) {
                    otherMetricValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
                }
                tableOtherMetrics.addCell(otherMetricValue);
            }

        }

        section.add(new Paragraph(" ", new Font(FontFamily.COURIER, 6)));
        section.add(otherMetricsTitle);
        section.add(new Paragraph(" "));
        section.add(tableOtherMetrics);

    }

    private boolean hasProjectGivenMeasure(final Project project, final String metricName) {
        return project.getMeasure(metricName).getValue() != null
                && project.getMeasure(metricName).getValue().trim().length() > 0;
    }

    protected void printOtherMetricsOfDomain(final Project project, final String domainName,
                                             final CustomTable tableOtherMetrics) {
        final Period_ currentPeriod = getCurrentPeriod(project);

        Set<String> otherMetrics = this.otherMetrics;

        otherMetrics.removeAll(MetricKeys.getAllMetricKeys());
        otherMetrics = otherMetrics.stream().filter(om -> project.getMeasures().containsMeasure(om)
                && project.getMeasure(om).getDomain().equals(domainName)).collect(Collectors.toSet());

        for (String metricName : otherMetrics) {

            // Other Metric Title
            CustomCellTitle otherMetric = new CustomCellTitle(
                    new Phrase(project.getMeasure(metricName).getMetricTitle(), Style.DASHBOARD_TITLE_FONT));
            tableOtherMetrics.addCell(otherMetric);

            // Other Metric Value
            if (hasProjectGivenMeasure(project, metricName)) {
                CustomCellValue otherMetricValue = new CustomCellValue(
                        new Phrase(SonarUtil.getFormattedValue(project.getMeasure(metricName).getValue(),
                                project.getMeasure(metricName).getDataType()), Style.DASHBOARD_DATA_FONT_2));
                if (metricName.contains("new")) {
                    otherMetricValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
                }
                tableOtherMetrics.addCell(otherMetricValue);
            } else {
                final Optional<Period> firstPeriod = project.getMeasure(metricName).getPeriods().stream()
                                                            .filter(p -> p.getIndex() == currentPeriod.getIndex())
                                                            .findFirst();
                CustomCellValue otherMetricValue = new CustomCellValue(
                        new Phrase(
                                SonarUtil
                                        .getFormattedValue(
                                                firstPeriod.get().getValue(),
                                                project.getMeasure(metricName).getDataType()),
                                Style.DASHBOARD_DATA_FONT_2));
                if (metricName.contains("new")) {
                    otherMetricValue.setBackgroundColor(Style.DASHBOARD_NEW_METRIC_BACKGROUND_COLOR);
                }
                tableOtherMetrics.addCell(otherMetricValue);
            }
        }
    }
}