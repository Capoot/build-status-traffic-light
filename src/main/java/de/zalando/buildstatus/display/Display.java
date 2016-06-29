package de.zalando.buildstatus.display;

public interface Display {
    void displayFailure();
    void displaySuccess();
    void displayUnstable();
}
