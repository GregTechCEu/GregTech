package gregtech.api.graphnet.gather;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.INetLogicEntry;
import gregtech.api.graphnet.logic.MultiNetCountLogic;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.predicate.ShutterPredicate;
import gregtech.api.graphnet.predicate.IEdgePredicate;
import gregtech.common.pipelike.block.cable.CableStructure;
import gregtech.common.pipelike.block.laser.LaserStructure;
import gregtech.common.pipelike.block.optical.OpticalStructure;
import gregtech.common.pipelike.block.pipe.PipeStructure;
import gregtech.common.pipelike.net.energy.VoltageLossLogic;
import gregtech.common.pipelike.net.energy.SuperconductorLogic;
import gregtech.common.pipelike.net.energy.VoltageLimitLogic;

import gregtech.common.pipelike.net.fluid.FluidContainmentLogic;

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
        event.registerPredicate(ShutterPredicate.INSTANCE);
    }

    @SubscribeEvent
    public static void gatherLogics(GatherLogicsEvent event) {
        event.registerLogic(WeightFactorLogic.INSTANCE);
        event.registerLogic(ThroughputLogic.INSTANCE);
        event.registerLogic(ChannelCountLogic.INSTANCE);
        event.registerLogic(MultiNetCountLogic.INSTANCE);
        event.registerLogic(VoltageLossLogic.INSTANCE);
        event.registerLogic(VoltageLimitLogic.INSTANCE);
        event.registerLogic(SuperconductorLogic.INSTANCE);
        event.registerLogic(TemperatureLogic.INSTANCE);
        event.registerLogic(FluidContainmentLogic.INSTANCE);
    }

    @SubscribeEvent
    public static void gatherStructures(GatherStructuresEvent<?> event) {
        if (event.getStructure() == PipeStructure.class) {
            GatherStructuresEvent<PipeStructure> cast = (GatherStructuresEvent<PipeStructure>) event;
            PipeStructure.registerDefaultStructures(cast::registerMaterialStructure);
        }
        if (event.getStructure() == CableStructure.class) {
            GatherStructuresEvent<CableStructure> cast = (GatherStructuresEvent<CableStructure>) event;
            CableStructure.registerDefaultStructures(cast::registerMaterialStructure);
        }
        if (event.getStructure() == OpticalStructure.class) {
            GatherStructuresEvent<OpticalStructure> cast = (GatherStructuresEvent<OpticalStructure>) event;
            OpticalStructure.registerDefaultStructures(cast::registerMaterialStructure);
        }
        if (event.getStructure() == LaserStructure.class) {
            GatherStructuresEvent<LaserStructure> cast = (GatherStructuresEvent<LaserStructure>) event;
            LaserStructure.registerDefaultStructures(cast::registerMaterialStructure);
        }
    }
}
