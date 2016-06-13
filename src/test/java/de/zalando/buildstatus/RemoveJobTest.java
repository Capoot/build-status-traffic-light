package de.zalando.buildstatus;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


public class RemoveJobTest {

    private static final TemporaryFolder jobsFolder = new TemporaryFolder();
    public static final String JOB_NAME = "deletejob";
    private static BuildStatusMonitor buildStatusMonitor = mock(BuildStatusMonitor.class);

    @BeforeClass
    public static void setup() throws Exception {

        jobsFolder.create();
        IOUtils.copy(RemoveJobTest.class.getResourceAsStream("/deletejob.json"),
                new FileOutputStream(jobsFolder.getRoot().getAbsolutePath() + "/" + JOB_NAME + ".json"));

        CommandLineInterface cli = new CommandLineInterface(jobsFolder.getRoot().getAbsolutePath(), buildStatusMonitor);
        cli.removeJob(JOB_NAME);
    }

    @Test
    public void jobFileShouldNotExistAnymore() {
        File f = new File(jobsFolder.getRoot().getAbsoluteFile() + "/" + JOB_NAME + ".json");
        assertFalse(f.exists());
    }

    @Test
    public void noStatusUpdateShouldHaveBeenTriggered() {
        verify(buildStatusMonitor, never()).update(any());
        verify(buildStatusMonitor, never()).update();
    }
}
