package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.entity.Issue;
import com.cybage.sonar.report.pdf.entity.QualityProfile;

/**
 * Test-only fixture factory. Issue/QualityProfile only expose a no-arg constructor + setters
 * in production code (to keep their constructors under Sonar's parameter-count limit); this
 * keeps test call sites as terse as the old all-args constructors were.
 */
final class TestEntities {

    private TestEntities() {
    }

    static Issue issue(String component, String componentPath, String severity, Integer line, String status,
                        String message, String type, String effort) {
        Issue issue = new Issue();
        issue.setComponent(component);
        issue.setComponentPath(componentPath);
        issue.setSeverity(severity);
        issue.setLine(line);
        issue.setStatus(status);
        issue.setMessage(message);
        issue.setType(type);
        issue.setEffort(effort);
        return issue;
    }

    static QualityProfile qualityProfile(String key, String name, String language, String languageName,
                                          Boolean isInherited, Boolean isDefault, Long activeRuleCount,
                                          String rulesUpdatedAt, Long projectCount) {
        QualityProfile qp = new QualityProfile();
        qp.setKey(key);
        qp.setName(name);
        qp.setLanguage(language);
        qp.setLanguageName(languageName);
        qp.setIsInherited(isInherited);
        qp.setIsDefault(isDefault);
        qp.setActiveRuleCount(activeRuleCount);
        qp.setRulesUpdatedAt(rulesUpdatedAt);
        qp.setProjectCount(projectCount);
        return qp;
    }
}
