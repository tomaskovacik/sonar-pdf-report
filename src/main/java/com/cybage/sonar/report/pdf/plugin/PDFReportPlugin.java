package com.cybage.sonar.report.pdf.plugin;

import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;

import com.cybage.sonar.report.pdf.batch.PDFPostJob;

@Properties({
		@Property(key = PDFPostJob.SKIP_PDF_KEY, name = "Skip", description = "Skip generation of PDF report.", defaultValue = ""
				+ PDFPostJob.SKIP_PDF_DEFAULT_VALUE, global = true, project = true, type = PropertyType.BOOLEAN),
		@Property(key = PDFPostJob.OTHER_METRICS, name = "Other Metrics", description = "Metrics list that you would like to include explicitly in your PDF report.", defaultValue = "", global = true, project = true, type = PropertyType.STRING, multiValues = true),
		@Property(key = PDFPostJob.LOGO, name = "Logo URL", description = "Logo to be displayed in your PDF report (an URL)", defaultValue = "", global = true, project = true, type = PropertyType.STRING, multiValues = true),
		@Property(key = PDFPostJob.TYPES_OF_ISSUE, name = "Issue Details", description = "Type of issues details that you would like to include explicitly in your PDF report. Allowed values are : ( BUG / CODE_SMELL / VULNERABILITY )", defaultValue = "NONE", global = true, project = true, type = PropertyType.STRING, multiValues = true) })

public class PDFReportPlugin implements Plugin {

	@Override
	public void define(Context context) {
		context.addExtension(PDFPostJob.class);
	}
}
