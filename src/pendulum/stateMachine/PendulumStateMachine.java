package pendulum.stateMachine;

import java.io.IOException;

public interface PendulumStateMachine {
    void readNewSample(Double degree) throws IOException;
    void nextImg();
    void previousImg();
}
