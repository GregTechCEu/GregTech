package gregtech.api.util.function;

@FunctionalInterface
public interface QuadConsumer<P, S, T, Q> {

    void accept(P p, S s, T t, Q q);
}
