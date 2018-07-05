package pendulum;

import AHRS.Quaternion;
import devices.sensors.dataTypes.CircularArrayRing;

import java.io.IOException;

public class PendulumStateMachine {
    private static Quaternion vertQ = new Quaternion((float) Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0);
    private ImgDisplay imgDisplay;
    private ImgStorage imgStorage;
    private int sizeX;
    private int sizeY;
    private CircularArrayRing<Quaternion> sampleBuffer = new CircularArrayRing<>(100);

    public PendulumStateMachine(ImgDisplay imgDisplay, ImgStorage imgStorage, int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.imgDisplay = imgDisplay;
        this.imgStorage = imgStorage;
        imgDisplay.setImg(this.imgStorage.getImg("img")); // TODO: think about state machine realization
    }

    public void getNewSample(Quaternion q) throws IOException {
        addNewSample(q);
        imgDisplay.displayLine(quaternionToLine(q));
    }

    private void addNewSample(Quaternion q) {
        sampleBuffer.add(q);
    }

    public int quaternionToLine(Quaternion q) {
        return (int)(this.sizeX /2 + new Quaternion(q).multiply(vertQ).getD() * 2 * 90 / Math.sqrt(2));
    }
}
