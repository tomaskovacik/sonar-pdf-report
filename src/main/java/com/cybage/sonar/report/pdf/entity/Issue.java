package com.cybage.sonar.report.pdf.entity;

public class Issue {

	// Component Name ( File Name / Directory Name / etc.. )
	private String component;
	
	// Component Name ( File Name / Directory Name / etc.. )
	private String componentPath;

	// Issue Severity ( Info / Minor / Major / Critical / Blocker )
	private String severity;

	// Line
	private Integer line;

	// Status of issue ( Open / Confirmed / Reopened / Resolved / Closed )
	private String status;

	// Message of issue
	private String message;

	// Type of issue ( Vulnerability / Bug / Code Smell )
	private String type;

	// Efforts needed to resolve issue
	private String effort;

	public Issue() {
		super();
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getComponentPath() {
		return componentPath;
	}

	public void setComponentPath(String componentPath) {
		this.componentPath = componentPath;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEffort() {
		return effort;
	}

	public void setEffort(String effort) {
		this.effort = effort;
	}

	@Override
	public String toString() {
		return "Issue [component=" + component + ", severity=" + severity + ", line=" + line + ", status=" + status
				+ ", message=" + message + ", type=" + type + ", effort=" + effort + "]";
	}

}
