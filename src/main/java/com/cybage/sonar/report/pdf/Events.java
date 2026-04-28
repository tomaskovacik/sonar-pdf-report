package com.cybage.sonar.report.pdf;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Add the logo header to the PDF document.
 */
public class Events extends PdfPageEventHelper {

	private static final Logger LOG = LoggerFactory.getLogger(Events.class);

	private Toc toc;
	private Header header;

	public Events(final Toc toc, final Header header) {
		this.toc = toc;
		this.header = header;
		toc.setHeader(header);
	}

	@Override
	public void onChapter(final PdfWriter writer, final Document document, final float position,
			final Paragraph paragraph) {
		toc.onChapter(writer, document, position, paragraph);
	}

	@Override
	public void onChapterEnd(final PdfWriter writer, final Document document, final float position) {
		toc.onChapterEnd(writer, document, position);
	}

	@Override
	public void onSection(final PdfWriter writer, final Document document, final float position, final int depth,
			final Paragraph paragraph) {
		toc.onSection(writer, document, position, depth, paragraph);
	}

	@Override
	public void onEndPage(final PdfWriter writer, final Document document) {
		header.onEndPage(writer, document);
		printPageNumber(writer, document);
	}

	@Override
	public void onCloseDocument(final PdfWriter writer, final Document document) {
		toc.onCloseDocument(writer, document);
	}

	private void printPageNumber(final PdfWriter writer, final Document document) {
		PdfContentByte cb = writer.getDirectContent();
		cb.saveState();
		float textBase = document.bottom() - 20;
		try {
			cb.setFontAndSize(BaseFont.createFont("Helvetica", BaseFont.WINANSI, false), 12);
		} catch (DocumentException | IOException e) {
			LOG.error("Can not print page number", e);
		}
		cb.beginText();
		cb.setTextMatrix(document.right() - 10, textBase);
		cb.showText(String.valueOf(writer.getPageNumber()));
		cb.endText();
		cb.restoreState();

	}
}
