package com.cybage.sonar.report.pdf.design;

import com.cybage.sonar.report.pdf.Style;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

public class CustomCellValue extends PdfPCell {

	public CustomCellValue(Phrase phrase) {
		super(phrase);
		this.setVerticalAlignment(Element.ALIGN_MIDDLE);
		this.setHorizontalAlignment(ALIGN_RIGHT);
		this.setPaddingTop(6f);
		this.setPaddingBottom(6f);
		this.setPaddingLeft(8f);
		this.setPaddingRight(8f);
		this.setBorderColor(Style.COLOR_BORDER);
		this.setBorderWidth(0.5f);
	}
}
