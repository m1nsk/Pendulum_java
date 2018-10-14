package AHRS;

public class Quaternion {

    private float a, b, c, d;

    public Quaternion(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Quaternion() {
        this(0, 0, 0, 0);
    }

    public Quaternion(Quaternion q) {
        a = q.getA();
        b = q.getB();
        c = q.getC();
        d = q.getD();
    }

    public Quaternion(float[] data) {
        this.a = data[0];
        this.b = data[0];
        this.c = data[0];
        this.d = data[0];
    }

    public void normalize() {
        float norm;
        // Normalise accelerometer measurement
        norm = (float) Math.sqrt(a * a + b * b + c * c + d * d);
        if (norm == 0.0f) {
            throw new ArithmeticException(); // handle NaN
        }
        norm = 1f / norm;
        a *= norm;
        b *= norm;
        c *= norm;
        d *= norm;
    }

    public void setAll(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Quaternion multiply(Quaternion b) {
        Quaternion res = new Quaternion();
        res.a = this.a * this.a - this.b * b.b - this.c * b.c - this.d * b.d;
        res.b = this.a * b.b + this.b * b.a + this.c * b.d - this.d * b.c;
        res.c = this.a * b.c - this.b * b.d + this.c * b.a + this.d * b.b;
        res.d = this.a * b.d + this.b * b.c - this.c * b.b + this.d * b.a;
        return res;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    public void setC(float c) {
        this.c = c;
    }

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    @Override
    public String toString() {
        return "Quaternion{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                '}';
    }
}
