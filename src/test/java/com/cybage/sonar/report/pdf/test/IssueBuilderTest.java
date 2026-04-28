package com.cybage.sonar.report.pdf.test;

import com.cybage.sonar.report.pdf.builder.IssueBuilder;
import com.cybage.sonar.report.pdf.entity.Issue;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.issues.IssuesService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"metrics"})
public class IssueBuilderTest {

    private WsClient mockWsClient;
    private IssuesService mockIssuesService;

    @BeforeMethod
    public void setUp() throws Exception {
        mockWsClient = mock(WsClient.class);
        mockIssuesService = mock(IssuesService.class);
        when(mockWsClient.issues()).thenReturn(mockIssuesService);

        // Reset the static builder field before each test so getInstance() always creates a fresh one
        Field builderField = IssueBuilder.class.getDeclaredField("builder");
        builderField.setAccessible(true);
        builderField.set(null, null);
    }

    // -------------------------------------------------------------------------
    // getInstance()
    // -------------------------------------------------------------------------

    @Test
    public void testGetInstanceReturnsNewIssueBuilderWhenBuilderFieldIsNull() {
        IssueBuilder instance = IssueBuilder.getInstance(mockWsClient);
        Assert.assertNotNull(instance, "getInstance() should return a non-null IssueBuilder");
    }

    @Test
    public void testGetInstanceReturnsDifferentInstancesWhenBuilderFieldIsNull() {
        IssueBuilder first  = IssueBuilder.getInstance(mockWsClient);
        IssueBuilder second = IssueBuilder.getInstance(mockWsClient);
        // Both should be non-null (both calls see builder == null, so both create new instances)
        Assert.assertNotNull(first);
        Assert.assertNotNull(second);
    }

    // -------------------------------------------------------------------------
    // convertTypes() via reflection
    // -------------------------------------------------------------------------

    @Test
    public void testConvertTypesUpperCasesValues() throws Exception {
        IssueBuilder ib = new IssueBuilder(mockWsClient);
        Set<String> input = new HashSet<>(Arrays.asList("bug", "code_smell"));

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokeConvertTypes(ib, input);

        Assert.assertTrue(result.contains("BUG"), "should contain BUG");
        Assert.assertTrue(result.contains("CODE_SMELL"), "should contain CODE_SMELL");
        Assert.assertEquals(result.size(), 2);
    }

    @Test
    public void testConvertTypesEmptySetReturnsEmptyList() throws Exception {
        IssueBuilder ib = new IssueBuilder(mockWsClient);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokeConvertTypes(ib, Collections.emptySet());

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // initIssueDetailsByProjectKey() – empty typesOfIssue, total == 0
    // -------------------------------------------------------------------------

    @Test
    public void testInitIssueDetailsEmptyTypesAndZeroTotalReturnsEmptyList() {
        Issues.SearchWsResponse emptyResponse = Issues.SearchWsResponse.newBuilder()
                .setPaging(Common.Paging.newBuilder().setTotal(0).build())
                .build();
        when(mockIssuesService.search(any())).thenReturn(emptyResponse);

        IssueBuilder ib = new IssueBuilder(mockWsClient);
        List<Issue> result = ib.initIssueDetailsByProjectKey("test:project", Collections.emptySet());

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty(), "should return empty list when total is 0");
    }

    // -------------------------------------------------------------------------
    // initIssueDetailsByProjectKey() – one page, one issue
    // -------------------------------------------------------------------------

