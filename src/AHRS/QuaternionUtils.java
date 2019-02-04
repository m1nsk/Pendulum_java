package AHRS;

public class QuaternionUtils {
    private static final Quaternion rotationQ = new Quaternion(Math.sqrt(2) / 2, - Math.sqrt(2) / 2, 0 ,0);
    private static final Quaternion vertQ = new Quaternion(0.707, 0, -0.707 ,0);
    private static Quaternion closest = new Quaternion(1, 0, 0, 0);

    public static Double quaternionToDegree(Quaternion q) {
        Quaternion candidate = Quaternion.multiply(q, vertQ);
        if(Math.abs(candidate.getWp()) < Math.abs(closest.getWp())) {
            closest = candidate;
            System.out.println(closest.getWp() + " closest");
        }
        return Quaternion.getYProjectionDegree(Quaternion.multiply(q, rotationQ));
    }

}
