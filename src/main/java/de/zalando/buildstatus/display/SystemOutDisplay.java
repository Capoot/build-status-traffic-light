package de.zalando.buildstatus.display;

public class SystemOutDisplay implements Display {

    @Override
    public void displayFailure(boolean flashing) {
        System.out.println("Build failed");
    }

    @Override
    public void displaySuccess(boolean flashing) {
        System.out.println("Build successful");
    }

    @Override
    public void displayUnstable(boolean flashing) {
        System.out.println("Build unstable (test failures)");
    }
}
