package com.cybage.sonar.report.pdf.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Request.Part;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;

public class PdfReportWebService implements WebService {

    public static final String CONTROLLER_KEY     = "api/pdfreport";
    public static final String STORE_ACTION       = "store";
    public static final String GET_ACTION         = "get";
    public static final String INFO_ACTION        = "info";
    public static final String PARAM_PROJECT      = "project";
    public static final String PARAM_REPORT       = "report";
    public static final String PARAM_CONTENT_TYPE = "content_type";

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfReportWebService.class);

    private final File reportsDir;

    public PdfReportWebService(ServerFileSystem serverFileSystem) {
        // data/ is a writable volume in the SonarQube Docker image; home/ is read-only
        this.reportsDir = new File(serverFileSystem.getHomeDir(), "data/pdf-reports");
        this.reportsDir.mkdirs();
    }

    @Override
    public void define(Context context) {
        NewController controller = context.createController(CONTROLLER_KEY);
        controller.setDescription("PDF/HTML report storage and retrieval for the PDF Report plugin");

        NewAction storeAction = controller.createAction(STORE_ACTION)
                .setPost(true)
                .setDescription("Upload a PDF or HTML report for a project. Requires authentication.")
                .setHandler(this::handleStore);
        storeAction.createParam(PARAM_PROJECT)
                .setRequired(true)
                .setDescription("The project key");
        storeAction.createParam(PARAM_CONTENT_TYPE)
                .setRequired(false)
                .setDescription("Report format: pdf or html")
                .setDefaultValue("pdf")
                .setPossibleValues("pdf", "html");
        storeAction.createParam(PARAM_REPORT)
                .setRequired(true)
                .setDescription("The report file (multipart/form-data)");

        NewAction getAction = controller.createAction(GET_ACTION)
                .setDescription("Download the stored PDF or HTML report for a project.")
                .setHandler(this::handleGet);
        getAction.createParam(PARAM_PROJECT)
                .setRequired(true)
                .setDescription("The project key");
        getAction.createParam(PARAM_CONTENT_TYPE)
                .setRequired(false)
                .setDescription("Report format: pdf or html")
                .setDefaultValue("pdf")
                .setPossibleValues("pdf", "html");

        NewAction infoAction = controller.createAction(INFO_ACTION)
                .setDescription("Returns which report types (pdf, html) exist for a project.")
                .setHandler(this::handleInfo);
        infoAction.createParam(PARAM_PROJECT)
                .setRequired(true)
                .setDescription("The project key");

        controller.done();
    }

    private void handleInfo(Request request, Response response) throws IOException {
        String projectKey = request.mandatoryParam(PARAM_PROJECT);
        boolean hasPdf    = reportFile(projectKey, "pdf").exists();
        boolean hasHtml   = reportFile(projectKey, "html").exists();
        response.newJsonWriter()
                .beginObject()
                .prop("pdf",  hasPdf)
                .prop("html", hasHtml)
                .endObject()
                .close();
    }

    private void handleStore(Request request, Response response) throws IOException {
        String projectKey  = request.mandatoryParam(PARAM_PROJECT);
        String contentType = resolveContentType(request.param(PARAM_CONTENT_TYPE));
        Part   part        = request.paramAsPart(PARAM_REPORT);

        if (part == null) {
            LOGGER.error("No report file received for project {}", projectKey);
            response.stream().setStatus(400).setMediaType("text/plain")
                    .output().write(("Missing report file part '" + PARAM_REPORT + "'").getBytes());
            return;
        }

        File dest = reportFile(projectKey, contentType);
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
            LOGGER.error("Failed to create reports directory: {}", dest.getParentFile().getAbsolutePath());
            response.stream().setStatus(500).setMediaType("text/plain")
                    .output().write(("Cannot create reports directory: " + dest.getParentFile().getAbsolutePath()).getBytes());
            return;
        }
        try (InputStream in = part.getInputStream();
             OutputStream out = new FileOutputStream(dest)) {
            copy(in, out);
        }
        LOGGER.info("Stored {} report for project {} at {}", contentType.toUpperCase(), projectKey, dest.getAbsolutePath());
        response.noContent();
    }

    private void handleGet(Request request, Response response) throws IOException {
        String projectKey  = request.mandatoryParam(PARAM_PROJECT);
        String contentType = resolveContentType(request.param(PARAM_CONTENT_TYPE));
        File   report      = reportFile(projectKey, contentType);

        if (!report.exists()) {
            LOGGER.warn("Report not found for project {} ({})", projectKey, contentType);
            response.stream().setStatus(404).setMediaType("text/plain")
                    .output().write(("No " + contentType.toUpperCase() + " report found for project: " + projectKey).getBytes());
            return;
        }

        String mimeType  = "html".equals(contentType) ? "text/html" : "application/pdf";
        String filename  = report.getName();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        try (InputStream in = new FileInputStream(report);
             OutputStream out = response.stream().setMediaType(mimeType).output()) {
            copy(in, out);
        }
    }

    private File reportFile(String projectKey, String ext) {
        return new File(reportsDir, sanitize(projectKey) + "." + ext);
    }

    private static String resolveContentType(String raw) {
        return (raw != null && raw.equalsIgnoreCase("html")) ? "html" : "pdf";
    }

    private static String sanitize(String projectKey) {
        return projectKey.replace(':', '-').replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }
}
