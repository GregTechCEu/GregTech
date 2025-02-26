package gregtech.api.util.function;

@FunctionalInterface
public interface QuadConsumer<T, U, S, G> {

    void accept(T t, U u, S s, G g);
}
