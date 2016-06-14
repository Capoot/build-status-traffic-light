package de.zalando.buildstatus.job;

public interface Job {

    JobStatus queryStatus();

    String getName();

    String getDetails();
}
