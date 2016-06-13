package de.zalando.buildstatus;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CliAddJenkinsJobTest {

    private static final int PORT = 8080;
    private static final String HOST = "http://localhost:" + PORT;
    private static final String JOB_NAME = "jobName";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";

    private static final TemporaryFolder jobsFolder = new TemporaryFolder();
    private static final ArgumentCaptor<Collection> triggeredJobsCollectionArgument = ArgumentCaptor.forClass(Collection.class);
    private static final ArgumentCaptor<Job> jobArgument = ArgumentCaptor.forClass(Job.class);

    private static JSONObject job;
    private static BuildStatusMonitor buildStatusMonitor;

    @BeforeClass
    public static void setup() throws IOException {

        jobsFolder.create();
        buildStatusMonitor = mock(BuildStatusMonitor.class);
        CommandLineInterface cli = new CommandLineInterface(jobsFolder.getRoot().getAbsolutePath(), buildStatusMonitor);
        cli.addJenkinsJob(HOST, JOB_NAME, USER_NAME, PASSWORD);
        readJobFromJsonFile();
    }

    private static void readJobFromJsonFile() throws IOException {
        String path = jobsFolder.getRoot().getPath() + "/" + JOB_NAME + ".json";
        String jsonData = IOUtils.toString(new FileInputStream(path));
        job = new JSONObject(jsonData);
    }

    @AfterClass
    public static void tearDown() {
        jobsFolder.delete();
    }

    @Test
    public void jobFileShouldContainCorrectUrl() {
        assertEquals(HOST + "/" + JOB_NAME + "/api/json", job.getString("url"));
    }

    @Test
    public void jobFileShouldContainCorrectUserName() {
        assertEquals(USER_NAME, job.getString("userName"));
    }

    @Test
    public void jobFileShouldContainCorrectPassword() {
        assertEquals(PASSWORD, job.getString("password"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void jobStatusUpdateShouldHaveBeenTriggered() {
        verify(buildStatusMonitor, times(1)).update(triggeredJobsCollectionArgument.capture());
        Collection<Job> jobs = triggeredJobsCollectionArgument.getValue();
        assertEquals(1, jobs.size());
        Job job = jobs.iterator().next();
        assertEquals(HOST + "/" + JOB_NAME + "/api/json", ((JenkinsJob)job).getUrl());
    }

    @Test
    public void jobShouldHaveBeenAddedToTheBuildStatusMonitor() {
        verify(buildStatusMonitor, times(1)).addJob(jobArgument.capture());
        Job job = jobArgument.getValue();
        assertEquals(HOST + "/" + JOB_NAME + "/api/json", ((JenkinsJob)job).getUrl());
    }
}
