package com.cybage.sonar.report.pdf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybage.sonar.report.pdf.entity.Project;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.events.PdfPageEventForwarder;

public class Header extends PdfPageEventForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(Header.class);

    private final URL     logo;
    private final Project project;

    public Header(final URL logo, final Project project) {
        this.logo = logo;
        this.project = project;
    }

    @Override
    public void onEndPage(final PdfWriter writer, final Document document) {
        try {
            Rectangle page = document.getPageSize();

            // Colored background bar across the full page width
            PdfPTable head = new PdfPTable(3);
            head.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            head.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            head.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            head.getDefaultCell().setBackgroundColor(Style.COLOR_DARK_NAVY);
            head.getDefaultCell().setPaddingTop(6f);
            head.getDefaultCell().setPaddingBottom(6f);
            head.getDefaultCell().setPaddingLeft(8f);
            head.getDefaultCell().setPaddingRight(8f);

            // Logo cell
            Image logoImage = Image.getInstance(logo);
            logoImage.scaleToFit(60, 30);
            PdfPCell logoCell = new PdfPCell(logoImage);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setBackgroundColor(Style.COLOR_DARK_NAVY);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            logoCell.setPaddingLeft(8f);
            logoCell.setPaddingTop(4f);
            logoCell.setPaddingBottom(4f);
            head.addCell(logoCell);

            // Centre: report title
            Phrase reportTitle = new Phrase("Sonar PDF Report",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, BaseColor.WHITE));
            head.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            head.addCell(reportTitle);

            // Right: project name
            Phrase projectName = new Phrase(project.getName(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, new BaseColor(200, 220, 240)));
            head.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            head.addCell(projectName);

            head.setTotalWidth(page.getWidth());
            head.writeSelectedRows(0, -1, 0, page.getHeight(), writer.getDirectContent());
        } catch (BadElementException | IOException e) {
            LOG.error("Can not generate PDF header", e);
        }
    }

}
