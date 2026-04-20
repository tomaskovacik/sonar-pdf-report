package com.cybage.sonar.report.pdf.util;

/**
 * Credentials holding the SonarQube host URL and a bearer token used for
 * authentication against the Web API.
 */
public class Credentials {

	private String url   = null;
	private String token = null;

	public Credentials(final String url, final String token) {
		this.url   = url;
		this.token = token;
	}

	/**
	 * @return the bearer token
	 */
	public String getToken() {
		return token;
	}

	public String getUrl() {
		return url;
	}
}
