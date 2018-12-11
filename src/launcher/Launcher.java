package launcher;

import Observer.EventType;
import bluetooth.WaitThread;
import Observer.EventManager;

public class Launcher {
    private static EventManager eventManager = new EventManager();

    public static void main(String[] args) {
        Thread waitThread = new Thread(new WaitThread(eventManager));
        waitThread.start();
        Thread pendulumThread = new Thread(new Pendulum(eventManager));
        pendulumThread.start();
    }
}
