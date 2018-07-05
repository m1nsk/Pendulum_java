package pendulum;

import AHRS.Quaternion;

public class PendulumUtils {
    private static Quaternion vertQ = new Quaternion((float) Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0);
    public static int quaternionToLine(Quaternion q) {
        return (int)(new Quaternion(q).multiply(vertQ).getD() * 2 * 90 / Math.sqrt(2));
    }
}
