package pendulum.stateMachine.Impl;

import devices.sensors.dataTypes.CircularArrayRing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import observer.EventListener;
import observer.EventType;
import pendulum.display.ImgDisplay;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgListStorage;
import transmission.Protocol.Command;
import transmission.Protocol.CommandQueue;
import transmission.Protocol.CommandType;

import java.io.IOException;

public class PendulumStateMachineImpl implements PendulumStateMachine, EventListener {
    private static int MOVE_LIMIT_FLAG = 100;
    private MovementState state = MovementState.SLOW;
    private ImgDisplay imgDisplay;
    private ImgListStorage imgStorage;
    private CircularArrayRing<DoubleTimeStampedValue> lineValueBuffer = new CircularArrayRing<>(MOVE_LIMIT_FLAG);

    public PendulumStateMachineImpl(ImgDisplay imgDisplay, ImgListStorage imgStorage) {
        this.imgDisplay = imgDisplay;
        this.imgStorage = imgStorage;
        imgDisplay.setImg(imgStorage.current());
    }

    @Override
    public void readNewSample(Double degree) throws IOException {
        System.out.println(degree);
        lineValueBuffer.add(new DoubleTimeStampedValue(degree));
        displayLine(degree);
    }

    @Override
    public void nextImg() {
        imgStorage.next();
    }

    @Override
    public void previousImg() {
        imgStorage.previous();
    }

    @Override
    public void extrapolate() throws IOException {
        long now  = System.nanoTime();
        if(lineValueBuffer.size() > 1) {
            DoubleTimeStampedValue prevData = lineValueBuffer.get(0);
            DoubleTimeStampedValue beforePrevData = lineValueBuffer.get(1);
            double speed = (prevData.value - beforePrevData.value) / (prevData.nanoTime - beforePrevData.nanoTime);
            double extrapolatedDegree = prevData.value + speed * (now - prevData.nanoTime);
            System.out.println(extrapolatedDegree + " ex");
            displayLine(extrapolatedDegree);
        }
    }

    protected void displayLine(Double line) throws IOException {
        imgDisplay.displayLine(line);
    }

    private boolean checkTurn() {
        int windowSize = 20;
        int moveTurnLimit = 7;
        if(lineValueBuffer.size() > windowSize) {
            int size = lineValueBuffer.size() - 1;
            double start = lineValueBuffer.get(size - windowSize).getValue();
            double middle = lineValueBuffer.get(size - windowSize / 2).getValue();
            double end = lineValueBuffer.get(size).getValue();
            if(Math.abs(start - end) < moveTurnLimit) {
                state = MovementState.SLOW;
            }
            if((start - middle) * (middle - end) > 0)
                return false;
            if(Math.abs(start - middle) < moveTurnLimit || Math.abs(middle - start) < moveTurnLimit)
                return false;
            if(Math.abs(Math.abs(start - middle) - Math.abs(middle - end)) < 100) {
                switch (state) {
                    case LEFT: {
                        state = MovementState.RIGHT;
                        lineValueBuffer.clear();
                        return true;
                    }
                    case RIGHT: {
                        state = MovementState.LEFT;
                        lineValueBuffer.clear();
                        return true;
                    }
                    case SLOW: {
                        state = middle - end > 0 ? MovementState.LEFT : MovementState.RIGHT;
                        lineValueBuffer.clear();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void update(EventType type) {
        if(type.equals(EventType.STORAGE_UPDATED)) {
            imgStorage.loadData();
            imgDisplay.setImg(imgStorage.current());
        } else if (type.equals(EventType.MESSAGE_RECEIVE)) {
            Command command = CommandQueue.peek();
            if(command.getType().equals(CommandType.IMAGE)) {
                command = CommandQueue.poll();
                String name = command.getArgs().get("name");
                imgStorage.chooseImgByName(name);
                imgDisplay.setImg(imgStorage.current());
            }
        }
    }

    enum MovementState {
        RIGHT, LEFT, SLOW
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public class DoubleTimeStampedValue {

        public DoubleTimeStampedValue(double value) {
            this.value = value;
            this.nanoTime = System.nanoTime();
        }

        public static final long NANOS_PER_SEC = 1000000000;
        private double value;
        private long nanoTime;
    }
}
