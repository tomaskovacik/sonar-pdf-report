package com.cybage.sonar.report.pdf.batch;

import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.config.Configuration;
import org.sonarqube.ws.Ce;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.ce.TaskRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class PDFPostJob implements PostJob {

    public static final String  SKIP_PDF_KEY                        = "sonar.pdf.skip";
    public static final boolean SKIP_PDF_DEFAULT_VALUE              = false;
    public static final String  REPORT_TYPE                         = "report.type";
    public static final String  REPORT_TYPE_DEFAULT_VALUE           = "pdf";
    public static final String  SONAR_USER_TOKEN_ENV                = "SONAR_USER_TOKEN";
    public static final String  SONAR_HOST_URL                      = "sonar.host.url";
    public static final String  SONAR_HOST_URL_DEFAULT_VALUE        = "http://localhost:9000";
    public static final String  SONAR_PROJECT_VERSION               = "sonar.projectVersion";
    public static final String  SONAR_PROJECT_VERSION_DEFAULT_VALUE = "1.0";
    public static final String  SONAR_LANGUAGE                      = "sonar.language";
    public static final String  OTHER_METRICS                       = "sonar.pdf.other.metrics";
    public static final String  TYPES_OF_ISSUE                      = "sonar.pdf.issue.details";
    public static final String  LEAK_PERIOD                         = "sonar.leak.period";
    public static final String  LOGO                                = "report.logo";

    // Maximum time to wait for the CE task to finish (5 minutes)
    private static final long CE_TASK_TIMEOUT_MS  = 5 * 60 * 1000L;
    // Interval between CE task status polls
    private static final long CE_POLL_INTERVAL_MS = 5_000L;

    private static final Logger       LOGGER = LoggerFactory.getLogger(PDFPostJob.class);
    private final        FileSystem    fs;
    private final        Configuration configuration;

    public PDFPostJob(Configuration configuration, FileSystem fs) {
        this.fs            = fs;
        this.configuration = configuration;
    }

    @Override
    public void describe(PostJobDescriptor arg0) {
        // No descriptor configuration required for this post-job.
    }

    @Override
    public void execute(PostJobContext postJobContext) {
        Configuration configuration = postJobContext.config();
        if (configuration.hasKey(SKIP_PDF_KEY) && configuration.getBoolean(SKIP_PDF_KEY).get() == true) {
            LOGGER.info("Skipping generation of report (sonar.pdf.skip=true)..");
            return;
        }

        String projectKey = configuration.get("sonar.projectKey").get();
        LOGGER.info("Executing decorator: PDF Report");

        String sonarHostUrl = configuration.hasKey(SONAR_HOST_URL)
                ? configuration.get(SONAR_HOST_URL).get() : SONAR_HOST_URL_DEFAULT_VALUE;

        // Prefer the SONAR_USER_TOKEN environment variable (User Token required; Analysis Tokens are not supported).
        // Fall back to the sonar.token configuration property for backwards compatibility.
        String envToken = getEnvToken();
        if (envToken == null || envToken.isEmpty()) {
            LOGGER.warn("SONAR_USER_TOKEN environment variable is not set. Skipping PDF report generation. "
                    + "Please set the SONAR_USER_TOKEN environment variable with a valid SonarQube user token "
                    + "(see SonarQube documentation: https://docs.sonarsource.com/sonarqube/latest/user-guide/user-account/generating-and-using-tokens/).");
            return;
        }
        String token = envToken;

        waitForCeTask(sonarHostUrl, token);

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

        generatePdfs(projectKey, sonarHostUrl, token, reportType, projectVersion, sonarLanguage, otherMetrics,
                typesOfIssue, leakPeriodConfiguration);
    }

    /**
     * Reads the CE task ID from report-task.txt written by the scanner, then polls
     * /api/ce/task until the task reaches a terminal state (SUCCESS, FAILED, CANCELED).
     * Falls back to a single 5-second sleep if the task ID cannot be determined.
     */
    private void waitForCeTask(String sonarHostUrl, String token) {
        String ceTaskId = readCeTaskId();
        if (ceTaskId == null) {
            LOGGER.warn("Could not read CE task ID from report-task.txt — falling back to 5s wait.");
            sleep(CE_POLL_INTERVAL_MS);
            return;
        }

        LOGGER.info("Waiting for CE task {} to complete before generating report...", ceTaskId);

        HttpConnector connector = HttpConnector.newBuilder()
                .url(sonarHostUrl)
                .token(token)
                .build();
        var wsClient = WsClientFactories.getDefault().newClient(connector);
        TaskRequest taskRequest = new TaskRequest().setId(ceTaskId);

        long deadline = System.currentTimeMillis() + CE_TASK_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            try {
                Ce.TaskResponse response = wsClient.ce().task(taskRequest);
                Ce.TaskStatus  status   = response.getTask().getStatus();
                LOGGER.info("CE task {} status: {}", ceTaskId, status);

                switch (status) {
                    case SUCCESS:
                        LOGGER.info("CE task {} completed successfully. Proceeding with report generation.", ceTaskId);
                        return;
                    case FAILED:
                        LOGGER.warn("CE task {} FAILED. Report will reflect the last successful analysis.", ceTaskId);
                        return;
                    case CANCELED:
                        LOGGER.warn("CE task {} was CANCELED. Report will reflect the last successful analysis.", ceTaskId);
                        return;
                    default:
                        // PENDING or IN_PROGRESS — keep polling
                        break;
                }
            } catch (Exception e) {
                LOGGER.warn("Error polling CE task {}: {}. Retrying...", ceTaskId, e.getMessage());
            }
            sleep(CE_POLL_INTERVAL_MS);
        }

        LOGGER.warn("Timed out waiting for CE task {} after {}s. Proceeding anyway.",
                ceTaskId, CE_TASK_TIMEOUT_MS / 1000);
    }

    /**
     * Reads ceTaskId from the report-task.txt file the scanner writes to the work directory.
     */
    private String readCeTaskId() {
        File reportTaskFile = new File(fs.workDir(), "report-task.txt");
        if (!reportTaskFile.exists()) {
            LOGGER.debug("report-task.txt not found at {}", reportTaskFile.getAbsolutePath());
            return null;
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(reportTaskFile)) {
            props.load(in);
        } catch (IOException e) {
            LOGGER.warn("Failed to read report-task.txt: {}", e.getMessage());
            return null;
        }
        String ceTaskId = props.getProperty("ceTaskId");
        if (ceTaskId == null || ceTaskId.isBlank()) {
            LOGGER.debug("ceTaskId not present in report-task.txt");
            return null;
        }
        return ceTaskId;
    }

    public String getEnvToken() {
        return System.getenv(SONAR_USER_TOKEN_ENV);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
            LOGGER.error("Error in generating report.");
        }
    }
}
