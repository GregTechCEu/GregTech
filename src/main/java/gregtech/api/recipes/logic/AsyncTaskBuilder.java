package gregtech.api.recipes.logic;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AsyncTaskBuilder<T> {

    protected static final Executor executor = Executors.newWorkStealingPool(4);

    protected final T shared;

    protected final List<Object> tasks = new ObjectArrayList<>();

    public AsyncTaskBuilder(T shared) {
        this.shared = shared;
    }

    @Contract("_->this")
    public AsyncTaskBuilder<T> addTask(@NotNull Consumer<T> task) {
        this.tasks.add(task);
        return this;
    }

    @Contract("_,_->this")
    public <V> AsyncTaskBuilder<T> addTask(@NotNull BiConsumer<T, V> task, V accept) {
        this.tasks.add(task);
        this.tasks.add(accept);
        return this;
    }

    @Contract("_->this")
    public AsyncTaskBuilder<T> addTask(@NotNull UnaryOperator<T> task) {
        this.tasks.add(task);
        return this;
    }

    public CompletableFuture<T> build() {
        return CompletableFuture.supplyAsync(new DataObject(shared, tasks), executor);
    }

    protected final class DataObject implements Supplier<T> {

        protected T shared;

        protected final List<Object> tasks;

        protected DataObject(T shared, List<Object> tasks) {
            this.shared = shared;
            this.tasks = tasks;
        }

        @Override
        public T get() {
            for (int i = 0; i < tasks.size(); i++) {
                Object task = tasks.get(i);
                if (task instanceof BiConsumer<?,?> c) {
                    helper((BiConsumer<T, ?>) c, tasks.get(++i));
                } else if (task instanceof UnaryOperator<?> u) {
                    shared = ((UnaryOperator<T>) u).apply(shared);
                } else if (task instanceof Consumer<?> c) {
                    ((Consumer<T>) c).accept(shared);
                }
            }
            return shared;
        }

        private <V> void helper(BiConsumer<T, V> consumer, Object v) {
            consumer.accept(shared, (V) v);
        }
    }

}
