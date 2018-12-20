package launcher;

import bluetooth.WaitThread;
import observer.EventManager;

public class Launcher {
    private static EventManager eventManager = new EventManager();

    public static void main(String[] args) {
        Thread waitThread = new Thread(new WaitThread(eventManager));
        waitThread.start();
        Thread pendulumThread = new Thread(new Pendulum(eventManager));
        pendulumThread.start();
    }
}
