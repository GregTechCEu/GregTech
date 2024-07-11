package gregtech.api.util.function;

@FunctionalInterface
public interface TriFunction<P, S, T, R> {

    R apply(P p, S s, T t);
}
