package gregtech.api.recipes.loader;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

/**
 * @see IRecipeLoader
 *
 * Cancel this event to disable recipes from being loaded.
 * This can easily break progression, so use with caution.
 */
@Cancelable
public class GTRecipeLoadingEvent extends Event {

    public final IRecipeLoader loader;
    public final String modid;

    /**
     * @param loader the current recipe loader handling recipes
     * @param modid  the modid of the mod loading the recipes
     */
    public GTRecipeLoadingEvent(@Nonnull IRecipeLoader loader, @Nonnull String modid) {
        this.loader = loader;
        this.modid = modid;
    }
}
