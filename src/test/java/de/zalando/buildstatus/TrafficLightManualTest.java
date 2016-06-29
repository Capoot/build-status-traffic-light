package de.zalando.buildstatus;

import de.zalando.buildstatus.display.ClewareTrafficLightDisplay;

public class TrafficLightManualTest {

    public static void main(String[] args) throws Exception {

        ClewareTrafficLightDisplay light = new ClewareTrafficLightDisplay();

        System.out.println("Red light on for 5 sec...");
        light.displayFailure();
        Thread.sleep(5000L);

        System.out.println("Yellow light on for 5 sec...");
        light.displayUnstable();
        Thread.sleep(5000L);

        System.out.println("Green light on for 5 sec...");
        light.displaySuccess();
        Thread.sleep(5000L);

        System.out.println("All lights off...");
        light.lightsOff();
    }
}
