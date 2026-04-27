package com.cybage.sonar.report.pdf.entity;

/**
 * Priorities.
 */
public class Priority {

	public static final String INFO = "INFO";
	public static final String MINOR = "MINOR";
	public static final String MAJOR = "MAJOR";
	public static final String CRITICAL = "CRITICAL";
	public static final String BLOCKER = "BLOCKER";
	public static final String ALL = "";

	public static String[] getPrioritiesArray() {
		return new String[] { ALL, INFO, MINOR, MAJOR, CRITICAL, BLOCKER };
	}

	public static String getPriority(String priority) {
		if (Priority.ALL.equals(priority)) {
			return "ALL";
		} else {
			return priority;
		}
	}
}