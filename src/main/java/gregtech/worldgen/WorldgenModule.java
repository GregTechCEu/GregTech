package gregtech.worldgen;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.common.ConfigHolder;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;

import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.*;

@GregTechModule(
                moduleID = GregTechModules.MODULE_WORLDGEN,
                containerID = GTValues.MODID,
                name = "GregTech Worldgen",
                description = "GregTech Worldgen Module.")
public class WorldgenModule extends BaseGregTechModule {

    public static final Logger LOGGER = LogManager.getLogger("GregTech Worldgen");

    private static final Set<OreGenEvent.GenerateMinable.EventType> VANILLA_ORE_GEN_EVENT_TYPES = EnumSet.of(
            COAL, DIAMOND, GOLD, IRON, LAPIS, REDSTONE, QUARTZ, EMERALD);

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public @NotNull List<Class<?>> getOreGenBusSubscribers() {
        return Collections.singletonList(WorldgenModule.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGenerateMineable(@NotNull OreGenEvent.GenerateMinable event) {
        if (ConfigHolder.worldgen.disableVanillaOres && VANILLA_ORE_GEN_EVENT_TYPES.contains(event.getType())) {
            event.setResult(Event.Result.DENY);
        }
    }
}
