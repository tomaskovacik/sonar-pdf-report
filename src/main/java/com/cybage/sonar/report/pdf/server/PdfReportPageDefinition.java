package com.cybage.sonar.report.pdf.server;

import org.sonar.api.web.page.Context;
import org.sonar.api.web.page.Page;
import org.sonar.api.web.page.PageDefinition;

public class PdfReportPageDefinition implements PageDefinition {

    @Override
    public void define(Context context) {
        context.addPage(Page.builder("sonar-pdf-report/report_page")
                .setName("PDF Report")
                .setScope(Page.Scope.COMPONENT)
                .build());
    }
}
