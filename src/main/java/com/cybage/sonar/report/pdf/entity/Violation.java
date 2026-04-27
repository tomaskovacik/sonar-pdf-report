package com.cybage.sonar.report.pdf.entity;

public class Violation {

	private String resource;
	private String line;
	private String source;

	public Violation(final String line, final String resource, final String source) {
		this.line = line;
		this.resource = resource;
		this.source = source;
	}

	public String getResource() {
		return resource;
	}

	public String getLine() {
		return line;
	}

	public void setResource(final String resource) {
		this.resource = resource;
	}

	public void setLine(final String line) {
		this.line = line;
	}

	public String getSource() {
		return source;
	}

	public void setSource(final String source) {
		this.source = source;
	}

	public static String getViolationLevelByKey(final String level) {
		String violationLevel = null;
		if (Priority.INFO.equals(level)) {
			violationLevel = "info_violations";
		} else if (Priority.MINOR.equals(level)) {
			violationLevel = "minor_violations";
		} else if (Priority.MAJOR.equals(level)) {
			violationLevel = "major_violations";
		} else if (Priority.CRITICAL.equals(level)) {
			violationLevel = "critical_violations";
		} else if (Priority.BLOCKER.equals(level)) {
			violationLevel = "blocker_violations";
		}
		return violationLevel;
	}

}
