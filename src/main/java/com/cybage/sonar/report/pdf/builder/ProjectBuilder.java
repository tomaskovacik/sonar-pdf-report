package com.cybage.sonar.report.pdf.builder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.Components;
import org.sonarqube.ws.Projects;
import org.sonarqube.ws.client.HttpException;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.components.ShowRequest;
import org.sonarqube.ws.client.projects.SearchRequest;

import com.cybage.sonar.report.pdf.entity.FileInfo;
import com.cybage.sonar.report.pdf.entity.Issue;
import com.cybage.sonar.report.pdf.entity.Measures;
import com.cybage.sonar.report.pdf.entity.Project;
import com.cybage.sonar.report.pdf.entity.ProjectStatus;
import com.cybage.sonar.report.pdf.entity.QualityProfile;
import com.cybage.sonar.report.pdf.entity.Rule;
import com.cybage.sonar.report.pdf.entity.exception.ReportException;

public class ProjectBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectBuilder.class);

	private static ProjectBuilder builder;

	private WsClient wsClient;

	public ProjectBuilder(final WsClient wsClient) {
		this.wsClient = wsClient;
	}

	public static ProjectBuilder getInstance(final WsClient wsClient) {
		if (builder == null) {
			return new ProjectBuilder(wsClient);
		}

		return builder;
	}

	public Project initializeProject(final String key, final String version, final List<String> sonarLanguage,
			final Set<String> otherMetrics, final Set<String> typesOfIssue, final String branch) throws ReportException {
		Project project = new Project(key, version, sonarLanguage);

		try {
			LOGGER.info("Retrieving project info for {}", project.getKey());

			ShowRequest showWsReq = new ShowRequest();
			showWsReq.setComponent(project.getKey());
			if (branch != null && !branch.isEmpty()) {
				showWsReq.setBranch(branch);
			}

			String[] nameAndDesc = fetchProjectComponent(showWsReq, project.getKey());
			String projectName        = nameAndDesc[0];
			String projectDescription = nameAndDesc[1];

			if (projectName == null) {
				LOGGER.info("Can't retrieve project info. Have you set sonar.token in Sonar settings?");
				throw new ReportException("Can't retrieve project info. Parent project node is empty. Authentication?");
			}

			initFromNode(project, projectName, projectDescription, branch);
			initMeasures(project, otherMetrics, branch);
			initMostViolatedRules(project, branch);
			initMostViolatedFiles(project, branch);
			initMostComplexFiles(project, branch);
			initMostDuplicatedFiles(project, branch);
			if (!typesOfIssue.isEmpty()) {
				initIssueDetails(project, typesOfIssue, branch);
			}
		} catch (Exception ex) {
			LOGGER.error("Exception in initializeProject()", ex);
		}
		return project;
	}

	private String[] fetchProjectComponent(final ShowRequest showWsReq, final String projectKey) {
		try {
			Components.ShowWsResponse showWsRes = wsClient.components().show(showWsReq);
			if (showWsRes != null && showWsRes.hasComponent()) {
				return new String[]{showWsRes.getComponent().getName(), showWsRes.getComponent().getDescription()};
			}
			return new String[]{null, ""};
		} catch (HttpException e) {
			if (e.code() == 403) {
				LOGGER.warn("Insufficient privileges to call api/components/show for {} (HTTP 403). Falling back to api/projects/search.", projectKey);
				SearchRequest searchReq = new SearchRequest();
				searchReq.setProjects(Collections.singletonList(projectKey));
				Projects.SearchWsResponse searchRes = wsClient.projects().search(searchReq);
				if (searchRes.getComponentsCount() > 0) {
					return new String[]{searchRes.getComponents(0).getName(), ""};
				}
				return new String[]{null, ""};
			}
			throw e;
		}
	}

	private void initFromNode(final Project project, final String name, final String description,
			final String branch) {

		// Set Project Name
		project.setName(name);

		// Set Project Description
		project.setDescription(description);
		project.setSubprojects(new LinkedList<>());

		// Set Project Status
		initProjectStatus(project, branch);
		initQualityProfiles(project);

		project.setMostViolatedRules(new LinkedList<>());
		project.setMostComplexFiles(new LinkedList<>());
		project.setMostDuplicatedFiles(new LinkedList<>());
		project.setMostViolatedFiles(new LinkedList<>());
	}

	private void initMeasures(final Project project, final Set<String> otherMetrics, final String branch)
			throws ReportException {
		LOGGER.info("Retrieving measures");
		MeasuresBuilder measuresBuilder = MeasuresBuilder.getInstance(wsClient);
		Measures measures = measuresBuilder.initMeasuresByProjectKey(project.getKey(), otherMetrics, branch);
		project.setMeasures(measures);
	}

	private void initProjectStatus(final Project project, final String branch) {
		LOGGER.info("Retrieving project status");
		ProjectStatusBuilder projectStatusBuilder = ProjectStatusBuilder.getInstance(wsClient);
		ProjectStatus projectStatus = projectStatusBuilder.initProjectStatusByProjectKey(project.getKey(), branch);
		project.setProjectStatus(projectStatus);
	}

	private void initQualityProfiles(final Project project) {
		LOGGER.info("Retrieving quality profile information");
		QualityProfileBuilder qualityProfileBuilder = QualityProfileBuilder.getInstance(wsClient);
		List<QualityProfile> qualityProfiles = qualityProfileBuilder
				.initProjectQualityProfilesByProjectKey(project.getKey());
		project.setQualityProfiles(qualityProfiles);
	}

	private void initMostViolatedRules(final Project project, final String branch) {
		LOGGER.info("Retrieving most violated rules");
		RuleBuilder ruleBuilder = RuleBuilder.getInstance(wsClient);
		List<Rule> rules = ruleBuilder.initProjectMostViolatedRulesByProjectKey(project.getKey(), branch);
		project.setMostViolatedRules(rules);
	}

	private void initMostViolatedFiles(final Project project, final String branch) {
		LOGGER.info("Retrieving most violated files");
		FileInfoBuilder fileInfoBuilder = FileInfoBuilder.getInstance(wsClient);
		List<FileInfo> filesInfo = fileInfoBuilder.initProjectMostViolatedFilesByProjectKey(project.getKey(), branch);
		project.setMostViolatedFiles(filesInfo);
	}

	private void initMostComplexFiles(final Project project, final String branch) {
		LOGGER.info("Retrieving most complex files");
		FileInfoBuilder fileInfoBuilder = FileInfoBuilder.getInstance(wsClient);
		List<FileInfo> filesInfo = fileInfoBuilder.initProjectMostComplexFilesByProjectKey(project.getKey(), branch);
		project.setMostComplexFiles(filesInfo);
	}

	private void initMostDuplicatedFiles(final Project project, final String branch) {
		LOGGER.info("Retrieving most duplicated files");
		FileInfoBuilder fileInfoBuilder = FileInfoBuilder.getInstance(wsClient);
		List<FileInfo> filesInfo = fileInfoBuilder.initProjectMostDuplicatedFilesByProjectKey(project.getKey(), branch);
		project.setMostDuplicatedFiles(filesInfo);
	}

	private void initIssueDetails(final Project project, final Set<String> typesOfIssue, final String branch) {
		LOGGER.info("Retrieving issue details");
		IssueBuilder issueBuilder = IssueBuilder.getInstance(wsClient);
		List<Issue> issues = issueBuilder.initIssueDetailsByProjectKey(project.getKey(), typesOfIssue, branch);
		project.setIssues(issues);
	}
}
