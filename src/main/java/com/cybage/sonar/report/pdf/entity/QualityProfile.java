package com.cybage.sonar.report.pdf.entity;

public class QualityProfile {

    private String  key;
    private String  name;
    private String  language;
    private String  languageName;
    private Boolean isInherited;
    private Boolean isDefault;
    private Long    activeRuleCount;
    private String  rulesUpdatedAt;
    private Long    projectCount;

    public QualityProfile() {
        super();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public Boolean getIsInherited() {
        return isInherited;
    }

    public void setIsInherited(Boolean isInherited) {
        this.isInherited = isInherited;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Long getActiveRuleCount() {
        return activeRuleCount;
    }

    public void setActiveRuleCount(Long activeRuleCount) {
        this.activeRuleCount = activeRuleCount;
    }

    public String getRulesUpdatedAt() {
        return rulesUpdatedAt;
    }

    public void setRulesUpdatedAt(String rulesUpdatedAt) {
        this.rulesUpdatedAt = rulesUpdatedAt;
    }

    public Long getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Long projectCount) {
        this.projectCount = projectCount;
    }

    @Override
    public String toString() {
        return "QualityProfile [key=" + key + ", name=" + name + ", language=" + language + ", languageName="
                + languageName + ", isInherited=" + isInherited + ", isDefault=" + isDefault + ", activeRuleCount="
                + activeRuleCount + ", rulesUpdatedAt=" + rulesUpdatedAt + ", projectCount=" + projectCount + "]";
    }

}