package de.zalando.buildstatus.job;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JenkinsJobTest {

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithNullHostShouldFail() {
        new JenkinsJob(null, "job1", "user", "password");
    }

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithEmptyHostShouldFail() {
        new JenkinsJob("", "job1", "user", "password");
    }

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithNullJobNameShouldFail() {
        new JenkinsJob("http://host", null, "user", "password");
    }

    @Test(expected=IllegalArgumentException.class)
    public void creatingJobWithEmptyJobNameShouldFail() {
        new JenkinsJob("http://host", "", "user", "password");
    }

    @Test
    public void ifProtocolWasNotSpecifiedInHostUrlItShouldPrependHttp() {
        assertEquals("http://host", new JenkinsJob("host", "job", "user", "password").getHost());
    }
}
