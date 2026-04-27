package com.cybage.sonar.report.pdf.util;

public class SonarUtil {

	private SonarUtil() {
		// utility class
	}

	public static final int EIGHT_HOURS = 480;
	private static final Integer MINUTES_PER_HOUR = 60;

	public static String getWorkDurConversion(Integer minutes) {
		Integer hours = null;
		Integer days = null;

		// 1140
		if (minutes >= MINUTES_PER_HOUR && minutes < EIGHT_HOURS) {
			hours = minutes / MINUTES_PER_HOUR;
			minutes = minutes % MINUTES_PER_HOUR;
			return hours + "h " + minutes + "min";
		} else if (minutes >= EIGHT_HOURS) {
			days = (minutes / MINUTES_PER_HOUR) / 8;
			// minutes = minutes - (minutes * days);
			minutes = minutes % EIGHT_HOURS;
			hours = minutes / MINUTES_PER_HOUR;
			minutes = minutes % MINUTES_PER_HOUR;
			return days + "d " + hours + "h " + minutes + "min";
		} else {
			return minutes + "min";
		}
	}

	public static String getFormattedValue(Object value, String dataType) {
		switch (dataType) {
		case MetricDataTypes.WORKDUR:
			return getWorkDurConversion(Integer.parseInt(String.valueOf(value)));
		case MetricDataTypes.PERCENT:
			return String.valueOf(value) + "%";
		case MetricDataTypes.RATING:
			return Rating.getRating(String.valueOf(value));
		case MetricDataTypes.MILLISEC:
			return getWorkDurConversion((Integer.parseInt(String.valueOf(value)) / 1000) / 60);
		case MetricDataTypes.BOOL:
			return String.valueOf(value).equals("TRUE") ? "TRUE" : "FALSE";
		default:
			return String.valueOf(value);
		}
	}

}
