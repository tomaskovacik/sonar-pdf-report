package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.Events;
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

import java.util.Collections;

import static org.mockito.Mockito.mock;

@Test(groups = {"metrics"})
public class EventsTest {

    private Toc   toc;
    private Header header;

    @BeforeMethod
    public void setUp() throws DocumentException {
        toc    = new Toc();
        header = new Header(null, new Project("test:key", "1.0", Collections.singletonList("java")));
    }

    @Test
    public void testConstructorSetsHeaderOnToc() throws DocumentException {
        Assert.assertNull(toc.getTocOutputStream(), "tocOutputStream should be null before Events constructor");
        new Events(toc, header);
        Assert.assertNotNull(toc.getTocOutputStream(), "tocOutputStream should be set after Events constructor calls toc.setHeader()");
    }

    @Test
    public void testOnChapterDelegatesToToc() throws DocumentException {
        Events events = new Events(toc, header);
        PdfWriter writer   = mock(PdfWriter.class);
        Document  document = mock(Document.class);
        // Should not throw — delegates to toc.onChapter()
        events.onChapter(writer, document, 0f, new Paragraph("Chapter"));
    }

    @Test
    public void testOnChapterEndDelegatesToToc() throws DocumentException {
        Events events = new Events(toc, header);
        PdfWriter writer   = mock(PdfWriter.class);
        Document  document = mock(Document.class);
        events.onChapterEnd(writer, document, 0f);
    }

    @Test
    public void testOnSectionDepthTwoDelegatesToToc() throws DocumentException {
        Events events = new Events(toc, header);
        PdfWriter writer   = mock(PdfWriter.class);
        Document  document = mock(Document.class);
        // depth == 2 exercises the if-branch in Toc.onSection()
        events.onSection(writer, document, 0f, 2, new Paragraph("Section"));
    }

    @Test
    public void testOnSectionDepthOtherDelegatesToToc() throws DocumentException {
        Events events = new Events(toc, header);
        PdfWriter writer   = mock(PdfWriter.class);
        Document  document = mock(Document.class);
        // depth != 2 exercises the else-branch in Toc.onSection()
        events.onSection(writer, document, 0f, 3, new Paragraph("Sub-section"));
    }

    @Test
    public void testOnCloseDocumentDelegatesToToc() throws DocumentException {
        Events events = new Events(toc, header);
        // setHeader (called inside Events constructor) attaches a PdfWriter to tocDoc,
        // so we can open it here to avoid the "document not open" error inside Toc.onCloseDocument
        toc.getTocDocument().open();
        PdfWriter writer   = mock(PdfWriter.class);
        Document  document = mock(Document.class);
        events.onCloseDocument(writer, document);
        // Do not close manually — iText closes the document itself via the PdfWriter
    }
}
