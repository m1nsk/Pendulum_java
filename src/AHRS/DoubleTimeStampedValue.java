package AHRS;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class DoubleTimeStampedValue {

    public DoubleTimeStampedValue(double value) {
        this.value = value;
        this.nanoTime = System.nanoTime();
    }

    public static final long NANOS_PER_SEC = 1000000000;
    private double value;
    private long nanoTime;
}
