package de.zalando.buildstatus.job;

import de.zalando.buildstatus.http.SimpleHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenericRestApiJobTest {

    private final int port = 8081;
    private final String host = "http://localhost:" + port;
    private final String path = host + "/my/custom/job-path";

    @Mock private SimpleHttpClient simpleHttpClient;
    private GenericRestApiJob job = new GenericRestApiJob(path, "johndoe", "password", ".*success", ".*test " +
            "failures", false);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void ifSuccessRegexMatchesResultShouldBeSuccess() {

        when(simpleHttpClient.sendGetWithBasicAuth(path, "johndoe", "password", false))
                .thenReturn("the build was a success");

        JobStatus status = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.SUCCESS, status);
    }

    @Test
    public void ifSuccessRegexDoesNotMatchResultShouldBeFailure() {

        when(simpleHttpClient.sendGetWithBasicAuth(path, "johndoe", "password", false))
                .thenReturn("the build was a failure");

        JobStatus status = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.FAILED, status);
    }

    @Test
    public void ifUnstableMatchesAndSuccessRegexDoesNotMatchResultShouldBeUnstable() {

        when(simpleHttpClient.sendGetWithBasicAuth(path, "johndoe", "password", false))
                .thenReturn("the build has test failures");

        JobStatus status = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.UNSTABLE, status);
    }

    @Test
    public void acceptInsecureSslCertStatusShouldBepassedCorrectly() {

        when(simpleHttpClient.sendGetWithBasicAuth(path, "johndoe", "password", true))
                .thenReturn("the build has test failures");

        new GenericRestApiJob(path, "johndoe", "password", ".*success", ".*test " +
                "failures", true).queryStatus(simpleHttpClient);

        verify(simpleHttpClient, never()).sendGetWithBasicAuth(path, "johndoe", "password", false);
    }
}
