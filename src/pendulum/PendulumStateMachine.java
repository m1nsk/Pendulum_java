package pendulum;

import AHRS.Quaternion;
import devices.sensors.dataTypes.CircularArrayRing;

import java.io.IOException;

public class PendulumStateMachine {
    private PendulumParams params;
    private ImgDisplay imgDisplay;
    private ImgStorage imgStorage;
    private CircularArrayRing<Quaternion> sampleBuffer = new CircularArrayRing<>(100);

    public PendulumStateMachine(PendulumParams params, ImgDisplay imgDisplay, ImgStorage imgStorage) {
        this.params = params;
        this.imgDisplay = imgDisplay;
        this.imgStorage = imgStorage;
        imgDisplay.setImg(imgStorage.getImg("img")); // TODO: think about state machine realization
    }

    public void getNewSample(Quaternion q) throws IOException {
        addNewSample(q);
        imgDisplay.displayLine(PendulumUtils.quaterionToLine(q));
    }

    private void addNewSample(Quaternion q) {
        sampleBuffer.add(q);
    }
}
