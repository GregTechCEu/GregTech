package gregtech.loaders.recipe.handlers;

import gregtech.api.unification.ore.handler.OreProcessorManager;
import gregtech.api.unification.ore.handler.OreProcessorEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
public final class RecipeHandlerList {

    private RecipeHandlerList() {}

    @SubscribeEvent
    public static void registerProcessors(@NotNull OreProcessorEvent event) {
        if (event.registrationPhase() == OreProcessorManager.Phase.REGISTRATION) {
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
