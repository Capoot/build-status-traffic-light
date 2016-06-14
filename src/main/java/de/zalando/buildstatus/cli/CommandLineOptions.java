package de.zalando.buildstatus.cli;

public class CommandLineOptions {

    private final boolean systemOutDisplay;

    public CommandLineOptions(String[] args) {
        systemOutDisplay = isFlagSet("sysout", args);
    }

    private boolean isFlagSet(String flag, String[] args) {
        for(String s : args) {
            if(!s.startsWith("--")) {
                continue;
            }
            if(s.equals("--" + flag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSystemOutDisplay() {
        return systemOutDisplay;
    }
}
