package gregtech.api.task;

/**
 * A framework for creating tasks that don't need to store state, and can call methods or mutate their executor as they are progressed.
 * @param <E> the type of the executor that must be provided in order to progress and complete the task.
 */
public interface ProgressibleTask<E> {

    double getRequiredProgress();

    /**
     * Called when this task is progressed.
     * @param executor the object currently progressing this task.
     * @param oldProgress the progress of the task, as it was.
     * @param newProgress the progress of the task, updated.
     */
    void progressTask(E executor, double oldProgress, double newProgress);

    /**
     * Called when this task is completed. Should always be called <i>after</i> a call to
     * {@link #progressTask(Object, double, double)}
     * @param executor the object currently progressing this task.
     */
    void completeTask(E executor);
}
