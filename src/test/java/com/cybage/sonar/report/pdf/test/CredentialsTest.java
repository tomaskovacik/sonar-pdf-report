package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.util.Credentials;

@Test(groups = { "metrics" })
public class CredentialsTest {

    @Test
    public void testConstructorAndGetters() {
        Credentials creds = new Credentials("http://localhost:9000", "mytoken123");
        Assert.assertEquals(creds.getUrl(), "http://localhost:9000");
        Assert.assertEquals(creds.getToken(), "mytoken123");
    }

    @Test
    public void testNullToken() {
        Credentials creds = new Credentials("http://sonar.example.com", null);
        Assert.assertEquals(creds.getUrl(), "http://sonar.example.com");
        Assert.assertNull(creds.getToken());
    }

    @Test
    public void testNullUrl() {
        Credentials creds = new Credentials(null, "token");
        Assert.assertNull(creds.getUrl());
        Assert.assertEquals(creds.getToken(), "token");
    }
}
