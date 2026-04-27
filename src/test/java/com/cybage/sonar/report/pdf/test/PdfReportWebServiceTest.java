package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.server.PdfReportWebService;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

@Test(groups = {"metrics"})
public class PdfReportWebServiceTest {

    private Path tempHomeDir;
    private PdfReportWebService service;
    private File reportsDir;

    @BeforeMethod
    public void setUp() throws IOException {
        tempHomeDir = Files.createTempDirectory("sonar-test-home");
        ServerFileSystem mockFs = mock(ServerFileSystem.class);
        when(mockFs.getHomeDir()).thenReturn(tempHomeDir.toFile());
        service = new PdfReportWebService(mockFs);
        reportsDir = new File(tempHomeDir.toFile(), "data/pdf-reports");
    }

    @AfterMethod
    public void tearDown() {
        deleteDirectory(tempHomeDir.toFile());
    }

    // ---- constants ----

    @Test
    public void testControllerKey() {
        Assert.assertEquals(PdfReportWebService.CONTROLLER_KEY, "api/pdfreport");
    }

    @Test
    public void testStoreAction() {
        Assert.assertEquals(PdfReportWebService.STORE_ACTION, "store");
    }

    @Test
    public void testGetAction() {
        Assert.assertEquals(PdfReportWebService.GET_ACTION, "get");
    }

    @Test
    public void testInfoAction() {
        Assert.assertEquals(PdfReportWebService.INFO_ACTION, "info");
    }

    @Test
    public void testParamProject() {
        Assert.assertEquals(PdfReportWebService.PARAM_PROJECT, "project");
    }

    @Test
    public void testParamReport() {
        Assert.assertEquals(PdfReportWebService.PARAM_REPORT, "report");
    }

    @Test
    public void testParamContentType() {
        Assert.assertEquals(PdfReportWebService.PARAM_CONTENT_TYPE, "content_type");
    }

    // ---- constructor ----

    @Test
    public void testConstructorCreatesReportsDirectory() {
        Assert.assertTrue(reportsDir.exists(), "reports directory should be created by constructor");
        Assert.assertTrue(reportsDir.isDirectory());
    }

    // ---- define() ----

    @Test
    public void testDefineCreatesControllerWithCorrectKey() {
        WebService.Context context = mock(WebService.Context.class, RETURNS_DEEP_STUBS);
        WebService.NewController controller = mock(WebService.NewController.class, RETURNS_DEEP_STUBS);
        when(context.createController(PdfReportWebService.CONTROLLER_KEY)).thenReturn(controller);

        service.define(context);

        verify(context).createController(PdfReportWebService.CONTROLLER_KEY);
    }

    @Test
    public void testDefineCallsDone() {
        WebService.Context context = mock(WebService.Context.class, RETURNS_DEEP_STUBS);
        WebService.NewController controller = mock(WebService.NewController.class, RETURNS_DEEP_STUBS);
        when(context.createController(anyString())).thenReturn(controller);

        service.define(context);

        verify(controller).done();
    }

    // ---- handleInfo() ----

