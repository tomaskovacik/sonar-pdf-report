package com.cybage.sonar.report.pdf.batch;

import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.config.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PDFPostJob implements PostJob {

    public static final  String        SKIP_PDF_KEY                        = "sonar.pdf.skip";
    public static final  boolean       SKIP_PDF_DEFAULT_VALUE              = false;
    public static final  String        REPORT_TYPE                         = "report.type";
    public static final  String        REPORT_TYPE_DEFAULT_VALUE           = "pdf";
    public static final  String        SONAR_TOKEN                         = "sonar.token";
    public static final  String        SONAR_USER_TOKEN_ENV                = "SONAR_USER_TOKEN";
    public static final  String        SONAR_HOST_URL                      = "sonar.host.url";
    public static final  String        SONAR_HOST_URL_DEFAULT_VALUE        = "http://localhost:9000";
    public static final  String        SONAR_PROJECT_VERSION               = "sonar.projectVersion";
    public static final  String        SONAR_PROJECT_VERSION_DEFAULT_VALUE = "1.0";
    public static final  String        SONAR_LANGUAGE                      = "sonar.language";
    public static final  String        OTHER_METRICS                       = "sonar.pdf.other.metrics";
    public static final  String        TYPES_OF_ISSUE                      = "sonar.pdf.issue.details";
    public static final  String        LEAK_PERIOD                         = "sonar.leak.period";
    public static final  int           STARTUP_DELAY_IN_MS                 = 5000;
    public static final  String        LOGO                                = "report.logo";
    private static final Logger        LOGGER                              = LoggerFactory.getLogger(PDFPostJob.class);
    private final        FileSystem    fs;
    private final        Configuration configuration;

    public PDFPostJob(Configuration configuration, FileSystem fs) {
        this.fs = fs;
        this.configuration = configuration;
    }

    @Override
    public void describe(PostJobDescriptor arg0) {

    }

    @Override
    public void execute(PostJobContext postJobContext) {
        Configuration configuration = postJobContext.config();
        if (configuration.hasKey(SKIP_PDF_KEY) && configuration.getBoolean(SKIP_PDF_KEY).get() == true) {
            LOGGER.info("Skipping generation of PDF Report..");
            return;
        }

        waitBeforeReporting();

        String projectKey = configuration.get("sonar.projectKey").get();
        LOGGER.info("Executing decorator: PDF Report");

        String sonarHostUrl = configuration.hasKey(SONAR_HOST_URL)
                ? configuration.get(SONAR_HOST_URL).get() : SONAR_HOST_URL_DEFAULT_VALUE;
        // Prefer the SONAR_USER_TOKEN environment variable (User Token required; Analysis Tokens are not supported).
        // Fall back to the sonar.token configuration property for backwards compatibility.
        String envToken = System.getenv(SONAR_USER_TOKEN_ENV);
        String token = (envToken != null && !envToken.isEmpty())
                ? envToken
                : (configuration.hasKey(SONAR_TOKEN) ? configuration.get(SONAR_TOKEN).orElse("") : "");
        String reportType = configuration.hasKey(REPORT_TYPE)
                ? configuration.get(REPORT_TYPE).get() : REPORT_TYPE_DEFAULT_VALUE;
        String projectVersion = configuration.hasKey(SONAR_PROJECT_VERSION)
                ? configuration.get(SONAR_PROJECT_VERSION).get() : SONAR_PROJECT_VERSION_DEFAULT_VALUE;
        List<String> sonarLanguage = configuration.hasKey(SONAR_LANGUAGE)
                ? Arrays.asList(configuration.getStringArray(SONAR_LANGUAGE)) : null;
        Set<String> otherMetrics = configuration.hasKey(OTHER_METRICS)
                ? new HashSet<>(Arrays.asList(configuration.getStringArray(OTHER_METRICS))) : null;
        Set<String> typesOfIssue = configuration.hasKey(TYPES_OF_ISSUE)
                ? new HashSet<>(Arrays.asList(configuration.getStringArray(TYPES_OF_ISSUE)))
                : new HashSet<>();

        LeakPeriodConfiguration leakPeriodConfiguration = new LeakPeriodConfiguration();
        if (configuration.hasKey(LEAK_PERIOD)) {
            String configurationValue = configuration.get(LEAK_PERIOD).get();
            LOGGER.info("Plugin will use the following leak period MODE={}", configurationValue);
            leakPeriodConfiguration.update(configurationValue);
        } else {
            LOGGER.info("Plugin will try to guess the default LEAK Period");
        }


        generatePdfs(projectKey, sonarHostUrl, token, reportType, projectVersion, sonarLanguage, otherMetrics, typesOfIssue, leakPeriodConfiguration);
    }

    private void waitBeforeReporting() {
        try {
            Thread.sleep(STARTUP_DELAY_IN_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generatePdfs(String projectKey,
                              String sonarHostUrl,
                              String token,
                              String reportType,
                              String projectVersion,
                              List<String> sonarLanguage,
                              Set<String> otherMetrics,
                              Set<String> typesOfIssue,
                              LeakPeriodConfiguration leakPeriodConfiguration) {
        PDFGenerator generator = new PDFGenerator(projectKey, projectVersion, sonarLanguage, otherMetrics,
                typesOfIssue, leakPeriodConfiguration, fs, sonarHostUrl, token, reportType);

        try {
            generator.execute();
        } catch (Exception ex) {
            LOGGER.error("Error in generating PDF report.");
        }
    }

}
