package de.zalando.buildstatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class BuildStatusMonitorTest {

    @Test
    public void ifJenkinsJobFailsStatusMonitorShouldSetIndicatorToFailed() throws Exception {


        Job job = new JenkinsJob("http://localhost:8080/", "job1", "user", "password");
        BuildStatusIndicator indicator = mock(BuildStatusIndicator.class);

        String jobJson = IOUtils.toString(this.getClass().getResourceAsStream("/jenkinsapi-job-red.json"));
        ClientAndServer mockServer = new ClientAndServer(8080);
        mockServer.when(
                    request()
                            .withMethod("GET")
                            .withPath("/job1/api/json")
                ).respond(
                    response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(jobJson)
        );

        BuildStatusMonitor monitor = new BuildStatusMonitor(singletonList(job), indicator);
        monitor.update();

        Mockito.verify(indicator, Mockito.times(1)).displayFailure();
    }
}
