package pendulum;

import AHRS.MotionProcessor;
import lombok.extern.slf4j.Slf4j;
import observer.EventListener;
import observer.EventManager;
import observer.EventType;
import pendulum.display.ImgDisplay;
import pendulum.display.impl.ImgDefaultDisplayImpl;
import pendulum.stateMachine.Impl.PendulumStateMachineImpl;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgListStorage;
import pendulum.storage.Impl.ImgListStorageImpl;
import transmission.device.Device;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "PENDULUM")
public class Pendulum implements Runnable {
    private static EventManager eventManager;
    private static Device device = Device.getInstance();
    private static File storage;
    private static MotionProcessor motionProcessor;

    public Pendulum(EventManager eventManager) {
        log.info("started");
        Pendulum.eventManager = eventManager;
        storage = new StorageFileGetter().getStorage();
    }

    @Override
    public void run() {
        try {
            device.setStorage(storage);
            device.loadFromStorage();
            PendulumParams params = PendulumParams.getInstance();
            ImgListStorage imgStorage = new ImgListStorageImpl(params.getPolarYSize());

            ImgDisplay imgDisplay = new ImgDefaultDisplayImpl(params.getSpiApa102Channel(), params.getSpiAPA102Speed());

            PendulumStateMachine stateMachine = new PendulumStateMachineImpl(imgDisplay, imgStorage);

            eventManager.subscribe(EventType.STORAGE_UPDATED, (EventListener) stateMachine);
            eventManager.subscribe(EventType.MESSAGE_RECEIVE, (EventListener) stateMachine);

            motionProcessor = new MotionProcessor();
            motionProcessor.init();

            long start = System.nanoTime();
            int counter = 0;
            while (true) {
                if(checkTime(params, start)) {
                    if (++counter % 2 == 0) {
                        motionProcessor.imuLoop();
                        stateMachine.readNewSample(motionProcessor.getSample());
                        start = System.nanoTime();
                    } else {
                        stateMachine.readNewSample(motionProcessor.extrapolate());
                        start = System.nanoTime();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean checkTime(PendulumParams params, long start) {
        long delta = TimeUnit.SECONDS.toNanos(1) / params.getPolarYSize();
        return System.nanoTime() - start >= delta;
    }
}
