package com.cybage.sonar.report.pdf.batch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import com.cybage.sonar.report.pdf.util.FileUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;

import com.cybage.sonar.report.pdf.ExecutivePDFReporter;
import com.cybage.sonar.report.pdf.HTMLReporter;
import com.cybage.sonar.report.pdf.PDFReporter;
import com.cybage.sonar.report.pdf.entity.exception.ReportException;
import com.cybage.sonar.report.pdf.util.Credentials;
import com.itextpdf.text.DocumentException;

/**
 * The type Pdf generator is responsible to configure and launch the PDF report generation.
 */
public class PDFGenerator {

    public static final String SONAR_BASE_URL  = "sonar.base.url";
    public static final String FRONT_PAGE_LOGO = "front.page.logo";
    public static final String DATE_PATTERN    = "yyyy.MM.dd.HH.mm.ss";

    private static final Logger                  LOGGER = LoggerFactory.getLogger(PDFGenerator.class);
    private final        String                  token;
    private final        String                  reportType;
    private final        String                  projectKey;
    private final        String                  projectVersion;
    private final        List<String>            sonarLanguage;
    private final        Set<String>             otherMetrics;
    private final        Set<String>             typesOfIssue;
    private final        LeakPeriodConfiguration leakPeriod;
    private final        FileSystem              fs;
    private              String                  sonarHostUrl;

    public PDFGenerator(final String projectKey,
                        final String projectVersion,
                        final List<String> sonarLanguage,
                        final Set<String> otherMetrics,
                        final Set<String> typesOfIssue,
                        final LeakPeriodConfiguration leakPeriod,
                        final FileSystem fs,
                        final String sonarHostUrl,
                        final String token,
                        final String reportType) {
        this.projectKey     = projectKey;
        this.projectVersion = projectVersion;
        this.sonarLanguage  = sonarLanguage;
        this.otherMetrics   = otherMetrics;
        this.typesOfIssue   = typesOfIssue;
        this.leakPeriod     = leakPeriod;
        this.fs             = fs;
        this.sonarHostUrl   = sonarHostUrl;
        this.token          = token;
        this.reportType     = reportType;
    }

    public void execute() {
        Properties config     = new Properties();
        Properties configLang = new Properties();

        try {
            configureAndLaunchReports(config, configLang);


        } catch (IOException | DocumentException e) {
            LOGGER.error("Problem in generating PDF file.", e);
        } catch (ReportException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void configureAndLaunchReports(Properties config, Properties configLang) throws IOException, ReportException, DocumentException {
        if (sonarHostUrl != null) {
            if (sonarHostUrl.endsWith("/")) {
                sonarHostUrl = sonarHostUrl.substring(0, sonarHostUrl.length() - 1);
            }
            config.put(SONAR_BASE_URL, sonarHostUrl);
            config.put(FRONT_PAGE_LOGO, "sonar.png");
        } else {
            config.load(this.getClass().getResourceAsStream("/report.properties"));
        }
        configLang.load(this.getClass().getResourceAsStream("/report-texts-en.properties"));

        Credentials credentials = new Credentials(config.getProperty(SONAR_BASE_URL), token);

        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

        final String path = computeReportPath(projectKey, sdf);

        PDFReporter reporter = initializeReporter(config, configLang, credentials, projectKey, projectVersion, sonarLanguage, otherMetrics, typesOfIssue, leakPeriod);

        if (reporter == null) {
            LOGGER.warn("Could not initialize the reporting plugin");
            return;
        }
        writeReport(projectKey, sdf, path, reporter);
        uploadReport(path, credentials, projectKey, reporter.getReportType());
    }

    private void uploadReport(final String path, final Credentials credentials, final String projectKey, final String reportType) {
        final FileUploader fileUploader = new FileUploader(credentials);
        String contentType = (reportType != null && reportType.equalsIgnoreCase("html")) ? "html" : "pdf";
        fileUploader.upload(new File(path), projectKey, contentType);
    }

    private String computeReportPath(String projectId, SimpleDateFormat sdf) {
        String ext = isHtmlReport() ? ".html" : ".pdf";
        return fs.workDir().getAbsolutePath() + "/" + projectId.replace(':', '-') + "-"
                + sdf.format(new Timestamp(System.currentTimeMillis())) + ext;
    }

    private boolean isHtmlReport() {
        return reportType != null && reportType.equalsIgnoreCase("html");
    }

    private PDFReporter initializeReporter(Properties config, Properties configLang, Credentials credentials, String sonarProjectId, String sonarProjectVersion, List<String> sonarLanguage, Set<String> otherMetrics, Set<String> typesOfIssue, LeakPeriodConfiguration leakPeriod) {
        if (reportType == null) {
            LOGGER.info("No report type provided. Default report type selected (PDF)");
            return createPdfReporter(credentials, sonarProjectId, sonarProjectVersion, sonarLanguage, otherMetrics, typesOfIssue, leakPeriod, config, configLang);
        }
        if (reportType.equalsIgnoreCase("pdf")) {
            return createPdfReporter(credentials, sonarProjectId, sonarProjectVersion, sonarLanguage, otherMetrics, typesOfIssue, leakPeriod, config, configLang);
        }
        if (reportType.equalsIgnoreCase("html")) {
            return new HTMLReporter(
                    credentials,
                    this.getClass().getResource("/sonar.png"),
                    sonarProjectId,
                    sonarProjectVersion,
                    sonarLanguage,
                    otherMetrics,
                    typesOfIssue,
                    leakPeriod,
                    config,
                    configLang);
        }
        LOGGER.warn("Unknown report type '{}'. Supported values: pdf, html. Defaulting to PDF.", reportType);
        return createPdfReporter(credentials, sonarProjectId, sonarProjectVersion, sonarLanguage, otherMetrics, typesOfIssue, leakPeriod, config, configLang);
    }

    private ExecutivePDFReporter createPdfReporter(Credentials credentials, String sonarProjectId, String sonarProjectVersion, List<String> sonarLanguage, Set<String> otherMetrics, Set<String> typesOfIssue, LeakPeriodConfiguration leakPeriod, Properties config, Properties configLang) {
        return new ExecutivePDFReporter(
                credentials,
                this.getClass().getResource("/sonar.png"),
                sonarProjectId,
                sonarProjectVersion,
                sonarLanguage,
                otherMetrics,
                typesOfIssue,
                leakPeriod,
                config,
                configLang);
    }

    private static void writeReport(String sonarProjectId, SimpleDateFormat sdf, String path, PDFReporter reporter) throws IOException, ReportException, DocumentException {
        try (ByteArrayOutputStream baos = reporter.getReport();
             FileOutputStream fos = new FileOutputStream(new File(path))) {
            baos.writeTo(fos);
            fos.flush();
            String sonarProjectIdConverted = sonarProjectId.replace(':', '-');
            String ext = reporter.getReportType().equalsIgnoreCase("html") ? "html" : "pdf";
            LOGGER.info("{} report generated (see {}-{}.{} on build output directory)",
                    ext.toUpperCase(), sonarProjectIdConverted, sdf.format(new Timestamp(System.currentTimeMillis())), ext);
        }
    }

}
