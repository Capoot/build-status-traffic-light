package de.zalando.buildstatus;

public interface BuildStatusIndicator {
    void displayFailure();
    void displaySuccess();
    void displayUnstable();
}
