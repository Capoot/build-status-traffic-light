package de.zalando.buildstatus.job;

import de.zalando.buildstatus.http.SimpleHttpClient;

public interface Job {

    JobStatus queryStatus(SimpleHttpClient client);

    String getName();

    String getPrintableDetails();
}
