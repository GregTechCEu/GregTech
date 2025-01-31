package gregtech.api.task;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class SerializableTaskCastTest {

    @Test
    public void test() {
        SerializableTaskDefinition<SerializableTask<Number>, Number> definition = new SerializableTaskDefinition<>(Number.class);
        SerializableTask<Number> task = new SerializableTask<>() {

            @Override
            public @NotNull SerializableTaskDefinition<?, Number> getDefinition() {
                return definition;
            }

            @Override
            public double getRequiredProgress() {
                return 0;
            }

            @Override
            public void progressTask(Number executor, double oldProgress, double newProgress) {
                executor.byteValue();
            }

            @Override
            public void completeTask(Number executor) {
                executor.byteValue();
            }
        };
        Integer i = 10;
        Object obj = new Object();
        SerializableTask<Integer> cast = definition.castTask(i, task);
        assert cast != null;
        cast.progressTask(i, 0, 0);
        SerializableTask<Object> castFail = definition.castTask(obj, task);
        assert castFail == null;
    }
}
