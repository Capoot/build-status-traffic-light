package de.zalando.buildstatus;

import java.util.Collection;

public class BuildStatusMonitor {

    private final Collection<Job> jobs;
    private final BuildStatusIndicator indicator;
    private JobStatus displayStatus;

    public BuildStatusMonitor(Collection<Job> jobs, BuildStatusIndicator indicator) {
        this.jobs = jobs;
        this.indicator = indicator;
    }

    public void update() {

        clearDisplayStatus();
        for (Job job : jobs) {
            JobStatus status = job.queryStatus();
            setDisplayStatus(status);
            if (displayStatus == JobStatus.FAILED) {
                break;
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
        }
    }

    private void clearDisplayStatus() {
        displayStatus = null;
    }

    public void setDisplayStatus(JobStatus displayStatus) {
        if(this.displayStatus == JobStatus.FAILED) {
            return;
        }
        if(this.displayStatus == JobStatus.UNSTABLE && displayStatus == JobStatus.SUCCESS) {
            return;
        }
        this.displayStatus = displayStatus;
    }
}
