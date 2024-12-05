package gregtech.api.util.function;

@FunctionalInterface
public interface ToBooleanFunction<T> {

    boolean applyAsBool(T value);
}
