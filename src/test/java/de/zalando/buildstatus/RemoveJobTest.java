package de.zalando.buildstatus;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class RemoveJobTest {

    private static final TemporaryFolder jobsFolder = new TemporaryFolder();
    private static final String JOB_NAME = "jenkinsjob";
    private static final String JOB_FILE_NAME = JOB_NAME + ".json";
    private static final BuildStatusMonitor buildStatusMonitor = mock(BuildStatusMonitor.class);

    @BeforeClass
    public static void setup() throws Exception {

        jobsFolder.create();
        IOUtils.copy(RemoveJobTest.class.getResourceAsStream("/" + JOB_FILE_NAME),
                new FileOutputStream(jobsFolder.getRoot().getAbsolutePath() + "/" + JOB_FILE_NAME));

        JobService cli = new JobService(jobsFolder.getRoot().getAbsolutePath(), buildStatusMonitor);
        cli.removeJob(JOB_NAME);
    }

    @Test
    public void jobFileShouldNotExistAnymore() {
        File f = new File(jobsFolder.getRoot().getAbsoluteFile() + "/" + JOB_FILE_NAME);
        assertFalse(f.exists());
    }

    @Test
    public void noStatusUpdateShouldHaveBeenTriggered() {
        verify(buildStatusMonitor, never()).update(any());
        verify(buildStatusMonitor, never()).update();
    }

    @Test
    public void jobShouldHaveBeenRemovedFromBuildStatusMonitor() {
        verify(buildStatusMonitor, times(1)).removeJob(JOB_NAME);
    }
}
