package com.cybage.sonar.report.pdf.util;

import java.io.File;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybage.sonar.report.pdf.batch.PDFPostJob;

public class FileUploader {

    public static final  String                                       UPLOAD_PATH = "/api/ce/submit?projectKey=ALMMaturity_JenkinsService_API_Feature:feature&projectName=ALMMaturity_JenkinsService_API_Feature";
    private static final Logger                                       LOGGER      = LoggerFactory.getLogger(PDFPostJob.class);
    private final        com.cybage.sonar.report.pdf.util.Credentials credentials;

    public FileUploader(com.cybage.sonar.report.pdf.util.Credentials credentials) {
        this.credentials = credentials;

    }

    public void upload(final File reportPath) {
        String     url      = credentials.getUrl();
        PostMethod filePost = new PostMethod(url + UPLOAD_PATH);

        try {
            LOGGER.info("Uploading PDF to server...");
            LOGGER.info("Upload URL : " + url);

            Part[] parts = {new FilePart("upload", reportPath)};

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

            HttpClient client = new HttpClient();
            final String token = credentials.getToken();
            if (token != null && !token.isEmpty()) {
                filePost.setRequestHeader("Authorization", "Bearer " + token);
            }
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);

            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                LOGGER.info("PDF uploaded.");
            } else {
                LOGGER.error("Something went wrong storing the PDF at server side. Status: " + status);
            }
        } catch (Exception ex) {
            LOGGER.error("Something went wrong storing the PDF at server side", ex);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
        }

    }

}
