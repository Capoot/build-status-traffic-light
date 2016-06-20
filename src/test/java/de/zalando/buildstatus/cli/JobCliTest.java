package de.zalando.buildstatus.cli;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JobCliTest {

    private final TemporaryFolder TEMP_DIR = new TemporaryFolder();
    private String path;

    private PrintStream systemout;
    private ByteArrayOutputStream out;

    @Before
    public void setup() throws Exception {
        systemout = System.out;
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        TEMP_DIR.create();
        path = TEMP_DIR.getRoot().getAbsolutePath();
        EnvironmentVariables.set("TE_DATA_DIR", path);
    }

    @After
    public void tearDown() {

        System.setOut(systemout);

        TEMP_DIR.delete();
    }

    @Test
    public void addJenkinsJob() {

        JobCli.main(new String[]{"add-jenkins", "--host", "http://localhost:7999", "--jobname", "job1", "--user",
                "user", "--password", "password"});

        assertTrue(new File(path + "/job1.json").exists());
    }

    @Test
    public void removeJob() throws Exception {

        IOUtils.copy(JobCliTest.class.getResourceAsStream("/jenkinsjob.json"), new FileOutputStream(path +
                "/jenkinsjob.json"));

        JobCli.main(new String[]{"remove", "jenkinsjob"});

        assertFalse(new File(path + "/jenkinsjob.json").exists());
    }

    @Test
    public void listJobs() throws Exception {

        IOUtils.copy(JobCliTest.class.getResourceAsStream("/jenkinsjob.json"), new FileOutputStream(path +
                "/jenkinsjob.json"));

        JobCli.main(new String[]{"list"});

        assertEquals("jenkinsjob\t(jenkins)\thttp://jenkins:8080/jenkinsjob/api/json", out.toString());
    }
}
