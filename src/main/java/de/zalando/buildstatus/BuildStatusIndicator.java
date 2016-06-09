package de.zalando.buildstatus;

public interface BuildStatusIndicator {
    void displayFailure(boolean flashing);
    void displaySuccess(boolean flashing);
    void displayUnstable(boolean flashing);
}
