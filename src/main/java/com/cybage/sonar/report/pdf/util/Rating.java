package com.cybage.sonar.report.pdf.util;

import java.util.HashMap;
import java.util.Map;

import com.cybage.sonar.report.pdf.Style;
import com.itextpdf.text.Font;

public class Rating {

    private Rating() {
        // utility class
    }

    public static final String RATING_1 = "1.0";
    public static final String RATING_2 = "2.0";
    public static final String RATING_3 = "3.0";
    public static final String RATING_4 = "4.0";
    public static final  String              RATING_5 = "5.0";
    private static final Map<String, String> ratings;

    static {
        ratings = new HashMap<>();
        ratings.put(RATING_1, "A");
        ratings.put(RATING_2, "B");
        ratings.put(RATING_3, "C");
        ratings.put(RATING_4, "D");
        ratings.put(RATING_5, "E");
    }

    public static String getRating(String rating) {
        return ratings.get(rating);
    }

    public static Font getRatingStyle(String rating) {
        if (rating.equals(RATING_1)) {
            return Style.DASHBOARD_RATING_FONT_A;
        } else if (rating.equals(RATING_2)) {
            return Style.DASHBOARD_RATING_FONT_B;
        } else if (rating.equals(RATING_3)) {
            return Style.DASHBOARD_RATING_FONT_C;
        } else if (rating.equals(RATING_4)) {
            return Style.DASHBOARD_RATING_FONT_D;
        } else if (rating.equals(RATING_5)) {
            return Style.DASHBOARD_RATING_FONT_E;
        }
        return null;
    }
}
