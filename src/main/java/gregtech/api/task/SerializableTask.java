package gregtech.api.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of the {@link ProgressibleTask} to provide support for serialization.
 */
public interface SerializableTask<E> extends ProgressibleTask<E> {

    @NotNull SerializableTaskDefinition<?, E> getDefinition();

    default <V> @Nullable SerializableTask<V> attemptMakeCompatible(V executor) {
        return getDefinition().castTask(executor, this);
    }
}
