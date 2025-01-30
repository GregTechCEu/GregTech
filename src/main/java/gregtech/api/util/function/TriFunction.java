package gregtech.api.util.function;

@FunctionalInterface
public interface TriFunction<T, U, Z, R> {

    R apply(T t, U u, Z z);
}
