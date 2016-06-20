package de.zalando.buildstatus;

public class TrafficLightBlinkingManualTest {

    public static void main(String[] args) throws Exception {

        LinuxClewareTrafficLight light = new LinuxClewareTrafficLight();

        System.out.println("start flashing red light for 10 seconds...");
        light.displayFailure(true);

        for(int i=1; i<=10; i++) {
            System.out.println(i);
            Thread.sleep(1000);
        }

        System.out.println("start flashing yellow light for 10 seconds...");
        light.displayUnstable(true);

        for(int i=1; i<=10; i++) {
            System.out.println(i);
            Thread.sleep(1000);
        }

        System.out.println("start flashing green light for 10 seconds...");
        light.displaySuccess(true);

        for(int i=1; i<=10; i++) {
            System.out.println(i);
            Thread.sleep(1000);
        }

        System.out.println("turning light off");
        light.lightsOff();
    }
}
