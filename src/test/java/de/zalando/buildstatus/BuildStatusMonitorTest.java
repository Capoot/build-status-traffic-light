package de.zalando.buildstatus;

import de.zalando.buildstatus.display.Display;
import de.zalando.buildstatus.job.JenkinsJob;
import de.zalando.buildstatus.job.Job;
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
import java.util.Collection;
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

    @Mock private Display display;

    private BuildStatusMonitor monitor;
    private int port= 8081;


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
        mockServer = new ClientAndServer(port);
        monitor = new BuildStatusMonitor(display);
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void ifJenkinsJobIsRedIndicatorShouldDisplayFailed() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobRedJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    private Collection<Job> createJobsListFromJson(String... json) {
        List<Job> jobs = new ArrayList<>(json.length);
        int i = 0;
        for(String s : json) {
            String jobName = "job" + i++;
            configureJenkinsJob(jobName, s);
            jobs.add(new JenkinsJob("http://localhost:" + port +"/", jobName, "user", "password", true));
        }
        return jobs;
    }

    private void configureJenkinsJob(String jobId, String body) {
        mockServer
            .when(request()
                .withMethod("GET")
                .withPath("/job/" + jobId + "/api/json"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body)
        );
    }

    @Test
    public void ifJenkinsJobIsYellowIndicatorShouldDisplayUnstable() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobYellowJson));
        Mockito.verify(display, Mockito.times(1)).displayUnstable();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifJenkinsJobIsBlueIndicatorShouldDisplaySuccess() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobBlueJson));
        Mockito.verify(display, Mockito.times(1)).displaySuccess();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifAtLeastOneJobIsFailedIndicatorShouldDisplayFailure() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobBlueJson, jenkinsJobRedJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifAtLeastOneJobIsUnstableIndicatorShouldDisplayUnstable() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobBlueJson, jenkinsJobYellowJson));
        Mockito.verify(display, Mockito.times(1)).displayUnstable();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifThereAreUnstableAndFailedJobsIndicatorShouldDisplayFailed() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobRedJson, jenkinsJobYellowJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifAllJobsAreSuccessIndicatorShouldDisplaySuccess() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobBlueJson, jenkinsJobBlueJson));
        Mockito.verify(display, Mockito.times(1)).displaySuccess();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifJenkinsJobIsBlueAnimatedIndicatorShouldDisplaySuccess() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobBlueAnimatedJson));
        Mockito.verify(display, Mockito.times(1)).displaySuccess();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifJenkinsJobIsRedAnimatedIndicatorShouldDisplayFailure() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobRedAnimatedJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void ifJenkinsJobIsYellowAnimatedIndicatorShouldDisplayUnstable() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobRedAnimatedJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void successBlinkingStatusShouldOverrideNonBlinkingStatus() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobBlueJson, jenkinsJobBlueAnimatedJson));
        Mockito.verify(display, Mockito.times(1)).displaySuccess();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void unstableBlinkingStatusShouldOverrideNonBlinkingStatus() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobYellowJson, jenkinsJobYellowAnimatedJson));
        Mockito.verify(display, Mockito.times(1)).displayUnstable();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void failureBlinkingStatusShouldOverrideNonBlinkingStatus() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobRedJson, jenkinsJobRedAnimatedJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void failureBlinkingStatusShouldOverrideSuccessStatus() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobRedAnimatedJson, jenkinsJobBlueJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void failureBlinkingStatusShouldOverrideUnstableStatus() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobYellowJson, jenkinsJobRedAnimatedJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void unstableBlinkingStatusShouldNotOverrideFailedStatus() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobYellowAnimatedJson, jenkinsJobRedJson));
        Mockito.verify(display, Mockito.times(1)).displayFailure();
        Mockito.verifyNoMoreInteractions(display);
    }

    @Test
    public void unstableBlinkingStatusShouldOverrideSuccessStatus() throws Exception {
        monitor.update(createJobsListFromJson(jenkinsJobYellowAnimatedJson, jenkinsJobBlueJson));
        Mockito.verify(display, Mockito.times(1)).displayUnstable();
        Mockito.verifyNoMoreInteractions(display);
    }
}
