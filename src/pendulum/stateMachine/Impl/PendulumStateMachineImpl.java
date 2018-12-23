package pendulum.stateMachine.Impl;

import AHRS.Quaternion;
import observer.EventType;
import devices.sensors.dataTypes.CircularArrayRing;
import observer.EventListener;
import pendulum.display.ImgDisplay;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgListStorage;
import transmission.Protocol.Command;
import transmission.Protocol.CommandQueue;
import transmission.Protocol.CommandType;

import java.io.IOException;
import java.util.List;

public class PendulumStateMachineImpl implements PendulumStateMachine, EventListener {
    private static Quaternion vertQ = new Quaternion((float) Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0);
    private static int BUFFER_SIZE = 100;
    private static int MOVE_LIMIT_FLAG = 100;
    private MovementState state = MovementState.SLOW;
    private List<ImgDisplay> imgDisplayList;
    private ImgListStorage imgStorage;
    private CircularArrayRing<Quaternion> sampleBuffer = new CircularArrayRing<>(BUFFER_SIZE);
    private CircularArrayRing<Integer> lineValueBuffer = new CircularArrayRing<>(MOVE_LIMIT_FLAG);

    public PendulumStateMachineImpl(List<ImgDisplay> imgDisplayList, ImgListStorage imgStorage) {
        this.imgDisplayList = imgDisplayList;
        this.imgStorage = imgStorage;
        imgDisplayList.forEach(imgDisplay -> imgDisplay.setImg(imgStorage.current()));
    }

    @Override
    public void readNewSample(Quaternion q) throws IOException {
        addNewSample(q);
        int line = quaternionToLine(q);
        lineValueBuffer.add(line);
        if(checkTurn()) {
            imgDisplayList.forEach(imgDisplay -> imgDisplay.setImg(imgStorage.next()));
        }
        displayLine(line);
    }

    @Override
    public void nextImg() {
        imgStorage.next();
    }

    @Override
    public void previousImg() {
        imgStorage.previous();
    }

    protected void displayLine(int line) throws IOException {
        for(ImgDisplay imgDisplay: imgDisplayList){
            imgDisplay.displayLine(line);
        }
    }

    private void addNewSample(Quaternion q) {
        sampleBuffer.add(q);
    }

    protected int quaternionToLine(Quaternion q) {
        return (int) (90 + Quaternion.multiply(q, vertQ).getXp() * 2 * 90 / Math.sqrt(2));
    }

    private boolean checkTurn() {
        int windowSize = 20;
        int moveTurnLimit = 7;
        if(lineValueBuffer.size() > windowSize) {
            int size = lineValueBuffer.size() - 1;
            int start = lineValueBuffer.get(size - windowSize);
            int middle = lineValueBuffer.get(size - windowSize / 2);
            int end = lineValueBuffer.get(size);
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
            imgDisplayList.forEach(display -> display.setImg(imgStorage.current()));
        } else if (type.equals(EventType.MESSAGE_RECEIVE)) {
            Command command = CommandQueue.peek();
            if(command.getType().equals(CommandType.IMAGE)) {
                command = CommandQueue.poll();
                String name = command.getArgs().get("name");
                imgStorage.chooseImgByName(name);
                imgDisplayList.forEach(display -> display.setImg(imgStorage.current()));
            }
        }
    }

    enum MovementState {
        RIGHT, LEFT, SLOW
    }
}
