package gregtech.api.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SerializableTaskDefinition<T extends SerializableTask<E>, E> {

    private final @NotNull Class<E> executorClass;

    public SerializableTaskDefinition(@NotNull Class<E> executorClass) {
        this.executorClass = executorClass;
    }

    public <V> @Nullable SerializableTask<V> castTask(V executor, SerializableTask<E> task) {
        if (executorClass.isInstance(executor)) {
            // this should be safe, because if we know that the executor is an instance of the E generic,
            // then casting to the V generic is greater specificity, and the SerializableTask interface only takes in
            // instances of the executor, not returns them.
            //noinspection unchecked
            return (SerializableTask<V>) task;
        }
        return null;
    }
}
