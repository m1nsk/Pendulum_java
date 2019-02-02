package AHRS;

public class QuaternionUtils {
    private static Quaternion rotationQ = new Quaternion(Math.sqrt(2) / 2, - Math.sqrt(2) / 2, 0 ,0);

    public static Double quaternionToDegree(Quaternion q) {
        return Quaternion.getYProjectionDegree(Quaternion.multiply(q, rotationQ));
    }

}
