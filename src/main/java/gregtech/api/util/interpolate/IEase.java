package gregtech.api.util.interpolate;

/**
 * Object representation of an easing function.
 * <p/>
 * Easing functions describe numerical change rate of values, on a timescale of {@code 0 ~ 1}.
 * 
 * @see Eases
 */
@FunctionalInterface
public interface IEase {

    /**
     * Get change rate of values on specific time {@code t}.
     *
     * @param t Specific time to sample the rate. Value in a range of {@code 0 ~ 1} is expected.
     * @return Numerical value interpolated by the easing function. The returned value has range of {@code 0 ~ 1}.
     */
    float getInterpolation(float t);
}
