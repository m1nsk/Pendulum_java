package pendulum.stateMachine.Impl;

import AHRS.Quaternion;
import devices.sensors.dataTypes.CircularArrayRing;
import pendulum.display.ImgDisplay;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgStorage;

import java.io.IOException;

public class PendulumStateMachineImpl implements PendulumStateMachine {
    private static Quaternion vertQ = new Quaternion((float) Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0);
    private ImgDisplay imgDisplay;
    private ImgStorage imgStorage;
    private int sizeX;
    private int sizeY;
    private CircularArrayRing<Quaternion> sampleBuffer = new CircularArrayRing<>(100);

    public PendulumStateMachineImpl(ImgDisplay imgDisplay, ImgStorage imgStorage, int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.imgDisplay = imgDisplay;
        this.imgStorage = imgStorage;
    }

    @Override
    public void readNewSample(Quaternion q) throws IOException {
        addNewSample(q);
        displayLine(q);
    }

    protected void displayLine(Quaternion q) throws IOException {
        imgDisplay.displayLine(quaternionToFirstLine(q));
    }

    private void addNewSample(Quaternion q) {
        sampleBuffer.add(q);
    }

    protected int quaternionToFirstLine(Quaternion q) {
        return (int)(this.sizeX /2 + new Quaternion(q).multiply(vertQ).getD() * 2 * 90 / Math.sqrt(2));
    }

    protected int quaternionToSecondLine(Quaternion q) {
        return (int)(this.sizeX /2 - new Quaternion(q).multiply(vertQ).getD() * 2 * 90 / Math.sqrt(2));
    }
}
