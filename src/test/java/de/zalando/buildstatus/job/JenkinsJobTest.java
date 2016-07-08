package de.zalando.buildstatus.job;

import de.zalando.buildstatus.http.SimpleHttpClient;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class JenkinsJobTest {

    private static final String SUCCESSFUL_JENKINS_RESPONSE = loadFileToString("/jenkinsapi-job-blue.json");
    private static final String FAILED_JENKINS_RESPONSE = loadFileToString("/jenkinsapi-job-red.json");
    private static final String UNSTABLE_JENKINS_RESPONSE = loadFileToString("/jenkinsapi-job-yellow.json");

    private static String loadFileToString(String path) {
        try {
            return IOUtils.toString(JenkinsJobTest.class.getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Mock private SimpleHttpClient simpleHttpClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithNullHostShouldFail() {
        new JenkinsJob(null, "job1", "user", "password", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithEmptyHostShouldFail() {
        new JenkinsJob("", "job1", "user", "password", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithNullJobNameShouldFail() {
        new JenkinsJob("http://host", null, "user", "password", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithEmptyJobNameShouldFail() {
        new JenkinsJob("http://host", "", "user", "password", true);
    }

    @Test
    public void ifProtocolWasNotSpecifiedInHostUrlItShouldPrependHttp() {
        assertEquals("http://host", new JenkinsJob("host", "job", "user", "password", true).getHost());
    }

    @Test
    public void successfulJob() {

        when(simpleHttpClient.sendGetWithBasicAuth("http://host/job/job1/api/json", "user", "password", true))
                .thenReturn(SUCCESSFUL_JENKINS_RESPONSE);

        JenkinsJob job = new JenkinsJob("http://host", "job1", "user", "password", true);
        JobStatus jobStatus = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.SUCCESS, jobStatus);
    }

    @Test
    public void failingJob() {

        when(simpleHttpClient.sendGetWithBasicAuth("http://host/job/job1/api/json", "user", "password", true))
                .thenReturn(FAILED_JENKINS_RESPONSE);

        JenkinsJob job = new JenkinsJob("http://host", "job1", "user", "password", true);
        JobStatus jobStatus = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.FAILED, jobStatus);
    }

    @Test
    public void unstableJob() {

        when(simpleHttpClient.sendGetWithBasicAuth("http://host/job/job1/api/json", "user", "password", true))
                .thenReturn(UNSTABLE_JENKINS_RESPONSE);

        JenkinsJob job = new JenkinsJob("http://host", "job1", "user", "password", true);
        JobStatus jobStatus = job.queryStatus(simpleHttpClient);

        assertEquals(JobStatus.UNSTABLE, jobStatus);
    }
}
