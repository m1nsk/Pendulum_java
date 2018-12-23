package transmission.Protocol;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class CommandQueue {
    private static Queue<Command> commands = new LinkedBlockingDeque<>();

    public static Command element() {
        return commands.element();
    }

    public static boolean offer(Command command) {
        return commands.offer(command);
    }

    public static Command poll() {
        return commands.poll();
    }

    public static Command peek() {
        return commands.peek();
    }
}
