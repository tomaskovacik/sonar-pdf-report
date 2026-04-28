package com.cybage.sonar.report.pdf.builder;

import com.cybage.sonar.report.pdf.entity.Condition;
import com.cybage.sonar.report.pdf.entity.ProjectStatus;
import com.cybage.sonar.report.pdf.entity.StatusPeriod;
import org.sonarqube.ws.Qualitygates;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.qualitygates.ProjectStatusRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectStatusBuilder {

    /**
     * The new-code period index used in SonarQube 10.x+. There is only one new-code period,
     * consistently represented with index 1 for backward compatibility with the entity model.
     */
    private static final int NEW_CODE_PERIOD_INDEX = 1;

    private static ProjectStatusBuilder builder;

    private final WsClient wsClient;

    public ProjectStatusBuilder(final WsClient wsClient) {
        this.wsClient = wsClient;
    }

    public static ProjectStatusBuilder getInstance(final WsClient wsClient) {
        if (builder == null) {
            return new ProjectStatusBuilder(wsClient);
        }

        return builder;
    }

    public ProjectStatus initProjectStatusByProjectKey(final String key, final String branch) {

        ProjectStatusRequest projectStatusWsReq = new ProjectStatusRequest();
        projectStatusWsReq.setProjectKey(key);
        if (branch != null && !branch.isEmpty()) {
            projectStatusWsReq.setBranch(branch);
        }
        Qualitygates.ProjectStatusResponse projectStatusWsRes = wsClient.qualitygates().projectStatus(projectStatusWsReq);

        List<Condition>    conditions    = initProjectConditions(projectStatusWsRes);
        List<StatusPeriod> statusPeriods = initStatusPeriods(projectStatusWsRes);
        return new ProjectStatus(projectStatusWsRes.getProjectStatus().getStatus().toString(), conditions,
                statusPeriods);

    }

    private List<Condition> initProjectConditions(final Qualitygates.ProjectStatusResponse projectStatusWsRes) {
        List<Condition> conditions = new ArrayList<>();
        for (Qualitygates.ProjectStatusResponse.Condition condition : projectStatusWsRes.getProjectStatus().getConditionsList()) {
            Condition cond = new ConditionBuilder()
                    .setStatus(condition.getStatus().toString())
                    .setMetricKey(condition.getMetricKey())
                    .setComparator(condition.getComparator().toString())
                    .setErrorThreshold(condition.getErrorThreshold())
                    .setActualValue(condition.getActualValue())
                    .setWarningThreshold(condition.getWarningThreshold())
                    .createCondition();
            conditions.add(cond);
        }
        return conditions;
    }

    private List<StatusPeriod> initStatusPeriods(final Qualitygates.ProjectStatusResponse projectStatusWsRes) {
        // SonarQube 10.x+ replaced the list of periods with a single new-code period.
        Qualitygates.ProjectStatusResponse.ProjectStatus projectStatus = projectStatusWsRes.getProjectStatus();
        if (!projectStatus.hasPeriod()) {
            return Collections.emptyList();
        }
        Qualitygates.ProjectStatusResponse.NewCodePeriod period = projectStatus.getPeriod();
        StatusPeriod statusPeriod = new StatusPeriodBuilder()
                .setIndex(NEW_CODE_PERIOD_INDEX)
                .setMode(period.getMode())
                .createStatusPeriod();
        if (period.hasDate()) {
            statusPeriod.setDate(period.getDate());
        }
        if (period.hasParameter()) {
            statusPeriod.setParameter(period.getParameter());
        }
        return Collections.singletonList(statusPeriod);
    }
}
