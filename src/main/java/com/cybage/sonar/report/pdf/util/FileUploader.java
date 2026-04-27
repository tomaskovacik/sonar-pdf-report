package com.cybage.sonar.report.pdf.util;

import java.io.File;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybage.sonar.report.pdf.batch.PDFPostJob;
import com.cybage.sonar.report.pdf.server.PdfReportWebService;

public class FileUploader {

    private static final Logger      LOGGER      = LoggerFactory.getLogger(PDFPostJob.class);
    private final        Credentials credentials;

    public FileUploader(Credentials credentials) {
        this.credentials = credentials;
    }

    public void upload(final File reportFile, final String projectKey, final String contentType) {
        String uploadUrl = credentials.getUrl() + "/" + PdfReportWebService.CONTROLLER_KEY
                + "/" + PdfReportWebService.STORE_ACTION;
        PostMethod post = new PostMethod(uploadUrl);

        try {
            LOGGER.info("Uploading {} report to SonarQube server: {}", contentType.toUpperCase(), uploadUrl);

            Part[] parts = {
                    new FilePart(PdfReportWebService.PARAM_REPORT, reportFile),
                    new StringPart(PdfReportWebService.PARAM_PROJECT, projectKey),
                    new StringPart(PdfReportWebService.PARAM_CONTENT_TYPE, contentType)
            };

            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

            String token = credentials.getToken();
            if (token != null && !token.isEmpty()) {
                post.setRequestHeader("Authorization", "Bearer " + token);
            }

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);

            int status = client.executeMethod(post);
            if (status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_OK) {
                LOGGER.info("{} report uploaded successfully for project '{}'.", contentType.toUpperCase(), projectKey);
            } else {
                LOGGER.error("Failed to upload {} report for project '{}'. HTTP status: {}", contentType.toUpperCase(), projectKey, status);
            }
        } catch (Exception ex) {
            LOGGER.error("Error uploading {} report to SonarQube server", contentType.toUpperCase(), ex);
        } finally {
            post.releaseConnection();
        }
    }
}
