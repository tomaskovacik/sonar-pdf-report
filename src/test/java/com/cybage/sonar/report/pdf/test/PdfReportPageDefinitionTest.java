package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.server.PdfReportPageDefinition;
import org.sonar.api.web.page.Context;
import org.sonar.api.web.page.Page;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;

@Test(groups = {"metrics"})
public class PdfReportPageDefinitionTest {

    @Test
    public void testDefineAddsPageWithCorrectKey() {
        PdfReportPageDefinition def = new PdfReportPageDefinition();
        Context context = new Context();

        def.define(context);

        Page page = getFirstPage(context);
        Assert.assertEquals(page.getKey(), "sonarpdfreport/report_page");
    }

    @Test
    public void testDefineAddsPageWithCorrectName() {
        PdfReportPageDefinition def = new PdfReportPageDefinition();
        Context context = new Context();

        def.define(context);

        Page page = getFirstPage(context);
        Assert.assertEquals(page.getName(), "PDF Report");
    }

    @Test
    public void testDefineAddsPageWithComponentScope() {
        PdfReportPageDefinition def = new PdfReportPageDefinition();
        Context context = new Context();

        def.define(context);

        Page page = getFirstPage(context);
        Assert.assertEquals(page.getScope(), Page.Scope.COMPONENT);
    }

    @Test
    public void testDefineAddsExactlyOnePage() {
        PdfReportPageDefinition def = new PdfReportPageDefinition();
        Context context = new Context();

        def.define(context);

        Assert.assertEquals(context.getPages().size(), 1);
    }

    private Page getFirstPage(Context context) {
        Collection<Page> pages = context.getPages();
        Assert.assertFalse(pages.isEmpty(), "expected at least one page to be registered");
        return pages.iterator().next();
    }
}
