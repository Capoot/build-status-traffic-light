package de.zalando.buildstatus;

public interface Job {

    JobStatus queryStatus();

    String getName();

    String getDetails();
}
