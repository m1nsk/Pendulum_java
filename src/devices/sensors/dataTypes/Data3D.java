package devices.sensors.dataTypes;

public class Data3D{

    private float x;
    private float y;
    private float z;

    public Data3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void normalize(){
		double norm;
		// Normalise measurements
		norm = getModule();
		if (norm == 0.0f)
			throw new ArithmeticException(); // handle NaN
		norm = 1f / norm;
		x *= norm;
		y *= norm;
		z *= norm;
	}

	public Double getModule() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String toString()
	{
		final String format = "%+04.3f";
		return 	super.toString() + " z: " + String.format(format,z);
	}
    public Data3D clone()
    {
        return new Data3D(x,y,z);
    }
}
