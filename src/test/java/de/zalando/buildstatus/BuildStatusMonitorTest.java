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
    private final String jenkinsJobRedAnimatedJson;
    private final String jenkinsJobBlueAnimatedJson;
    private final String jenkinsJobYellowAnimatedJson;

    private ClientAndServer mockServer;

    @Mock private BuildStatusIndicator buildStatusIndicator;


    public BuildStatusMonitorTest() throws IOException {
        jenkinsJobRedJson = IOUtils.toString(this.getClass().getResourceAsStream("/jenkinsapi-job-red.json"));
        jenkinsJobYellowJson = IOUtils.toString(this.getClass().getResourceAsStream("/jenkinsapi-job-yellow.json"));
        jenkinsJobBlueJson = IOUtils.toString(this.getClass().getResourceAsStream("/jenkinsapi-job-blue.json"));
        jenkinsJobRedAnimatedJson = IOUtils.toString(
                this.getClass().getResourceAsStream("/jenkinsapi-job-red_anime.json"));
        jenkinsJobBlueAnimatedJson = IOUtils.toString(
                this.getClass().getResourceAsStream("/jenkinsapi-job-blue_anime.json"));
        jenkinsJobYellowAnimatedJson = IOUtils.toString(
                this.getClass().getResourceAsStream("/jenkinsapi-job-yellow_anime.json"));
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
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(false);
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
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayUnstable(false);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifJenkinsJobIsBlueIndicatorShouldDisplaySuccess() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displaySuccess(false);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifAtLeastOneJobIsFailedIndicatorShouldDisplayFailure() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueJson, jenkinsJobRedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(false);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifAtLeastOneJobIsUnstableIndicatorShouldDisplayUnstable() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueJson, jenkinsJobYellowJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayUnstable(false);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifThereAreUnstableAndFailedJobsIndicatorShouldDisplayFailed() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobRedJson, jenkinsJobYellowJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(false);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifAllJobsAreSuccessIndicatorShouldDisplaySuccess() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueJson, jenkinsJobBlueJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displaySuccess(false);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifJenkinsJobIsBlueAnimatedIndicatorShouldDisplaySuccessBlinking() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueAnimatedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displaySuccess(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifJenkinsJobIsRedAnimatedIndicatorShouldDisplayFailureBlinking() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobRedAnimatedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void ifJenkinsJobIsYellowAnimatedIndicatorShouldDisplayUnstableBlinking() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobRedAnimatedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void successBlinkingStatusShouldOverrideNonBlinkingStatus() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobBlueJson, jenkinsJobBlueAnimatedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displaySuccess(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void unstableBlinkingStatusShouldOverrideNonBlinkingStatus() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobYellowJson, jenkinsJobYellowAnimatedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayUnstable(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void failureBlinkingStatusShouldOverrideNonBlinkingStatus() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobRedJson, jenkinsJobRedAnimatedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void failureBlinkingStatusShouldOverrideSuccessStatus() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobRedAnimatedJson, jenkinsJobBlueJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void failureBlinkingStatusShouldOverrideUnstableStatus() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobYellowJson, jenkinsJobRedAnimatedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void unstableBlinkingStatusShouldNotOverrideFailedStatus() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobYellowAnimatedJson, jenkinsJobRedJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayFailure(false);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }

    @Test
    public void unstableBlinkingStatusShouldOverrideSuccessStatus() throws Exception {
        BuildStatusMonitor monitor = configureStatusMonitorWithJobs(jenkinsJobYellowAnimatedJson, jenkinsJobBlueJson);
        monitor.update();
        Mockito.verify(buildStatusIndicator, Mockito.times(1)).displayUnstable(true);
        Mockito.verifyNoMoreInteractions(buildStatusIndicator);
    }
}
