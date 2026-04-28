package com.cybage.sonar.report.pdf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybage.sonar.report.pdf.server.PdfReportWebService;

public class FileUploader {

    private static final Logger      LOGGER      = LoggerFactory.getLogger(FileUploader.class);
    private final        Credentials credentials;

    public FileUploader(Credentials credentials) {
        this.credentials = credentials;
    }

    public void upload(final File reportFile, final String projectKey, final String contentType) {
        String uploadUrl = credentials.getUrl() + "/" + PdfReportWebService.CONTROLLER_KEY
                + "/" + PdfReportWebService.STORE_ACTION;

        String boundary = "----SonarPdfReportBoundary" + Long.toHexString(System.currentTimeMillis());

        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Uploading {} report to SonarQube server: {}", contentType.toUpperCase(), uploadUrl);
            }

            URL url = URI.create(uploadUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            String token = credentials.getToken();
            if (token != null && !token.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }

            try (OutputStream out = connection.getOutputStream()) {
                writeStringPart(out, boundary, PdfReportWebService.PARAM_PROJECT, projectKey);
                writeStringPart(out, boundary, PdfReportWebService.PARAM_CONTENT_TYPE, contentType);
                writeFilePart(out, boundary, PdfReportWebService.PARAM_REPORT, reportFile);
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_NO_CONTENT || status == HttpURLConnection.HTTP_OK) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("{} report uploaded successfully for project '{}'.", contentType.toUpperCase(), projectKey);
                }
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Failed to upload {} report for project '{}'. HTTP status: {}", contentType.toUpperCase(), projectKey, status);
                }
            }
            connection.disconnect();
        } catch (IOException ex) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error uploading {} report to SonarQube server", contentType.toUpperCase(), ex);
            }
        }
    }

    private static void writeStringPart(final OutputStream out, final String boundary,
                                        final String name, final String value) throws IOException {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
                + value + "\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeFilePart(final OutputStream out, final String boundary,
                                      final String name, final File file) throws IOException {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
