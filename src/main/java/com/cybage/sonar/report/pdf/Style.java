package com.cybage.sonar.report.pdf;

import java.util.Iterator;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;

public class Style {

    private Style() {}

    // -------------------------------------------------------------------------
    // Modern color palette
    // -------------------------------------------------------------------------

    /** Dark navy used for chapter headings, table headers and page header bar */
    public static final BaseColor COLOR_DARK_NAVY      = new BaseColor(30, 50, 77);
    /** Medium blue used for section titles and accents */
    public static final BaseColor COLOR_ACCENT_BLUE    = new BaseColor(41, 128, 185);
    /** Near-black used for body text */
    public static final BaseColor COLOR_BODY_TEXT      = new BaseColor(44, 62, 80);
    /** Subtle gray used for secondary labels */
    public static final BaseColor COLOR_LABEL_GRAY     = new BaseColor(95, 99, 104);
    /** Very light gray for alternating table rows */
    public static final BaseColor COLOR_ROW_ALT        = new BaseColor(248, 249, 250);
    /** Light gray used for table borders */
    public static final BaseColor COLOR_BORDER         = new BaseColor(218, 220, 224);
    /** Modern green for "passed" / A-rating */
    public static final BaseColor COLOR_GREEN          = new BaseColor(39, 174, 96);
    /** Light green for B-rating */
    public static final BaseColor COLOR_LIGHT_GREEN    = new BaseColor(46, 204, 113);
    /** Amber for C-rating / warnings */
    public static final BaseColor COLOR_AMBER          = new BaseColor(241, 196, 15);
    /** Orange for D-rating */
    public static final BaseColor COLOR_ORANGE         = new BaseColor(230, 126, 34);
    /** Red for E-rating / errors */
    public static final BaseColor COLOR_RED            = new BaseColor(192, 57, 43);
    /** Light blue for "new code" metric highlight */
    public static final BaseColor COLOR_NEW_CODE_BG    = new BaseColor(213, 234, 246);

    // -------------------------------------------------------------------------
    // Fonts
    // -------------------------------------------------------------------------

