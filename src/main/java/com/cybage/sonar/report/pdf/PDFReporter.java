package com.cybage.sonar.report.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.cybage.sonar.report.pdf.entity.LeakPeriodConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;

import com.cybage.sonar.report.pdf.builder.ProjectBuilder;
import com.cybage.sonar.report.pdf.entity.Project;
import com.cybage.sonar.report.pdf.entity.exception.ReportException;
import com.cybage.sonar.report.pdf.util.Credentials;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This is the superclass of concrete reporters. It provides the access to Sonar
 * data (project, measures, graphics) and report config data.
 * <p>
 * The concrete reporter class will provide: sonar base URL, logo (it will be
 * used in yhe PDF document), the project key and the implementation of
 * printPdfBody method.
 */
public abstract class PDFReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDFReporter.class);
    private static final String CAN_NOT_GENERATE_TENDENCY_IMAGE = "Can not generate tendency image";

    private final Credentials credentials;
    private final String      branchName;

    private Project project = null;

    protected PDFReporter(final Credentials credentials, final String branchName) {
        this.credentials = credentials;
        this.branchName  = branchName;
    }

    public ByteArrayOutputStream getReport() throws DocumentException, IOException, ReportException {

        // Creation of documents
        Document              mainDocument            = new Document(PageSize.A4, 50, 50, 110, 50);
        Toc                   tocDocument             = new Toc();
        Document              frontPageDocument       = new Document(PageSize.A4, 50, 50, 110, 50);
        ByteArrayOutputStream mainDocumentBaos        = new ByteArrayOutputStream();
        ByteArrayOutputStream frontPageDocumentBaos   = new ByteArrayOutputStream();
        PdfWriter             mainDocumentWriter      = PdfWriter.getInstance(mainDocument, mainDocumentBaos);
        PdfWriter             frontPageDocumentWriter = PdfWriter.getInstance(frontPageDocument, frontPageDocumentBaos);

        try {
            // Events for TOC, header and pages numbers
            Events events = new Events(tocDocument, new Header(this.getLogo(), this.getProject()));
            mainDocumentWriter.setPageEvent(events);

            mainDocument.open();
            tocDocument.getTocDocument().open();
            frontPageDocument.open();

            LOGGER.info("Generating PDF report...");
            printFrontPage(frontPageDocument, frontPageDocumentWriter);
            printTocTitle(tocDocument);
            printPdfBody(mainDocument);
            closeMainDocument(mainDocument);

            tocDocument.getTocDocument().close();
            frontPageDocument.close();

            // Get Readers
            PdfReader mainDocumentReader      = new PdfReader(mainDocumentBaos.toByteArray());
            PdfReader tocDocumentReader       = new PdfReader(tocDocument.getTocOutputStream().toByteArray());
            PdfReader frontPageDocumentReader = new PdfReader(frontPageDocumentBaos.toByteArray());

            // New document
            Document              documentWithToc = new Document(tocDocumentReader.getPageSizeWithRotation(1));
            ByteArrayOutputStream finalBaos       = new ByteArrayOutputStream();
            PdfCopy               copy            = new PdfCopy(documentWithToc, finalBaos);

            documentWithToc.open();
            copy.addPage(copy.getImportedPage(frontPageDocumentReader, 1));
            for (int i = 1; i <= tocDocumentReader.getNumberOfPages(); i++) {
                copy.addPage(copy.getImportedPage(tocDocumentReader, i));
            }
            for (int i = 1; i <= mainDocumentReader.getNumberOfPages(); i++) {
                copy.addPage(copy.getImportedPage(mainDocumentReader, i));
            }
            documentWithToc.close();

            // Return the final document (with TOC)
            return finalBaos;
        } catch (Exception e) {
            LOGGER.error("Exception in PDFReport", e);
        }
        return null;
    }

    private void closeMainDocument(Document doc) {
        try {
            doc.close();
        } catch (Exception e) {
            LOGGER.error("Exception in PDFReporter", e);
        }
    }

    protected abstract URL getLogo();

    public Project getProject() throws IOException, ReportException {
        if (project == null) {
            HttpConnector httpConnector = HttpConnector.newBuilder()
                    .url(credentials.getUrl())
                    .token(credentials.getToken())
                    .build();
            WsClient       wsClient       = WsClientFactories.getDefault().newClient(httpConnector);
            ProjectBuilder projectBuilder = ProjectBuilder.getInstance(wsClient);
            project = projectBuilder.initializeProject(getProjectKey(), getProjectVersion(), getSonarLanguage(),
                    getOtherMetrics(), getTypesOfIssue(), branchName);
        }
        return project;
    }
	 
    protected abstract void printFrontPage(Document frontPageDocument, PdfWriter frontPageWriter)
            throws ReportException;

    protected abstract void printTocTitle(Toc tocDocument) throws DocumentException, IOException;

    protected abstract void printPdfBody(Document document) throws DocumentException, IOException, ReportException;

    protected abstract String getProjectKey();

    protected abstract String getProjectVersion();

    protected abstract List<String> getSonarLanguage();

    protected abstract Set<String> getOtherMetrics();

    protected abstract Set<String> getTypesOfIssue();

    public String getTextProperty(final String key) {
        final String property = getLangProperties().getProperty(key);
        if (property == null) return "missing!" + key;
        return property;
    }

    protected abstract Properties getLangProperties();

    public String getConfigProperty(final String key) {
        return getReportProperties().getProperty(key);
    }

    protected abstract Properties getReportProperties();

    public Image getTendencyImage(final int tendencyQualitative, final int tendencyCuantitative) {
        // tendency parameters are t_qual and t_quant tags returned by
        // webservices api
        String iconName;
        if (tendencyQualitative == 0) {
            switch (tendencyCuantitative) {
                case -2:
                    iconName = "-2-black.png";
                    break;
                case -1:
                    iconName = "-1-black.png";
                    break;
                case 1:
                    iconName = "1-black.png";
                    break;
                case 2:
                    iconName = "2-black.png";
                    break;
                default:
                    iconName = "none.png";
            }
        } else {
            switch (tendencyQualitative) {
                case -2:
                    iconName = "-2-red.png";
                    break;
                case -1:
                    iconName = "-1-red.png";
                    break;
                case 1:
                    iconName = "1-green.png";
                    break;
                case 2:
                    iconName = "2-green.png";
                    break;
                default:
                    iconName = "none.png";
            }
        }
        Image tendencyImage = null;
        try {
            tendencyImage = Image.getInstance(this.getClass().getResource("/tendency/" + iconName));
        } catch (BadElementException | IOException e) {
            LOGGER.error(CAN_NOT_GENERATE_TENDENCY_IMAGE, e);
        }
        return tendencyImage;
    }

    protected abstract LeakPeriodConfiguration getLeakPeriod();

    public abstract String getReportType();


}