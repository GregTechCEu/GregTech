package gregtech.api.util.function;

@FunctionalInterface
public interface QuintFunction<P, S, T, Q, U, R> {

    R apply(P p, S s, T t, Q q, U u);
}
