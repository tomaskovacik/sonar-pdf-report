package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.util.Credentials;
import com.cybage.sonar.report.pdf.util.FileUploader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Test(groups = {"metrics"})
public class FileUploaderTest {

    private FileUploader uploader() {
        return new FileUploader(new Credentials("http://localhost:9000", "token123"));
    }

    // ---- constructor ----

    @Test
    public void testConstructorDoesNotThrow() {
        FileUploader fu = uploader();
        Assert.assertNotNull(fu);
    }

    // ---- writeStringPart() via reflection ----

    @Test
    public void testWriteStringPartProducesMultipartHeader() throws Exception {
        ByteArrayOutputStream baos     = new ByteArrayOutputStream();
        String                boundary = "testboundary";
        invokeWriteStringPart(baos, boundary, "myField", "myValue");

        String result = baos.toString(StandardCharsets.UTF_8);
        Assert.assertTrue(result.contains("--testboundary"), "should contain boundary marker");
        Assert.assertTrue(result.contains("Content-Disposition: form-data; name=\"myField\""),
                "should contain field name");
        Assert.assertTrue(result.contains("myValue"), "should contain field value");
    }

    @Test
    public void testWriteStringPartEndsWithCRLF() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        invokeWriteStringPart(baos, "b", "f", "v");
        String result = baos.toString(StandardCharsets.UTF_8);
        Assert.assertTrue(result.endsWith("\r\n"), "should end with CRLF");
    }

    // ---- writeFilePart() via reflection ----

    @Test
    public void testWriteFilePartContainsFilename() throws Exception {
        Path tempFile = Files.createTempFile("uploader-test", ".pdf");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Files.writeString(tempFile, "PDF content");
            invokeWriteFilePart(baos, "boundary123", "report", tempFile.toFile());
            String result = baos.toString(StandardCharsets.UTF_8);
            Assert.assertTrue(result.contains(tempFile.toFile().getName()),
                    "should contain the filename");
            Assert.assertTrue(result.contains("Content-Disposition: form-data; name=\"report\""),
                    "should contain the part name");
            Assert.assertTrue(result.contains("PDF content"), "should contain file content");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testWriteFilePartContainsContentTypeHeader() throws Exception {
        Path tempFile = Files.createTempFile("uploader-ct-test", ".bin");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Files.write(tempFile, new byte[]{0x01, 0x02});
            invokeWriteFilePart(baos, "b", "f", tempFile.toFile());
            String result = baos.toString(StandardCharsets.UTF_8);
            Assert.assertTrue(result.contains("Content-Type: application/octet-stream"),
                    "should contain octet-stream content type");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // ---- reflection helpers ----

    private void invokeWriteStringPart(ByteArrayOutputStream out, String boundary,
                                       String name, String value) throws Exception {
        Method m = FileUploader.class.getDeclaredMethod(
                "writeStringPart", java.io.OutputStream.class, String.class, String.class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(null, out, boundary, name, value);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }

    private void invokeWriteFilePart(ByteArrayOutputStream out, String boundary,
                                     String name, File file) throws Exception {
        Method m = FileUploader.class.getDeclaredMethod(
                "writeFilePart", java.io.OutputStream.class, String.class, String.class, File.class);
        m.setAccessible(true);
        try {
            m.invoke(null, out, boundary, name, file);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }
}