    @Test
    public void testHandleInfoWithNoReportsDoesNotThrow() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class, RETURNS_DEEP_STUBS);
        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");

        invokeHandleInfo(request, response);
        // verifying that newJsonWriter() is called at all is sufficient as a smoke test
        verify(response).newJsonWriter();
    }

    @Test
    public void testHandleInfoWithPdfFilePresent() throws Exception {
        new File(reportsDir, "my-project.pdf").createNewFile();

        Request request = mock(Request.class);
        Response response = mock(Response.class, RETURNS_DEEP_STUBS);
        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");

        invokeHandleInfo(request, response);

        verify(response).newJsonWriter();
    }

    @Test
    public void testHandleInfoWithHtmlFilePresent() throws Exception {
        new File(reportsDir, "my-project.html").createNewFile();

        Request request = mock(Request.class);
        Response response = mock(Response.class, RETURNS_DEEP_STUBS);
        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");

        invokeHandleInfo(request, response);

        verify(response).newJsonWriter();
    }

    // ---- handleStore() ----

    @Test
    public void testHandleStoreNullPartReturns400() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        Response.Stream stream = mock(Response.Stream.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        when(response.stream()).thenReturn(stream);
        when(stream.setStatus(400)).thenReturn(stream);
        when(stream.setMediaType("text/plain")).thenReturn(stream);
        when(stream.output()).thenReturn(bos);

        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("pdf");
        when(request.paramAsPart(PdfReportWebService.PARAM_REPORT)).thenReturn(null);

        invokeHandleStore(request, response);

        verify(stream).setStatus(400);
        Assert.assertTrue(bos.toString().contains("Missing report file part"));
    }

    @Test
    public void testHandleStoreWritesPdfFile() throws Exception {
        byte[] content = "fake-pdf-bytes".getBytes();
        Request.Part part = mock(Request.Part.class);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("pdf");
        when(request.paramAsPart(PdfReportWebService.PARAM_REPORT)).thenReturn(part);

        invokeHandleStore(request, response);

        File stored = new File(reportsDir, "my-project.pdf");
        Assert.assertTrue(stored.exists(), "stored file should exist");
        Assert.assertEquals(Files.readAllBytes(stored.toPath()), content);
        verify(response).noContent();
    }

    @Test
    public void testHandleStoreWritesHtmlFile() throws Exception {
        byte[] content = "<html>report</html>".getBytes();
        Request.Part part = mock(Request.Part.class);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("html");
        when(request.paramAsPart(PdfReportWebService.PARAM_REPORT)).thenReturn(part);

        invokeHandleStore(request, response);

        File stored = new File(reportsDir, "my-project.html");
        Assert.assertTrue(stored.exists(), "stored html file should exist");
        Assert.assertEquals(Files.readAllBytes(stored.toPath()), content);
    }

    @Test
    public void testHandleStoreSanitizesProjectKeyColons() throws Exception {
        byte[] content = "data".getBytes();
        Request.Part part = mock(Request.Part.class);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("group:artifact");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("pdf");
        when(request.paramAsPart(PdfReportWebService.PARAM_REPORT)).thenReturn(part);

        invokeHandleStore(request, response);

        // colon should be replaced with dash
        File stored = new File(reportsDir, "group-artifact.pdf");
        Assert.assertTrue(stored.exists(), "colon in project key should be replaced by dash");
    }

    // ---- handleGet() ----

    @Test
    public void testHandleGetNotFoundReturns404() throws Exception {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        Response.Stream stream = mock(Response.Stream.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        when(response.stream()).thenReturn(stream);
        when(stream.setStatus(404)).thenReturn(stream);
        when(stream.setMediaType("text/plain")).thenReturn(stream);
        when(stream.output()).thenReturn(bos);

        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("no:such:project");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("pdf");

        invokeHandleGet(request, response);

        verify(stream).setStatus(404);
        Assert.assertTrue(bos.toString().contains("No PDF report found"));
    }

    @Test
    public void testHandleGetReturnsStoredPdfContent() throws Exception {
        byte[] content = "pdf-content".getBytes();
        File stored = new File(reportsDir, "my-project.pdf");
        Files.write(stored.toPath(), content);

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        Response.Stream stream = mock(Response.Stream.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        when(response.stream()).thenReturn(stream);
        when(stream.setMediaType("application/pdf")).thenReturn(stream);
        when(stream.output()).thenReturn(bos);

        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("pdf");

        invokeHandleGet(request, response);

        Assert.assertEquals(bos.toByteArray(), content);
    }

    @Test
    public void testHandleGetReturnsStoredHtmlContent() throws Exception {
        byte[] content = "<html/>".getBytes();
        File stored = new File(reportsDir, "my-project.html");
        Files.write(stored.toPath(), content);

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        Response.Stream stream = mock(Response.Stream.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        when(response.stream()).thenReturn(stream);
        when(stream.setMediaType("text/html")).thenReturn(stream);
        when(stream.output()).thenReturn(bos);

        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("html");

        invokeHandleGet(request, response);

        Assert.assertEquals(bos.toByteArray(), content);
    }

    @Test
    public void testHandleGetSetsContentDispositionHeader() throws Exception {
        byte[] content = "data".getBytes();
        File stored = new File(reportsDir, "my-project.pdf");
        Files.write(stored.toPath(), content);

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        Response.Stream stream = mock(Response.Stream.class);
        when(response.stream()).thenReturn(stream);
        when(stream.setMediaType(anyString())).thenReturn(stream);
        when(stream.output()).thenReturn(new ByteArrayOutputStream());

        when(request.mandatoryParam(PdfReportWebService.PARAM_PROJECT)).thenReturn("my:project");
        when(request.param(PdfReportWebService.PARAM_CONTENT_TYPE)).thenReturn("pdf");

        invokeHandleGet(request, response);

        verify(response).setHeader(eq("Content-Disposition"), contains("my-project.pdf"));
    }

    // ---- reflection helpers ----

    private void invokeHandleInfo(Request request, Response response) throws Exception {
        Method m = PdfReportWebService.class.getDeclaredMethod("handleInfo", Request.class, Response.class);
        m.setAccessible(true);
        try {
            m.invoke(service, request, response);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void invokeHandleStore(Request request, Response response) throws Exception {
        Method m = PdfReportWebService.class.getDeclaredMethod("handleStore", Request.class, Response.class);
        m.setAccessible(true);
        try {
            m.invoke(service, request, response);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void invokeHandleGet(Request request, Response response) throws Exception {
        Method m = PdfReportWebService.class.getDeclaredMethod("handleGet", Request.class, Response.class);
        m.setAccessible(true);
        try {
            m.invoke(service, request, response);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
