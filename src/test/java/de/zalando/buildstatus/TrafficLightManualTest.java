package de.zalando.buildstatus;

public class TrafficLightManualTest {

    public static void main(String[] args) throws Exception {

        LinuxClewareTrafficLight light = new LinuxClewareTrafficLight();

        System.out.println("Red light on for 5 sec...");
        light.displayFailure(false);
        Thread.sleep(5000L);

        System.out.println("Yellow light on for 5 sec...");
        light.displayUnstable(false);
        Thread.sleep(5000L);

        System.out.println("Green light on for 5 sec...");
        light.displaySuccess(false);
        Thread.sleep(5000L);

        System.out.println("All lights off...");
        light.lightsOff();
    }
}
