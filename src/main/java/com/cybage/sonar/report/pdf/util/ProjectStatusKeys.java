package com.cybage.sonar.report.pdf.util;

import com.cybage.sonar.report.pdf.Style;
import com.itextpdf.text.BaseColor;

public class ProjectStatusKeys {
    public static final String STATUS_OK    = "OK";
    public static final String STATUS_WARN  = "WARN";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_NONE  = "None";

    public static final String EQ = "=";
    public static final String NE = "!=";
    public static final String LT = "<";
    public static final String GT = ">";

    private ProjectStatusKeys() {
        // utility class
    }

    public static String getComparatorAsString(String comparator) {
        if ("EQ".equals(comparator)) {
            return EQ;
        } else if ("NE".equals(comparator)) {
            return NE;
        } else if ("LT".equals(comparator)) {
            return LT;
        } else if ("GT".equals(comparator)) {
            return GT;
        }
        return null;
    }

    public static String getStatusAsString(String status) {
        if (STATUS_OK.equals(status)) {
            return "Passed";
        } else if (STATUS_ERROR.equals(status)) {
            return "Failed";
        }
        return "Undefined!" + status;
    }

    public static BaseColor getStatusBaseColor(String status) {
        if (STATUS_OK.equals(status)) {
            return Style.QUALITY_GATE_PASSED_COLOR;
        } else if (STATUS_ERROR.equals(status)) {
            return Style.QUALITY_GATE_FAILED_COLOR;
        }
        return null;
    }

}
