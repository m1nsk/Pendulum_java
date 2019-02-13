package AHRS;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Quaternion {

    private double wp, xp, yp, zp;

    public Quaternion(double wp, double xp, double yp, double zp) {
        this.wp = wp;
        this.xp = xp;
        this.yp = yp;
        this.zp = zp;
    }

    public Quaternion() {
        this(0, 0, 0, 0);
    }

    public Quaternion(Quaternion q) {
        wp = q.getWp();
        xp = q.getXp();
        yp = q.getYp();
        zp = q.getZp();
    }

    public Quaternion(double[] data) {
        this.wp = data[0];
        this.xp = data[0];
        this.yp = data[0];
        this.zp = data[0];
    }

    public void normalize() {
        float norm;
        // Normalise accelerometer measurement
        norm = (float) Math.sqrt(wp * wp + xp * xp + yp * yp + zp * zp);
        if (norm == 0.0f) {
            throw new ArithmeticException(); // handle NaN
        }
        norm = 1f / norm;
        wp *= norm;
        xp *= norm;
        yp *= norm;
        zp *= norm;
    }

    public void setAll(double a, double b, double c, double d) {
        this.wp = a;
        this.xp = b;
        this.yp = c;
        this.zp = d;
    }

    public static Quaternion multiply(Quaternion q1, Quaternion q2) {
        double yy = (q1.wp - q1.yp) * (q2.wp + q2.zp);
        double zz = (q1.wp + q1.yp) * (q2.wp - q2.zp);
        double ww = (q1.zp + q1.xp) * (q2.xp + q2.yp);
        double xx = ww + yy + zz;
        double qq = 0.5f * (xx + (q1.zp - q1.xp) * (q2.xp - q2.yp));

        double w = qq - ww + (q1.zp - q1.yp) * (q2.yp - q2.zp);
        double x = qq - xx + (q1.xp + q1.wp) * (q2.xp + q2.wp);
        double y = qq - yy + (q1.wp - q1.xp) * (q2.yp + q2.zp);
        double z = qq - zz + (q1.zp + q1.yp) * (q2.wp - q2.xp);
        return new Quaternion(w, x, y, z);
    }

    public static double scalar(Quaternion q1, Quaternion q2) {
        return q1.getXp() * q2.getXp() + q1.getYp() * q2.getYp() + q1.getZp() * q2.getZp() + q1.getWp() * q2.getWp();
    }

    public static double getYProjectionDegree(Quaternion q) {
        double v = q.wp * Math.sqrt((q.wp * q.wp + q.xp * q.xp + q.yp * q.yp + q.zp * q.zp) / (q.wp * q.wp + q.yp * q.yp));
        return Math.toDegrees(Math.acos(v) * 2) - 90;
    }

    @Override
    public String toString() {
        return "Quaternion{" +
                "wp=" + wp +
                ", xp=" + xp +
                ", yp=" + yp +
                ", zp=" + zp +
                '}';
    }
}

