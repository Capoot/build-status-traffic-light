package de.zalando.buildstatus.cli;

public class CliFlagsAndOptions {

    public static boolean isFlagSet(char flag, String[] args) {
        for(String s : args) {
            if(!s.startsWith("-")) {
                continue;
            }
            for(char c : s.toCharArray()) {
                if(c == '-') {
                    continue;
                }
                if(c == flag) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getOption(String option, String[] args) {
        if(args == null || args.length < 2 || option == null || option.isEmpty()) {
            return null;
        }
        for(int i=0; i<args.length-1; i++) {
            if(!args[i].startsWith("--")) {
                continue;
            }
            if(args[i].equalsIgnoreCase(option) || option.equalsIgnoreCase(args[i].substring(2))) {
                return args[i+1];
            }
        }
        return null;
    }
}
