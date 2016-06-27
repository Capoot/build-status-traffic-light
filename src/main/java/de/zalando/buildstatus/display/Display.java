package de.zalando.buildstatus.display;

public interface Display {
    void displayFailure(boolean flashing);
    void displaySuccess(boolean flashing);
    void displayUnstable(boolean flashing);
}
