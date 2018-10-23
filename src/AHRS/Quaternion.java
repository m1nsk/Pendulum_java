package AHRS;

public class Quaternion {

    private float wp, xp, yp, zp;

    public Quaternion(float wp, float xp, float yp, float zp) {
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

    public Quaternion(float[] data) {
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

    public void setAll(float a, float b, float c, float d) {
        this.wp = a;
        this.xp = b;
        this.yp = c;
        this.zp = d;
    }

    public static Quaternion multiply(Quaternion q1, Quaternion q2) {
        float yy = (q1.wp - q1.yp) * (q2.wp + q2.zp);
        float zz = (q1.wp + q1.yp) * (q2.wp - q2.zp);
        float ww = (q1.zp + q1.xp) * (q2.xp + q2.yp);
        float xx = ww + yy + zz;
        float qq = 0.5f * (xx + (q1.zp - q1.xp) * (q2.xp - q2.yp));

        float w = qq - ww + (q1.zp - q1.yp) * (q2.yp - q2.zp);
        float x = qq - xx + (q1.xp + q1.wp) * (q2.xp + q2.wp);
        float y = qq - yy + (q1.wp - q1.xp) * (q2.yp + q2.zp);
        float z = qq - zz + (q1.zp + q1.yp) * (q2.wp - q2.xp);
        
        return new Quaternion(w, x, y, z);
    }

    public float getWp() {
        return wp;
    }

    public void setWp(float wp) {
        this.wp = wp;
    }

    public float getXp() {
        return xp;
    }

    public void setXp(float xp) {
        this.xp = xp;
    }

    public float getYp() {
        return yp;
    }

    public void setYp(float yp) {
        this.yp = yp;
    }

    public float getZp() {
        return zp;
    }

    public void setZp(float zp) {
        this.zp = zp;
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
