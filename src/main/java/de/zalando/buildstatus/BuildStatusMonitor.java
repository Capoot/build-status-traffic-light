package de.zalando.buildstatus;

import de.zalando.buildstatus.display.Display;
import de.zalando.buildstatus.http.SimpleHttpClient;
import de.zalando.buildstatus.job.Job;
import de.zalando.buildstatus.job.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class BuildStatusMonitor {

    private static final Logger logger = LoggerFactory.getLogger(BuildStatusMonitor.class);
    private final Display indicator;
    private final SimpleHttpClient httpClient;
    private JobStatus displayStatus;

    public BuildStatusMonitor(Display indicator, SimpleHttpClient httpClient) {
        this.indicator = indicator;
        this.httpClient = httpClient;
    }

    public void updateDisplay(Collection<Job> jobs) {
        if(jobs == null || jobs.isEmpty()) {
            logger.warn("No jobs configured");
            setDisplayStatus(JobStatus.NO_JOBS_CONFIGURED);
        } else {
            clearDisplayStatus();
            for (Job job : jobs) {
                JobStatus status = job.queryStatus(httpClient);
                setDisplayStatus(status);
            }
        }
        switch(displayStatus) {
            case FAILED:
                indicator.displayFailure();
                break;
            case SUCCESS:
                indicator.displaySuccess();
                break;
            case UNSTABLE:
                indicator.displayUnstable();
                break;
            case FAILED_ANIMATION:
                indicator.displayFailure();
                break;
            case UNSTABLE_ANIMATION:
                indicator.displayUnstable();
                break;
            case SUCCESS_ANIMATION:
                indicator.displaySuccess();
                break;
            case NO_JOBS_CONFIGURED:
                indicator.displayFailure();
                break;
        }
    }

    private void clearDisplayStatus() {
        displayStatus = JobStatus.NO_JOBS_CONFIGURED;
    }

    private void setDisplayStatus(JobStatus displayStatus) {
        if(currentStatusIsFailed() && displayStatus != JobStatus.FAILED_ANIMATION) {
            return;
        }
        if(currentStatusIsUnstable() && displayStatus == JobStatus.SUCCESS) {
            return;
        }
        this.displayStatus = displayStatus;
    }

    private boolean currentStatusIsFailed() {
        return this.displayStatus == JobStatus.FAILED || this.displayStatus == JobStatus.FAILED_ANIMATION;
    }

    private boolean currentStatusIsUnstable() {
        return this.displayStatus == JobStatus.UNSTABLE || this.displayStatus == JobStatus.UNSTABLE_ANIMATION;
    }
}
