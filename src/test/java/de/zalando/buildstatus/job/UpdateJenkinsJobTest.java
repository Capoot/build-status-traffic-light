package de.zalando.buildstatus.job;

import de.zalando.buildstatus.BuildStatusMonitor;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UpdateJenkinsJobTest {

    private static final String JOB_NAME = "jenkinsjob";
    private static final String JOB_FILE_NAME = JOB_NAME + ".json";
    private static final String HOST = "http://jenkins:1234";
    private static final String FULL_JOB_URL = HOST + "/" + JOB_NAME + "/api/json";
    private static final String USER = "newUser";
    private static final String PASSWORD = "newPassword";

    private static final TemporaryFolder jobsFolder = new TemporaryFolder();
    private static final BuildStatusMonitor buildStatusMonitor = mock(BuildStatusMonitor.class);
    private static final ArgumentCaptor<Collection> jobsCollectionArgument = ArgumentCaptor.forClass(Collection.class);

    private static JSONObject jobJson;

    @BeforeClass
    public static void setup() throws Exception {

        jobsFolder.create();
        String jobFileName = jobsFolder.getRoot().getAbsolutePath() + "/" + JOB_FILE_NAME;
        IOUtils.copy(RemoveJobTest.class.getResourceAsStream("/" + JOB_FILE_NAME), new FileOutputStream(jobFileName));

        JobService cli = new JobService(jobsFolder.getRoot().getAbsolutePath(), buildStatusMonitor);
        cli.updateJenkinsJob(HOST, JOB_NAME, USER, PASSWORD);
        jobJson = new JSONObject(IOUtils.toString(new FileInputStream(jobFileName)));
    }

    @AfterClass
    public static void tearDown() {
        jobsFolder.delete();
    }

    @Test
    public void jobJsonShouldHaveNewHostUrl() {
        assertEquals(HOST, jobJson.getString("host"));
    }

    @Test
    public void jobJsonShouldHaveNewUserName() {
        assertEquals(USER, jobJson.getString("userName"));
    }

    @Test
    public void jobJsonShouldHaveNewPassword() {
        assertEquals(PASSWORD, jobJson.getString("password"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void jobStatusShouldHaveBeenUpdated() {
        verify(buildStatusMonitor, times(1)).update(jobsCollectionArgument.capture());
        Collection<Job> jobs = jobsCollectionArgument.getValue();
        JenkinsJob job = (JenkinsJob)jobs.iterator().next();
        assertEquals(FULL_JOB_URL, job.getUrl());
    }
}
