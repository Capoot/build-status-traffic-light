package de.zalando.buildstatus.job;

public enum JobStatus {
    SUCCESS,
    UNSTABLE,
    SUCCESS_ANIMATION,
    UNSTABLE_ANIMATION,
    FAILED_ANIMATION, FAILED,
    NO_JOBS_CONFIGURED
}
