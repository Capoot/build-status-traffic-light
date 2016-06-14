package de.zalando.buildstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BuildStatusMonitor {

    private final Collection<Job> jobs;
    private final BuildStatusIndicator indicator;
    private JobStatus displayStatus;

    public BuildStatusMonitor(Collection<Job> jobs, BuildStatusIndicator indicator) {
        this.jobs = jobs;
        this.indicator = indicator;
    }

    public void update() {
        update(jobs);
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

    public void addJob(Job job) {
        if(jobs.contains(job)) {
            throw new IllegalStateException("duplicate job");
        }
        jobs.add(job);
    }

    public void removeJob(String jobName) {

        Job job = null;
        for(Job j : jobs) {
            if(j.getName().equals(jobName)) {
                job = j;
                break;
            }
        }

        if(job == null) {
            throw new IllegalStateException("no such job: [" + jobName + "]");
        }

        jobs.remove(job);
    }

    public List<String> listJobDetails() {
        List<String> details = new ArrayList<>(jobs.size());
        for(Job job : jobs) {
            details.add(job.getDetails());
        }
        return Collections.unmodifiableList(details);
    }
}
