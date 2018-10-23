package pendulum.stateMachine.Impl;

import AHRS.Quaternion;
import devices.sensors.dataTypes.CircularArrayRing;
import pendulum.display.ImgDisplay;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgStorage;

import java.io.IOException;
import java.util.List;

public class PendulumStateMachineImpl implements PendulumStateMachine {
    private static Quaternion vertQ = new Quaternion((float) Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0);
    private List<ImgDisplay> imgDisplayList;
    private ImgStorage imgStorage;
    private int sizeX;
    private int sizeY;
    private CircularArrayRing<Quaternion> sampleBuffer = new CircularArrayRing<>(100);

    public PendulumStateMachineImpl(List<ImgDisplay> imgDisplayList, ImgStorage imgStorage, int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.imgDisplayList = imgDisplayList;
        this.imgStorage = imgStorage;
    }

    @Override
    public void readNewSample(Quaternion q) throws IOException {
        System.out.println(quaternionToLine(q));
        addNewSample(q);
        displayLine(q);
    }

    protected void displayLine(Quaternion q) throws IOException {
        for(ImgDisplay imgDisplay: imgDisplayList){
            imgDisplay.displayLine(quaternionToLine(q));
        }
    }

    private void addNewSample(Quaternion q) {
        sampleBuffer.add(q);
    }

    protected int quaternionToLine(Quaternion q) {
        return (int)(Quaternion.multiply(q, vertQ).getZp() * 2 * 90 / Math.sqrt(2));
    }
}
