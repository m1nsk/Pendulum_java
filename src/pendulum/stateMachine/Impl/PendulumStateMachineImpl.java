package pendulum.stateMachine.Impl;

import AHRS.Quaternion;
import devices.sensors.dataTypes.CircularArrayRing;
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
    private static Quaternion rotationQ = new Quaternion(Math.sqrt(2) / 2, - Math.sqrt(2) / 2, 0 ,0);
    private static int BUFFER_SIZE = 100;
    private static int MOVE_LIMIT_FLAG = 100;
    private MovementState state = MovementState.SLOW;
    private ImgDisplay imgDisplay;
    private ImgListStorage imgStorage;
    private CircularArrayRing<Quaternion> sampleBuffer = new CircularArrayRing<>(BUFFER_SIZE);
    private CircularArrayRing<DoubleTimeStampedValue> lineValueBuffer = new CircularArrayRing<>(MOVE_LIMIT_FLAG);

    public PendulumStateMachineImpl(ImgDisplay imgDisplay, ImgListStorage imgStorage) {
        this.imgDisplay = imgDisplay;
        this.imgStorage = imgStorage;
        imgDisplay.setImg(imgStorage.current());
    }

    @Override
    public void readNewSample(Quaternion q) throws IOException {
        addNewSample(q);
        Double line = Math.ceil(quaternionToLine(q));
        System.out.println(line);
        lineValueBuffer.add(new DoubleTimeStampedValue(line));
        displayLine(line.intValue());
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
    public void interpolateNewPosition() {
        Double deltaTime = (double) (lineValueBuffer.get(0).getNanoTime() - lineValueBuffer.get(1).getNanoTime()) / DoubleTimeStampedValue.NANOS_PER_SEC;
        lineValueBuffer.get(1);
    }

    protected void displayLine(int line) throws IOException {
        imgDisplay.displayLine(line);
    }

    private void addNewSample(Quaternion q) {
        sampleBuffer.add(q);
    }

    protected double quaternionToLine(Quaternion q) {
        return Quaternion.getYProjectionDegree(Quaternion.multiply(q, rotationQ));
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

    public static void main(String[] args) {
        Quaternion q = new Quaternion(0.707, -0.707, 0, 0);
        Quaternion qs = new Quaternion(0.707, 0.707, 0, 0);
        Quaternion qm = new Quaternion(0.5, 0.5, -0.5, 0.5);
        Quaternion qe = new Quaternion(0, 0, -0.707, 0.707);
        Quaternion qe1 = new Quaternion(0.612, 0.34, 0.669, 0.251);
        Quaternion qe2 = new Quaternion(0.5, 0.5, 0.5, 0.5);
        Quaternion qe3 = new Quaternion(0.707, 0.0, 0.707, 0);
//        System.out.println(Quaternion.getYProjectionDegree(qs));
//        System.out.println(Quaternion.getYProjectionDegree(qm));
//        System.out.println(Quaternion.getYProjectionDegree(qe));
//        System.out.println(Quaternion.getYProjectionDegree(qe1));
//        System.out.println(Quaternion.getYProjectionDegree(qe2));
        System.out.println(Quaternion.getYProjectionDegree(qe3));
    }
}
