package com.cybage.sonar.report.pdf.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Project {

	// Project info
	private short id;
	private String key;
	private String name;
	private String version;
	private List<String> languages;
	private String description;
	private List<String> links;
	private ProjectStatus projectStatus;
	private List<QualityProfile> qualityProfiles;

	// Measures
	private Measures measures;

	// Child projects
	private List<Project> subprojects;

	// Most violated rules
	private List<Rule> mostViolatedRules;

	// Most complex elements
	private List<FileInfo> mostComplexFiles;

	// Most violated files
	private List<FileInfo> mostViolatedFiles;

	// Most duplicated files
	private List<FileInfo> mostDuplicatedFiles;

	// Issue Details
	private List<Issue> issues;

	public Project(final String key) {
		this.key = key;
	}

	public Project(final String key, final String version, final List<String> sonarLanguage) {
		this.key = key;
		this.version = version;
		this.languages = sonarLanguage;
	}

	public Measure getMeasure(final String measureKey) {
		if (measures != null && measures.containsMeasure(measureKey)) {
			return measures.getMeasure(measureKey);
		} else {
			return new Measure();
		}
	}

	public Optional<QualityProfile> getQualityProfileByLanguage(final String language) {
		return qualityProfiles.stream().filter(qp -> qp.getLanguage().equals(language)).findFirst();
	}

	public Project getChildByKey(final String key) {
		Iterator<Project> it = this.subprojects.iterator();
		while (it.hasNext()) {
			Project child = it.next();
			if (child.getKey().equals(key)) {
				return child;
			}
		}
		return null;
	}

	public void setId(final short id) {
		this.id = id;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setLinks(final List<String> links) {
		this.links = links;
	}

	public short getId() {
		return id;
	}

	public void setProjectStatus(final ProjectStatus projectStatus) {
		this.projectStatus = projectStatus;
	}

	public void setQualityProfiles(List<QualityProfile> qualityProfiles) {
		this.qualityProfiles = qualityProfiles;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getLinks() {
		return links;
	}

	public List<Project> getSubprojects() {
		return subprojects;
	}

	public void setSubprojects(final List<Project> subprojects) {
		this.subprojects = subprojects;
	}

	public ProjectStatus getProjectStatus() {
		return projectStatus;
	}

	public List<QualityProfile> getQualityProfiles() {
		return qualityProfiles;
	}

	public Measures getMeasures() {
		return measures;
	}

	public void setMeasures(final Measures measures) {
		this.measures = measures;
	}

	public List<Rule> getMostViolatedRules() {
		return mostViolatedRules;
	}

	public List<FileInfo> getMostViolatedFiles() {
		return mostViolatedFiles;
	}

	public void setMostViolatedRules(final List<Rule> mostViolatedRules) {
		this.mostViolatedRules = mostViolatedRules;
	}

	public void setMostViolatedFiles(final List<FileInfo> mostViolatedFiles) {
		this.mostViolatedFiles = mostViolatedFiles;
	}

	public void setMostComplexFiles(final List<FileInfo> mostComplexFiles) {
		this.mostComplexFiles = mostComplexFiles;
	}

	public List<FileInfo> getMostComplexFiles() {
		return mostComplexFiles;
	}

	public List<FileInfo> getMostDuplicatedFiles() {
		return mostDuplicatedFiles;
	}

	public void setMostDuplicatedFiles(final List<FileInfo> mostDuplicatedFiles) {
		this.mostDuplicatedFiles = mostDuplicatedFiles;
	}

	public List<Issue> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}

	@Override
	public String toString() {
		return "Project [id=" + id + ", key=" + key + ", name=" + name + ", version=" + version + ", languages="
				+ languages + ", description=" + description + ", links=" + links + ", projectStatus=" + projectStatus
				+ ", qualityProfiles=" + qualityProfiles + ", measures=" + measures + ", subprojects=" + subprojects
				+ ", mostViolatedRules=" + mostViolatedRules + ", mostComplexFiles=" + mostComplexFiles
				+ ", mostViolatedFiles=" + mostViolatedFiles + ", mostDuplicatedFiles=" + mostDuplicatedFiles
				+ ", issues=" + issues + "]";
	}

}
