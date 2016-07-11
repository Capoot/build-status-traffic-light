package de.zalando.buildstatus.job;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JobsIoTest {

    private TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        tempDir.create();
    }

    @After
    public void tearDown() {
        tempDir.delete();
    }

    @Test
    public void genericRestApiJobShouldBeReadCorrectly() throws Exception {

        copyFileFromClassPathToFileSystem("/genericJob.json", tempDir.getRoot().getAbsolutePath() + "/genericJob.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        GenericRestApiJob job = (GenericRestApiJob)jobs.iterator().next();
        assertEquals("https://myhost:8081/myjob", job.getUrl());
        assertEquals("johndoe", job.getUserName());
        assertEquals("password", job.getPassword());
        assertTrue(job.isAcceptSelfSignedSslCert());
        assertEquals(".*success.*", job.getSuccessRegex());
        assertEquals(".*test failures.*", job.getUnstableRegex());
    }

    private void copyFileFromClassPathToFileSystem(String classPathFile, String fileSystemFile) throws IOException {
        IOUtils.copy(JobsIoTest.class.getResourceAsStream(classPathFile), new FileOutputStream(fileSystemFile));
    }

    @Test
    public void acceptInsecureSslCertShouldAlsoReadFromBooleanLiteral() throws Exception {

        copyFileFromClassPathToFileSystem("/genericJobWithBoolean.json", tempDir.getRoot().getAbsolutePath()
                + "/genericJob.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        GenericRestApiJob job = (GenericRestApiJob)jobs.iterator().next();
        assertTrue(job.isAcceptSelfSignedSslCert());
    }

    @Test
    public void readJenkinsJobFromFile() throws Exception {

        copyFileFromClassPathToFileSystem("/jenkinsjob.json", tempDir.getRoot().getAbsolutePath() + "/jenkinsjob.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        JenkinsJob job = (JenkinsJob)jobs.iterator().next();
        assertEquals("jenkinsjob", job.getName());
        assertEquals("http://jenkins:8080", job.getHost());
        assertEquals("testUser", job.getUserName());
        assertEquals("testPassword", job.getPassword());
        assertTrue(job.isAcceptInsecureSslCert());
    }

    @Test
    public void readJobsWithMissingCredentialsShouldntFail() throws Exception {

        copyFileFromClassPathToFileSystem("/genericJobWithoutCredentials.json", tempDir.getRoot().getAbsolutePath()
                + "/genericJob.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        GenericRestApiJob job = (GenericRestApiJob)jobs.iterator().next();
        assertNull(job.getUserName());
        assertNull(job.getPassword());
    }

    @Test
    public void readGenericJobWithMissingUnstableRegexShouldntFail() throws Exception {

        copyFileFromClassPathToFileSystem("/genericJobWithoutUnstableRegex.json", tempDir.getRoot().getAbsolutePath()
                + "/genericJob.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        GenericRestApiJob job = (GenericRestApiJob)jobs.iterator().next();
        assertNull(job.getUnstableRegex());
    }

    @Test
    public void readTravisCiDotOrgJobFromFile() throws Exception {

        copyFileFromClassPathToFileSystem("/travisjob.json", tempDir.getRoot().getAbsolutePath() + "/travisjob.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        TravisCiDotOrgJob job = (TravisCiDotOrgJob)jobs.iterator().next();
        assertEquals("https://api.travis-ci.org/ownerName/jobName?branch=master", job.getUrl());
    }

    @Test
    public void ifAcceptInsecureSslCertIsMissingInGenericJobItShouldBeSetToFalse() throws Exception {

        copyFileFromClassPathToFileSystem("/genericJobSslCert.json", tempDir.getRoot().getAbsolutePath()
                + "/genericJobSslCert.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        GenericRestApiJob job = (GenericRestApiJob)jobs.iterator().next();
        assertFalse(job.isAcceptSelfSignedSslCert());
    }

    @Test
    public void ifAcceptInsecureSslCertIsMissingInJenkinsJobItShouldBeSetToFalse() throws Exception {

        copyFileFromClassPathToFileSystem("/jenkinsJobSslCert.json", tempDir.getRoot().getAbsolutePath()
                + "/jenkinsJobSslCert.json");
        Collection<Job> jobs = JobsIO.readJobs(tempDir.getRoot().getAbsolutePath());

        assertEquals(1, jobs.size());
        JenkinsJob job = (JenkinsJob)jobs.iterator().next();
        assertFalse(job.isAcceptInsecureSslCert());
    }
}
