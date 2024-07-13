package gregtech.api.graphnet.gather;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.INetLogicEntry;
import gregtech.api.graphnet.logic.TemperatureLogic;
import gregtech.common.pipelike.block.cable.CableStructure;
import gregtech.common.pipelike.net.energy.LossAbsoluteLogic;
import gregtech.api.graphnet.logic.MultiNetCountLogic;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;

import gregtech.api.graphnet.predicate.IEdgePredicate;

import gregtech.common.pipelike.net.energy.VoltageLimitLogic;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public final class GTGraphGatherables {

    private static Map<String, Supplier<IEdgePredicate<?, ?>>> PREDICATES_REGISTRY;
    private static Map<String, Supplier<INetLogicEntry<?, ?>>> LOGICS_REGISTRY;

    public static Map<String, Supplier<IEdgePredicate<?, ?>>> getPredicatesRegistry() {
        if (PREDICATES_REGISTRY == null) {
            GatherPredicatesEvent predicates = new GatherPredicatesEvent();
            MinecraftForge.EVENT_BUS.post(predicates);
            PREDICATES_REGISTRY = predicates.gathered;
        }
        return PREDICATES_REGISTRY;
    }

    public static Map<String, Supplier<INetLogicEntry<?, ?>>> getLogicsRegistry() {
        if (LOGICS_REGISTRY == null) {
            GatherLogicsEvent logics = new GatherLogicsEvent();
            MinecraftForge.EVENT_BUS.post(logics);
            LOGICS_REGISTRY = logics.gathered;
        }
        return LOGICS_REGISTRY;
    }

    @SubscribeEvent
    public static void gatherPredicates(GatherPredicatesEvent event) {
    }

    @SubscribeEvent
    public static void gatherLogics(GatherLogicsEvent event) {
        event.registerLogic(WeightFactorLogic.INSTANCE);
        event.registerLogic(ThroughputLogic.INSTANCE);
        event.registerLogic(ChannelCountLogic.INSTANCE);
        event.registerLogic(MultiNetCountLogic.INSTANCE);
        event.registerLogic(LossAbsoluteLogic.INSTANCE);
        event.registerLogic(VoltageLimitLogic.INSTANCE);
        event.registerLogic(TemperatureLogic.INSTANCE);
    }

    @SubscribeEvent
    public static void gatherMaterialStructures(GatherMaterialStructuresEvent event) {
        CableStructure.registerDefaultStructures(event::registerMaterialStructure);
    }
}