    /**
     * Font used in main chapters title
     */
    public static final Font CHAPTER_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, COLOR_DARK_NAVY);

    /**
     * Font used in sub-chapters title
     */
    public static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, COLOR_DARK_NAVY);

    /**
     * Font used in graphics foots
     */
    public static final Font FOOT_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_LABEL_GRAY);

    /**
     * Font used in general plain text
     */
    public static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, COLOR_BODY_TEXT);

    /**
     * Font used in highlighted / warning plain text
     */
    public static final Font NORMAL_HIGHLIGHTED_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, COLOR_ORANGE);

    /**
     * Font used in code text (bold)
     */
    public static final Font MONOSPACED_BOLD_FONT = new Font(Font.FontFamily.COURIER, 11, Font.BOLD, COLOR_BODY_TEXT);

    /**
     * Font used in code text
     */
    public static final Font MONOSPACED_FONT = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL, COLOR_BODY_TEXT);

    /**
     * Font used in table of contents title
     */
    public static final Font TOC_TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, COLOR_DARK_NAVY);

    /**
     * Font used in front page (Project name)
     */
    public static final Font FRONTPAGE_FONT_1 = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE);

    /**
     * Font used in front page (Project description / version)
     */
    public static final Font FRONTPAGE_FONT_2 = new Font(Font.FontFamily.HELVETICA, 15, Font.NORMAL, BaseColor.WHITE);

    /**
     * Font used in front page (Project date / profile)
     */
    public static final Font FRONTPAGE_FONT_3 = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC,
            new BaseColor(200, 220, 240));

    /**
     * Section heading font (replaces underlined style with bold blue)
     */
    public static final Font UNDERLINED_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, COLOR_ACCENT_BLUE);

    /**
     * Dashboard metric label font
     */
    public static final Font DASHBOARD_TITLE_FONT = new Font(FontFamily.HELVETICA, 9, Font.BOLD, COLOR_LABEL_GRAY);

    /**
     * Dashboard metric value font (large KPI number)
     */
    public static final Font DASHBOARD_DATA_FONT = new Font(FontFamily.HELVETICA, 26, Font.BOLD, COLOR_BODY_TEXT);

    /**
     * Dashboard metric detail / secondary value font
     */
    public static final Font DASHBOARD_DATA_FONT_2 = new Font(FontFamily.HELVETICA, 9, Font.NORMAL, COLOR_BODY_TEXT);

    /**
     * Quality gate condition label font
     */
    public static final Font QUALITY_GATE_TITLE_FONT = new Font(FontFamily.HELVETICA, 9, Font.BOLD, COLOR_LABEL_GRAY);

    /**
     * Quality gate PASSED status font
     */
    public static final Font QUALITY_GATE_PASSED_FONT = new Font(FontFamily.HELVETICA, 11, Font.BOLD,
            BaseColor.WHITE);

    /**
     * Quality gate PASSED secondary font
     */
    public static final Font QUALITY_GATE_PASSED_FONT_2 = new Font(FontFamily.HELVETICA, 11, Font.NORMAL,
            BaseColor.WHITE);

    /**
     * Quality gate ERROR status font
     */
    public static final Font QUALITY_GATE_FAILED_FONT = new Font(FontFamily.HELVETICA, 11, Font.BOLD,
            BaseColor.WHITE);

    /**
     * Quality gate ERROR secondary font
     */
    public static final Font QUALITY_GATE_FAILED_FONT_2 = new Font(FontFamily.HELVETICA, 11, Font.NORMAL,
            BaseColor.WHITE);

    /**
     * Rating A font (green)
     */
    public static final Font DASHBOARD_RATING_FONT_A = new Font(FontFamily.HELVETICA, 26, Font.BOLD, COLOR_GREEN);

    /**
     * Rating B font (light green)
     */
    public static final Font DASHBOARD_RATING_FONT_B = new Font(FontFamily.HELVETICA, 26, Font.BOLD, COLOR_LIGHT_GREEN);

    /**
     * Rating C font (amber)
     */
    public static final Font DASHBOARD_RATING_FONT_C = new Font(FontFamily.HELVETICA, 26, Font.BOLD, COLOR_AMBER);

    /**
     * Rating D font (orange)
     */
    public static final Font DASHBOARD_RATING_FONT_D = new Font(FontFamily.HELVETICA, 26, Font.BOLD, COLOR_ORANGE);

    /**
     * Rating E font (red)
     */
    public static final Font DASHBOARD_RATING_FONT_E = new Font(FontFamily.HELVETICA, 26, Font.BOLD, COLOR_RED);

    /**
     * Background color for "new code" metric cells
     */
    public static final BaseColor DASHBOARD_NEW_METRIC_BACKGROUND_COLOR = COLOR_NEW_CODE_BG;

    /** Quality gate PASSED background */
    public static final BaseColor QUALITY_GATE_PASSED_COLOR = COLOR_GREEN;

    /** Quality gate ERROR background */
    public static final BaseColor QUALITY_GATE_FAILED_COLOR = COLOR_RED;

    /** Table column-header background (dark navy) */
    public static final BaseColor TABLE_HEADER_BACKGROUND_COLOR = COLOR_DARK_NAVY;

    /** Table column-header font (white Helvetica bold) */
    public static final Font TABLE_HEADER_FONT = new Font(FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);

    /** Alternating table row background */
    public static final BaseColor TABLE_ROW_ALT_BACKGROUND_COLOR = COLOR_ROW_ALT;

    public static final Integer TABLE_SUBMETRIC_WIDTH_PERCENTAGE = 95;

    public static final Float TABLE_MAINMETRIC_WIDTH_PERCENTAGE = 95.5F;

    /**
     * Tendency icons height + 2 (used in tables style)
     */
    public static final int TENDENCY_ICONS_HEIGHT = 20;

    public static final float FRONTPAGE_LOGO_POSITION_X = 114;

    public static final float FRONTPAGE_LOGO_POSITION_Y      = 492;
    public static final Font  DASHBOARD_DATA_FILEPATH_FONT_2 = new Font(FontFamily.HELVETICA, 7, Font.NORMAL,
            COLOR_LABEL_GRAY);

    public static void noBorderTable(final PdfPTable table) {
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);
    }

    /**
     * This method makes a simple table with content.
     *
     * @param left   Data for left column
     * @param right  Data for right column
     * @param title  The table title
     * @param noData Showed when left or right are empty
     * @return The table (iText table) ready to add to the document
     */
    public static PdfPTable createSimpleTable(final List<String> left, final List<String> right, final String title,
                                              final String noData) {
        PdfPTable table = new PdfPTable(2);
        table.getDefaultCell().setColspan(2);
        table.addCell(new Phrase(title, Style.DASHBOARD_TITLE_FONT));
        table.getDefaultCell().setBackgroundColor(BaseColor.GRAY);
        table.addCell("");
        table.getDefaultCell().setColspan(1);
        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);

        Iterator<String> itLeft  = left.iterator();
        Iterator<String> itRight = right.iterator();

        while (itLeft.hasNext()) {
            String textLeft  = itLeft.next();
            String textRight = itRight.next();
            table.addCell(textLeft);
            table.addCell(textRight);
        }

        if (left.isEmpty()) {
            table.getDefaultCell().setColspan(2);
            table.addCell(noData);
        }

        table.setSpacingBefore(20);
        table.setSpacingAfter(20);

        return table;
    }

    public static PdfPTable createTwoColumnsTitledTable(final List<String> titles, final List<String> content) {
        PdfPTable        table   = new PdfPTable(10);
        Iterator<String> itLeft  = titles.iterator();
        Iterator<String> itRight = content.iterator();
        while (itLeft.hasNext()) {
            String textLeft  = itLeft.next();
            String textRight = itRight.next();
            table.getDefaultCell().setColspan(1);
            table.addCell(textLeft);
            table.getDefaultCell().setColspan(9);
            table.addCell(textRight);
        }
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);
        table.setLockedWidth(false);
        table.setWidthPercentage(90);
        return table;
    }
}
