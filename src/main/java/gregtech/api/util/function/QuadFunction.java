package gregtech.api.util.function;

@FunctionalInterface
public interface QuadFunction<P, S, T, Q, R> {

    R apply(P p, S s, T t, Q q);
}
