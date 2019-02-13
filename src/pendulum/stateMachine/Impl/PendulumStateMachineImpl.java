package pendulum.stateMachine.Impl;

import observer.EventListener;
import observer.EventType;
import pendulum.display.ImgDisplay;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgListStorage;
import observer.Command;
import observer.CommandQueue;
import observer.CommandType;

import java.io.IOException;

public class PendulumStateMachineImpl implements PendulumStateMachine, EventListener {
    private ImgDisplay imgDisplay;
    private ImgListStorage imgStorage;

    public PendulumStateMachineImpl(ImgDisplay imgDisplay, ImgListStorage imgStorage) {
        this.imgDisplay = imgDisplay;
        this.imgStorage = imgStorage;
        imgDisplay.setImg(imgStorage.current());
    }

    @Override
    public void readNewSample(Double degree) throws IOException {
//        System.out.println(degree);
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

    private void displayLine(Double line) throws IOException {
        imgDisplay.displayLine(line);
    }

    @Override
    public void update(EventType type) {
        if(type.equals(EventType.STORAGE_UPDATED)) {
            imgStorage.loadData();
            imgDisplay.setImg(imgStorage.current());
        } else if (type.equals(EventType.MESSAGE_RECEIVE)) {
            Command command = CommandQueue.peek();
            if(command.getType().equals(CommandType.IMAGE)) {
                String name = command.getArgs().get("name");
                imgStorage.chooseImgByName(name);
                imgDisplay.setImg(imgStorage.current());
            }
        }
    }
}