    @Test
    public void testInitIssueDetailsOnePage() {
        Issues.Component component = Issues.Component.newBuilder()
                .setKey("test:project:src/MyFile.java")
                .setName("MyFile.java")
                .setLongName("src/MyFile.java")
                .build();

        Issues.Issue protoIssue = Issues.Issue.newBuilder()
                .setComponent("test:project:src/MyFile.java")
                .setSeverity(Common.Severity.MAJOR)
                .setType(Common.RuleType.BUG)
                .setLine(42)
                .setStatus("OPEN")
                .setMessage("Null pointer dereference")
                .setEffort("5min")
                .build();

        Issues.SearchWsResponse singlePageResponse = Issues.SearchWsResponse.newBuilder()
                .setPaging(Common.Paging.newBuilder().setTotal(1).build())
                .addIssues(protoIssue)
                .addComponents(component)
                .build();

        when(mockIssuesService.search(any())).thenReturn(singlePageResponse);

        IssueBuilder ib = new IssueBuilder(mockWsClient);
        Set<String> typesOfIssue = Collections.singleton("BUG");
        List<Issue> result = ib.initIssueDetailsByProjectKey("test:project", typesOfIssue);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1, "should return exactly one issue");
        Issue issue = result.get(0);
        Assert.assertEquals(issue.getComponent(), "MyFile.java");
        Assert.assertEquals(issue.getComponentPath(), "src/MyFile.java");
        Assert.assertEquals(issue.getSeverity(), "MAJOR");
        Assert.assertEquals(issue.getLine(), Integer.valueOf(42));
        Assert.assertEquals(issue.getStatus(), "OPEN");
        Assert.assertEquals(issue.getMessage(), "Null pointer dereference");
        Assert.assertEquals(issue.getType(), "BUG");
        Assert.assertEquals(issue.getEffort(), "5min");
    }

    // -------------------------------------------------------------------------
    // initIssueDetailsByProjectKey() – two pages (total=600, page 1 has 500, page 2 has 100)
    // -------------------------------------------------------------------------

    @Test
    public void testInitIssueDetailsTwoPages() {
        // Build the component used for all issues
        Issues.Component component = Issues.Component.newBuilder()
                .setKey("test:project:src/Foo.java")
                .setName("Foo.java")
                .setLongName("src/Foo.java")
                .build();

        // Build page 1: 500 issues, total = 600
        Issues.SearchWsResponse.Builder page1Builder = Issues.SearchWsResponse.newBuilder()
                .setPaging(Common.Paging.newBuilder().setTotal(600).build())
                .addComponents(component);
        for (int i = 0; i < 500; i++) {
            page1Builder.addIssues(
                    Issues.Issue.newBuilder()
                            .setComponent("test:project:src/Foo.java")
                            .setSeverity(Common.Severity.MINOR)
                            .setType(Common.RuleType.CODE_SMELL)
                            .setLine(i + 1)
                            .setStatus("OPEN")
                            .setMessage("issue " + i)
                            .setEffort("1min")
                            .build());
        }
        Issues.SearchWsResponse page1 = page1Builder.build();

        // Build page 2: 100 issues, total = 600
        Issues.SearchWsResponse.Builder page2Builder = Issues.SearchWsResponse.newBuilder()
                .setPaging(Common.Paging.newBuilder().setTotal(600).build())
                .addComponents(component);
        for (int i = 0; i < 100; i++) {
            page2Builder.addIssues(
                    Issues.Issue.newBuilder()
                            .setComponent("test:project:src/Foo.java")
                            .setSeverity(Common.Severity.MINOR)
                            .setType(Common.RuleType.CODE_SMELL)
                            .setLine(500 + i + 1)
                            .setStatus("OPEN")
                            .setMessage("issue " + (500 + i))
                            .setEffort("1min")
                            .build());
        }
        Issues.SearchWsResponse page2 = page2Builder.build();

        when(mockIssuesService.search(any()))
                .thenReturn(page1)
                .thenReturn(page2);

        IssueBuilder ib = new IssueBuilder(mockWsClient);
        List<Issue> result = ib.initIssueDetailsByProjectKey("test:project", Collections.singleton("CODE_SMELL"));

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 600, "should return 600 issues across two pages");
    }

    // -------------------------------------------------------------------------
    // Reflection helpers
    // -------------------------------------------------------------------------

    private Object invokeConvertTypes(IssueBuilder ib, Set<String> typesOfIssue) throws Exception {
        Method m = IssueBuilder.class.getDeclaredMethod("convertTypes", Set.class);
        m.setAccessible(true);
        try {
            return m.invoke(ib, typesOfIssue);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
            throw e;
        }
    }
}
