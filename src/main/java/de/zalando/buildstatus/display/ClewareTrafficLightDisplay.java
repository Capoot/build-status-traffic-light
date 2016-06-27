package de.zalando.buildstatus.display;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controls Cleware USB Mini Traffic light. Requires <a href="https://docs.google.com/a/zalando.de/document/d/1UCllGJj2ZUeLe1vxnVTs1pRWzoleir_1C9he31zUW6c/edit?usp=sharing">cleware control software</a> installed.
 * This class might only work as intended on Linux, other OS's were not tested. Supposed to run with cleware library
 * version 330.
 *
 * <p><strong>!!! Requires root privileges to run !!!</strong></p>
 */
public class ClewareTrafficLightDisplay implements Display {

    private static final Pattern NUMBER_OF_DEVICES_PATTERN =
            Pattern.compile(".*Number of Cleware devices found: (\\d*).*", Pattern.DOTALL);
    private static final Pattern SERIAL_NUMBER_PATTERN =
            Pattern.compile(".*serial number: (-?\\d*).*", Pattern.DOTALL);

    private static final int RED_LIGHT_ARGUMENT = 0;
    private static final int YELLOW_LIGHT_ARGUMENT = 1;
    private static final int GREEN_LIGHT_ARGUMENT = 2;

    private final int deviceId; // TODO: dynamically

    private FlashingLight currentFlashingRunnable;

    public ClewareTrafficLightDisplay() {

        ProcessBuilder pb = new ProcessBuilder("clewarecontrol", "-l");

        String output;
        try {
            Process p = pb.start();
            p.waitFor();
            output = IOUtils.toString(p.getInputStream());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("failed to initialize traffic light", e);
        }

        if(!output.startsWith("Cleware library version")) {
            throw new IllegalStateException("cleware control software does not seem to be installed");
        }

        if(noDeviceFound(output)) {
            throw new IllegalStateException("no cleware devices were found. Is the traffic light actually connected " +
                    "to a USB port?");
        }

        Matcher m = SERIAL_NUMBER_PATTERN.matcher(output);
        if(!m.matches()) {
            throw new IllegalStateException("failed to read traffic light's cleware device serial number");
        }
        deviceId = Integer.parseInt(m.group(1));
    }

    private boolean noDeviceFound(String output) {
        Matcher m = NUMBER_OF_DEVICES_PATTERN.matcher(output);
        if(!m.matches()) {
            return false;
        }
        String group = m.group(1);
        return Integer.parseInt(group) == 0;
    }

    @Override
    public void displayFailure(boolean flashing) {

        lightsOff();

        if(flashing) {
            startFlashing(RED_LIGHT_ARGUMENT);
        }

        setLight(true, RED_LIGHT_ARGUMENT);
    }

    private void startFlashing(int color) {
        currentFlashingRunnable = new FlashingLight(color);
        new Thread(currentFlashingRunnable).start();
    }

    @Override
    public void displaySuccess(boolean flashing) {

        lightsOff();

        if(flashing) {
            startFlashing(GREEN_LIGHT_ARGUMENT);
        }

        setLight(true, GREEN_LIGHT_ARGUMENT);
    }

    @Override
    public void displayUnstable(boolean flashing) {

        lightsOff();

        if(flashing) {
            startFlashing(YELLOW_LIGHT_ARGUMENT);
        }

        setLight(true, YELLOW_LIGHT_ARGUMENT);
    }

    private void setLight(boolean status, int color) {

        String lightStatus = status ? "1" : "0";
        ProcessBuilder pb = new ProcessBuilder("clewarecontrol", "-c", "1", "-d", String.valueOf(deviceId), "-as",
                String.valueOf(color), lightStatus);

        Process p;
        String error;
        try {
            p = pb.start();
            p.waitFor();
            error = IOUtils.toString(p.getErrorStream());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(error != null && !error.isEmpty() && !"0\r".equals(error)) { // TODO: make sure "0\r" is really always no error
            throw new RuntimeException("failed to access traffic light: " + error);
        }
    }

    public void lightsOff() {
        cancelFlashing();
        setLight(false, GREEN_LIGHT_ARGUMENT);
        setLight(false, RED_LIGHT_ARGUMENT);
        setLight(false, YELLOW_LIGHT_ARGUMENT);
    }

    private void cancelFlashing() {
        if(currentFlashingRunnable != null) {
            currentFlashingRunnable.cancel();
        }
    }

    private final class FlashingLight implements Runnable {

        private final int color;

        private boolean isCancelled = false;

        private FlashingLight(int color) {
            this.color = color;
        }

        @Override
        public void run() {

            boolean status = true;
            while(!isCancelled) {
                setLight(status, color);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
                status = !status;
            }

        }

        public void cancel() {
            isCancelled = true;
        }
    }
}
