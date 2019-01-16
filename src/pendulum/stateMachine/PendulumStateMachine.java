package pendulum.stateMachine;

import AHRS.Quaternion;

import java.io.IOException;

public interface PendulumStateMachine {
    void readNewSample(Quaternion q) throws IOException;
    void nextImg();
    void previousImg();

    void interpolateNewPosition();
}
