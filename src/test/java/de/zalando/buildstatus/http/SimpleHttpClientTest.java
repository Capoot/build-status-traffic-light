package de.zalando.buildstatus.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class SimpleHttpClientTest {

    private ClientAndServer mockServer;
    private final int port = 8081;
    private final String path = "/test";
    private final String url = "http://localhost:" + port + path;

    @Before
    public void setup() {
        mockServer = ClientAndServer.startClientAndServer(port);
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void sendRequestWithBasicAuth() {

        final String expectedResponse = "hello john";


        mockServer.when(
                request().withPath(path).withHeader("Authorization: Basic am9obmRvZTpwYXNzd29yZA==")).respond(
                response().withStatusCode(200).withBody(expectedResponse));

        String response = new SimpleHttpClient().sendGetWithBasicAuth(url, "johndoe",
                "password", false);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void ifAuthInfoIsNotSpecifiedAuthHeaderShouldNotBeAddedToRequest() {

        final String expectedResponse = "hello world";

        mockServer.when(
                request().withPath(path)).respond(
                response().withStatusCode(200).withBody(expectedResponse));

        new SimpleHttpClient().sendGetWithBasicAuth(url, null, null, false);

        HttpRequest[] actualRequests = mockServer.retrieveRecordedRequests(request().withPath(path));
        assertEquals(1, actualRequests.length);
        assertEquals("", actualRequests[0].getFirstHeader("Authorization"));
    }

    @Test
    public void ifStatusCodeIs301RedirectShouldBePerformed() {
        testRedirectWithStatusCode(301);
    }

    private void testRedirectWithStatusCode(int statusCode) {
        String initialPath = "/test";
        String requestUrl = "http://localhost:" + port + initialPath;
        String redirectedPath = "/redirect-target";
        String redirectTargetUrl = "http://localhost:" + port + redirectedPath;

        mockServer.when(
                request().withPath(initialPath)).respond(
                response().withStatusCode(statusCode).withHeader("Location", redirectTargetUrl));

        mockServer.when(
                request().withPath(redirectedPath)).respond(
                response().withStatusCode(200));

        new SimpleHttpClient().sendGetWithBasicAuth(requestUrl, null, null, false);

        mockServer.verify(request().withPath(redirectedPath));
    }

    @Test
    public void ifStatusCodeIs303RedirectShouldBePerformed() {
        testRedirectWithStatusCode(303);
    }

    @Test
    public void ifStatusCodeIs307RedirectShouldBePerformed() {
        testRedirectWithStatusCode(307);
    }
}
