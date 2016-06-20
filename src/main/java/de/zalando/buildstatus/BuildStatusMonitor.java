package de.zalando.buildstatus;

import de.zalando.buildstatus.display.Display;
import de.zalando.buildstatus.job.Job;
import de.zalando.buildstatus.job.JobStatus;

import java.util.Collection;

public class BuildStatusMonitor {

    private final Display indicator;
    private JobStatus displayStatus;

    public BuildStatusMonitor(Display indicator) {
        this.indicator = indicator;
    }

    public void update(Collection<Job> jobs) {

        clearDisplayStatus();
        for (Job job : jobs) {
            JobStatus status = job.queryStatus();
            setDisplayStatus(status);
        }
        switch(displayStatus) {
            case FAILED:
                indicator.displayFailure(false);
                break;
            case SUCCESS:
                indicator.displaySuccess(false);
                break;
            case UNSTABLE:
                indicator.displayUnstable(false);
                break;
            case FAILED_ANIMATION:
                indicator.displayFailure(true);
                break;
            case UNSTABLE_ANIMATION:
                indicator.displayUnstable(true);
                break;
            case SUCCESS_ANIMATION:
                indicator.displaySuccess(true);
        }
    }

    private void clearDisplayStatus() {
        displayStatus = null;
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
