package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.Header;
import com.cybage.sonar.report.pdf.Toc;
import com.cybage.sonar.report.pdf.entity.Project;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.mockito.Mockito.mock;

@Test(groups = {"metrics"})
public class TocTest {

    private Toc toc;

    @BeforeMethod
    public void setUp() throws DocumentException {
        toc = new Toc();
    }

    @Test
    public void testConstructorCreatesTocDocument() throws DocumentException {
        Toc t = new Toc();
        Assert.assertNotNull(t.getTocDocument(), "getTocDocument() should return non-null after construction");
    }

    @Test
    public void testTocOutputStreamIsNullBeforeSetHeader() {
        Assert.assertNull(toc.getTocOutputStream(),
                "getTocOutputStream() should be null before setHeader() is called");
    }

    @Test
    public void testSetHeaderMakesTocOutputStreamNonNull() {
        Header header = createRealHeader();
        toc.setHeader(header);
        Assert.assertNotNull(toc.getTocOutputStream(),
                "getTocOutputStream() should be non-null after setHeader()");
    }

    @Test
    public void testSetHeaderOutputStreamIsInstance() {
        Header header = createRealHeader();
        toc.setHeader(header);
        Assert.assertTrue(toc.getTocOutputStream() instanceof ByteArrayOutputStream,
                "getTocOutputStream() should return a ByteArrayOutputStream");
    }

    @Test
    public void testOnSectionDepthTwoDoesNotThrow() {
        PdfWriter writer = mock(PdfWriter.class);
        Document document = mock(Document.class);
        Paragraph title = new Paragraph("Section Title");

        // Should not throw
        toc.onSection(writer, document, 100f, 2, title);
    }

    @Test
    public void testOnSectionDepthThreeDoesNotThrow() {
        PdfWriter writer = mock(PdfWriter.class);
        Document document = mock(Document.class);
        Paragraph title = new Paragraph("Sub-section Title");

        // depth != 2, goes into the else branch
        toc.onSection(writer, document, 100f, 3, title);
    }

    @Test
    public void testOnChapterDoesNotThrow() {
        PdfWriter writer = mock(PdfWriter.class);
        Document document = mock(Document.class);
        Paragraph title = new Paragraph("Chapter Title");

        toc.onChapter(writer, document, 0f, title);
    }

    @Test
    public void testOnChapterEndDoesNotThrow() {
        PdfWriter writer = mock(PdfWriter.class);
        Document document = mock(Document.class);

        toc.onChapterEnd(writer, document, 0f);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a real {@link Header} with a null logo URL and a minimal {@link Project}.
     * The header is passed to {@link Toc#setHeader(Header)}; we only need it to be
     * a valid PdfPageEventHelper instance — the logo URL is only used inside
     * onEndPage(), which is not called in these unit tests.
     */
    private Header createRealHeader() {
        Project project = new Project("test:key", "1.0", Collections.singletonList("java"));
        project.setName("Test Project");
        return new Header(null, project);
    }
}
