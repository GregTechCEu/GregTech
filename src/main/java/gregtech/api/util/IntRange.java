package gregtech.api.util;

import java.util.Objects;

public class IntRange {

    public final int minValue;
    public final int maxValue;

    public IntRange(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public boolean isInsideOf(int value) {
        return value >= minValue && value <= maxValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntRange)) return false;
        IntRange intRange = (IntRange) o;
        return minValue == intRange.minValue &&
                maxValue == intRange.maxValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minValue, maxValue);
    }

    @Override
    public String toString() {
        return "IntRange{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}
