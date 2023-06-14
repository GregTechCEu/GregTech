package gregtech.loaders.recipe.handlers;

import gregtech.api.unification.ore.handler.IOreProcessorHandler;
import gregtech.api.unification.ore.handler.OreProcessorEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public final class RecipeHandlerList {

    private RecipeHandlerList() {}

    @SubscribeEvent
    public static void registerProcessors(@Nonnull OreProcessorEvent event) {
        if (event.getRegistrationPhase() == IOreProcessorHandler.Phase.REGISTRATION) {
            MaterialRecipeHandler.register();
            OreRecipeHandler.register();
            PartsRecipeHandler.register();
            WireRecipeHandler.register();
            WireCombiningHandler.register();
            PipeRecipeHandler.register();
            ToolRecipeHandler.register();
            PolarizingRecipeHandler.register();
            RecyclingRecipeHandler.register();
        }
    }
}
