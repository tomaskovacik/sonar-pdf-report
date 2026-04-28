package com.cybage.sonar.report.pdf.util;

import com.cybage.sonar.report.pdf.builder.IssueBuilder;
import com.cybage.sonar.report.pdf.entity.FileInfo;
import com.cybage.sonar.report.pdf.entity.Issue;
import com.cybage.sonar.report.pdf.entity.Project;
import com.cybage.sonar.report.pdf.entity.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SonarIssuesDumper {

    private static final Logger           LOGGER = LoggerFactory.getLogger(SonarIssuesDumper.class);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private SonarIssuesDumper() {}

    public static void dump(Project project, Credentials credentials, File outputDir) {
        File out = new File(outputDir, "sonar-issues.json");
        try {
            List<Issue> issues = resolveIssues(project, credentials);
            List<Rule>  rules  = deduplicateRules(project.getMostViolatedRules());
            String json = toJson(project, issues, rules);
            Files.writeString(out.toPath(), json, StandardCharsets.UTF_8);
            LOGGER.info("Sonar issues JSON written to {} ({} issues, {} rules)",
                    out.getAbsolutePath(), issues.size(), rules.size());
        } catch (IOException e) {
            LOGGER.warn("Could not write sonar-issues.json: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Issue fetching
    // -------------------------------------------------------------------------

    private static List<Issue> resolveIssues(Project project, Credentials credentials) {
        List<Issue> existing = project.getIssues();
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }
        // Issues were not fetched during normal report generation (typesOfIssue was empty).
        // Fetch all issue types now so the dump is always actionable.
        try {
            WsClient wsClient = buildWsClient(credentials);
            IssueBuilder ib = new IssueBuilder(wsClient);
            return ib.initIssueDetailsByProjectKey(project.getKey(), Collections.emptySet());
        } catch (Exception e) {
            LOGGER.warn("Could not fetch issues for JSON dump: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static WsClient buildWsClient(Credentials credentials) {
        HttpConnector connector = HttpConnector.newBuilder()
                .url(credentials.getUrl())
                .token(credentials.getToken())
                .build();
        return WsClientFactories.getDefault().newClient(connector);
    }

    // -------------------------------------------------------------------------
    // Rule deduplication — RuleBuilder queries every priority level including
    // Priority.ALL (""), which produces duplicate entries with severity="ALL".
    // Keep only entries with a specific severity (CRITICAL/MAJOR/MINOR/…).
    // -------------------------------------------------------------------------

    private static List<Rule> deduplicateRules(List<Rule> rules) {
        if (rules == null) return Collections.emptyList();
        Map<String, Rule> seen = new LinkedHashMap<>();
        for (Rule r : rules) {
            if ("ALL".equals(r.getSeverity())) continue;
            seen.putIfAbsent(r.getKey(), r);
        }
        return List.copyOf(seen.values());
    }

    // -------------------------------------------------------------------------
    // JSON serialisation
    // -------------------------------------------------------------------------

    private static String toJson(Project project, List<Issue> issues, List<Rule> rules) {
        StringBuilder sb = new StringBuilder(8192);
        sb.append("{\n");
        field(sb, 1, "project",     project.getKey());
        field(sb, 1, "name",        project.getName());
        field(sb, 1, "version",     project.getVersion());
        field(sb, 1, "generatedAt", LocalDateTime.now().format(TS_FMT));
        issuesArray(sb, issues);
        rulesArray(sb, rules);
        fileInfoArray(sb, "mostViolatedFiles",   project.getMostViolatedFiles(),   true);
        fileInfoArray(sb, "mostComplexFiles",    project.getMostComplexFiles(),    true);
        fileInfoArray(sb, "mostDuplicatedFiles", project.getMostDuplicatedFiles(), false);
        sb.append("}\n");
        return sb.toString();
    }

    private static void issuesArray(StringBuilder sb, List<Issue> issues) {
        sb.append("  \"issues\": ");
        if (issues.isEmpty()) { sb.append("[],\n"); return; }
        sb.append("[\n");
        for (int i = 0; i < issues.size(); i++) {
            Issue iss = issues.get(i);
            sb.append("    {\n");
            field(sb, 3, "severity",  iss.getSeverity());
            field(sb, 3, "type",      iss.getType());
            field(sb, 3, "component", iss.getComponent());
            field(sb, 3, "path",      iss.getComponentPath());
            intField(sb, 3, "line",   iss.getLine());
            field(sb, 3, "status",    iss.getStatus());
            field(sb, 3, "message",   iss.getMessage());
            lastField(sb, 3, "effort", iss.getEffort());
            sb.append("    }").append(i < issues.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ],\n");
    }

    private static void rulesArray(StringBuilder sb, List<Rule> rules) {
        sb.append("  \"rules\": ");
        if (rules.isEmpty()) { sb.append("[],\n"); return; }
        sb.append("[\n");
        for (int i = 0; i < rules.size(); i++) {
            Rule r = rules.get(i);
            sb.append("    {\n");
            field(sb, 3, "key",      r.getKey());
            field(sb, 3, "name",     r.getName());
            field(sb, 3, "language", r.getLanguageName());
            longField(sb, 3, "count", r.getCount());
            lastField(sb, 3, "severity", r.getSeverity());
            sb.append("    }").append(i < rules.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ],\n");
    }

    private static void fileInfoArray(StringBuilder sb, String key, List<FileInfo> files, boolean trailingComma) {
        sb.append("  \"").append(key).append("\": ");
        if (files == null || files.isEmpty()) {
            sb.append(trailingComma ? "[],\n" : "[]\n");
            return;
        }
        sb.append("[\n");
        for (int i = 0; i < files.size(); i++) {
            FileInfo fi = files.get(i);
            sb.append("    {\n");
            field(sb, 3, "name",           fi.getName());
            field(sb, 3, "path",           fi.getPath());
            field(sb, 3, "violations",     fi.getViolations());
            field(sb, 3, "complexity",     fi.getComplexity());
            lastField(sb, 3, "duplicatedLines", fi.getDuplicatedLines());
            sb.append("    }").append(i < files.size() - 1 ? "," : "").append("\n");
        }
        sb.append(trailingComma ? "  ],\n" : "  ]\n");
    }

    // -------------------------------------------------------------------------
    // Primitive field helpers  (indent level in units of 2 spaces)
    // -------------------------------------------------------------------------

    private static String indent(int level) {
        return "  ".repeat(level);
    }

    private static void field(StringBuilder sb, int level, String key, String value) {
        sb.append(indent(level)).append("\"").append(key).append("\": \"")
          .append(escape(value)).append("\",\n");
    }

    private static void lastField(StringBuilder sb, int level, String key, String value) {
        sb.append(indent(level)).append("\"").append(key).append("\": \"")
          .append(escape(value)).append("\"\n");
    }

    private static void intField(StringBuilder sb, int level, String key, Integer value) {
        sb.append(indent(level)).append("\"").append(key).append("\": ")
          .append(value == null ? "null" : value).append(",\n");
    }

    private static void longField(StringBuilder sb, int level, String key, Long value) {
        sb.append(indent(level)).append("\"").append(key).append("\": ")
          .append(value == null ? "null" : value).append(",\n");
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if      (c == '"')  out.append("\\\"");
            else if (c == '\\') out.append("\\\\");
            else if (c == '\n') out.append("\\n");
            else if (c == '\r') out.append("\\r");
            else if (c == '\t') out.append("\\t");
            else if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
            else                out.append(c);
        }
        return out.toString();
    }
}
