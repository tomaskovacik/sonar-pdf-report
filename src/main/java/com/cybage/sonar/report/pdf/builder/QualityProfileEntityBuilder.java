package com.cybage.sonar.report.pdf.builder;

import com.cybage.sonar.report.pdf.entity.QualityProfile;

public class QualityProfileEntityBuilder {
    private String key;
    private String  name;
    private String  language;
    private String  languageName;
    private Boolean isInherited;
    private Boolean isDefault;
    private Long    activeRuleCount;
    private String  rulesUpdatedAt;
    private Long    projectCount;

    public QualityProfileEntityBuilder setKey(final String key) {
        this.key = key;
        return this;
    }

    public QualityProfileEntityBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public QualityProfileEntityBuilder setLanguage(final String language) {
        this.language = language;
        return this;
    }

    public QualityProfileEntityBuilder setLanguageName(final String languageName) {
        this.languageName = languageName;
        return this;
    }

    public QualityProfileEntityBuilder setIsInherited(final Boolean isInherited) {
        this.isInherited = isInherited;
        return this;
    }

    public QualityProfileEntityBuilder setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public QualityProfileEntityBuilder setActiveRuleCount(final Long activeRuleCount) {
        this.activeRuleCount = activeRuleCount;
        return this;
    }

    public QualityProfileEntityBuilder setRulesUpdatedAt(final String rulesUpdatedAt) {
        this.rulesUpdatedAt = rulesUpdatedAt;
        return this;
    }

    public QualityProfileEntityBuilder setProjectCount(final Long projectCount) {
        this.projectCount = projectCount;
        return this;
    }

    public QualityProfile createQualityProfile() {
        final QualityProfile result = new QualityProfile();
        result.setKey(key);
        result.setName(name);
        result.setLanguage(language);
        result.setLanguageName(languageName);
        result.setIsInherited(isInherited);
        result.setIsDefault(isDefault);
        result.setActiveRuleCount(activeRuleCount);
        result.setRulesUpdatedAt(rulesUpdatedAt);
        result.setProjectCount(projectCount);
        return result;
    }
}