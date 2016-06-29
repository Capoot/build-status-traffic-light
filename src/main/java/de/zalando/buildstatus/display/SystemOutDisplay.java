package de.zalando.buildstatus.display;

public class SystemOutDisplay implements Display {

    @Override
    public void displayFailure() {
        System.out.println("Build failed");
    }

    @Override
    public void displaySuccess() {
        System.out.println("Build successful");
    }

    @Override
    public void displayUnstable() {
        System.out.println("Build unstable (test failures)");
    }
}
