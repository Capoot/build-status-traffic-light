package de.zalando.buildstatus;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class BuildStatusMonitorTest {

    private final String jenkinsJobRedJson;
    private final String jenkinsJobYellowJson;
    private final String jenkinsJobBlueJson;

    private ClientAndServer mockServer;

    @Mock private BuildStatusIndicator buildStatusIndicator;


    public BuildStatusMonitorTest() throws IOException {
        jenkinsJobRedJson = IOUtils.toString(this.getClass().getResourceAsStream("/jenkinsapi-job-red.json"));
        jenkinsJobYellowJson = IOUtils.toString(this.getClass().getResourceAsStream("/jenkinsapi-job-yellow.json"));
        jenkinsJobBlueJson = IOUtils.toString(this.getClass().getResourceAsStream("/jenkinsapi-job-blue.json"));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockServer = new ClientAndServer(8080);
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void ifJenkinsJobIsRedIndicatorShouldDisplayFailed() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobRedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure();
    }

    private void configureJenkinsJob(String jobId, String body) {
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath("/" + jobId + "/api/json"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body)
        );
    }

    @Test
    public void ifJenkinsJobIsYellowIndicatorShouldDisplayUnstable() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobYellowJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayUnstable();
    }

    @Test
    public void ifJenkinsJobIsBlueIndicatorShouldDisplaySuccess() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displaySuccess();
    }

    @Test
    public void ifAtLeastOneJobIsFailedIndicatorShouldDisplayFailure() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueJson, jenkinsJobRedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    private BuildStatusMonitor configureStatusMonitorWithJobs(String... json) {
        List<Job> jobs = new ArrayList<>(json.length);
        int i = 0;
        for(String s : json) {
            String jobName = "job" + i++;
            configureJenkinsJob(jobName, s);
            jobs.add(new JenkinsJob("http://localhost:8080/", jobName, "user", "password"));
        }
        return new BuildStatusMonitor(jobs, buildStatusIndicator);
    }
}
