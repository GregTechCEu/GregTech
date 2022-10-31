package gregtech.api.recipes.loader;

import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

/**
 * Used to allow other mods to control which recipes are registered.
 * Use the {@link GTRecipeLoadingEvent} to determine which recipes to cancel.
 */
public interface IRecipeLoader {

    /**
     * @return the modid loading the recipes
     */
    @Nonnull
    String getModid();

    /**
     * @return true if this loader should register recipes
     */
    default boolean shouldRegister() {
        return !MinecraftForge.EVENT_BUS.post(new GTRecipeLoadingEvent(this, getModid()));
    }

    /**
     * Runs the runnable if {@link IRecipeLoader#shouldRegister()} is true
     *
     * @param runnable a runnable to run
     */
    default void register(@Nonnull Runnable runnable) {
        if (shouldRegister()) {
            runnable.run();
        }
    }

    /**
     * Cancel the event if attempting to use this loader
     * <p>
     * Example: {@code GTRecipeLoaders.RECYCLING.cancel(event)} in an event bus subscriber
     *
     * @param event the event to cancel
     */
    default void cancel(@Nonnull GTRecipeLoadingEvent event) {
        if (event.loader == this) event.setCanceled(true);
    }
}
