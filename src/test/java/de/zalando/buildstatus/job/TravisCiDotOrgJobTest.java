package de.zalando.buildstatus.job;

import de.zalando.buildstatus.http.SimpleHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TravisCiDotOrgJobTest {

    private final String passingBuildSvgXml = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"90\" height=\"20\">" +
            "<linearGradient id=\"a\" x2=\"0\" y2=\"100%\"><stop offset=\"0\" stop-color=\"#bbb\" " +
            "stop-opacity=\".1\"/><stop offset=\"1\" stop-opacity=\".1\"/></linearGradient><rect rx=\"3\" " +
            "width=\"90\" height=\"20\" fill=\"#555\"/><rect rx=\"3\" x=\"37\" width=\"53\" height=\"20\" " +
            "fill=\"#4c1\"/><path fill=\"#4c1\" d=\"M37 0h4v20h-4z\"/><rect rx=\"3\" width=\"90\" height=\"20\" " +
            "fill=\"url(#a)\"/><g fill=\"#fff\" text-anchor=\"middle\" " +
            "font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"19.5\" y=\"15\" " +
            "fill=\"#010101\" fill-opacity=\".3\">build</text><text x=\"19.5\" y=\"14\">build</text><text x=\"62.5\" " +
            "y=\"15\" fill=\"#010101\" fill-opacity=\".3\">passing</text><text x=\"62.5\" y=\"14\">passing</text>" +
            "</g></svg>";

    private final String failingBuildSvgXml = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"76\" height=\"20\">" +
            "<linearGradient id=\"a\" x2=\"0\" y2=\"100%\"><stop offset=\"0\" stop-color=\"#bbb\" " +
            "stop-opacity=\".1\"/><stop offset=\"1\" stop-opacity=\".1\"/></linearGradient><rect rx=\"3\" " +
            "width=\"76\" height=\"20\" fill=\"#555\"/><rect rx=\"3\" x=\"37\" width=\"39\" height=\"20\" " +
            "fill=\"#9f9f9f\"/><path fill=\"#9f9f9f\" d=\"M37 0h4v20h-4z\"/><rect rx=\"3\" width=\"76\" " +
            "height=\"20\" fill=\"url(#a)\"/><g fill=\"#fff\" text-anchor=\"middle\" " +
            "font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"19.5\" y=\"15\" " +
            "fill=\"#010101\" fill-opacity=\".3\">build</text><text x=\"19.5\" y=\"14\">build</text><text x=\"55.5\" " +
            "y=\"15\" fill=\"#010101\" fill-opacity=\".3\">error</text><text x=\"55.5\" y=\"14\">error</text></g>" +
            "</svg>";

    @Mock private SimpleHttpClient simpleHttpClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void apiRequestsShouldConstructTheCorrectUrl() {

        when(simpleHttpClient.sendGetWithBasicAuth(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(passingBuildSvgXml);

        new TravisCiDotOrgJob("job-name", "owner", "master").queryStatus(simpleHttpClient);

        String expectedUrl = "https://api.travis-ci.org/owner/job-name?branch=master";
        verify(simpleHttpClient, times(1)).sendGetWithBasicAuth(expectedUrl, null, null, false);
    }

    @Test
    public void successfulJob() {

        when(simpleHttpClient.sendGetWithBasicAuth(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(passingBuildSvgXml);

        TravisCiDotOrgJob job = new TravisCiDotOrgJob("job-name", "owner", "master");
        JobStatus jobStatus = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.SUCCESS, jobStatus);
    }

    @Test
    public void failedJob() {

        when(simpleHttpClient.sendGetWithBasicAuth(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(failingBuildSvgXml);

        TravisCiDotOrgJob job = new TravisCiDotOrgJob("job-name", "owner", "master");
        JobStatus jobStatus = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.FAILED, jobStatus);
    }
}
