package gregtech.common.pipelikeold.fluidpipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenetold.WorldPipeNetComplex;
import gregtech.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.FluidFilterMode;
import gregtech.common.covers.ManualImportExportMode;
import gregtech.common.pipelikeold.fluidpipe.FluidPipeType;
import gregtech.common.pipelikeold.fluidpipe.tile.TileEntityFluidPipe;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import org.jetbrains.annotations.NotNull;

public class WorldFluidPipeNet extends WorldPipeNetComplex<FluidPipeProperties, FluidPipeType, NetFlowEdge> {

    // TODO handle fluids in old fluid pipes

    private static final String DATA_ID_BASE = "gregtech.fluid_pipe_net";

    public static WorldFluidPipeNet getWorldPipeNet(World world) {
        String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldFluidPipeNet netWorldData = (WorldFluidPipeNet) world.loadData(WorldFluidPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldFluidPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldFluidPipeNet(String name) {
        super(name, true, () -> new NetFlowEdge(20), DynamicWeightsShortestPathsAlgorithm::new);
    }

    @Override
    protected boolean needsDynamicWeights() {
        return true;
    }

    @Override
    protected Capability<?>[] getConnectionCapabilities() {
        return new Capability[] { CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY };
    }

    @Override
    protected Class<? extends IPipeTile<FluidPipeType, FluidPipeProperties, NetFlowEdge>> getBasePipeClass() {
        return TileEntityFluidPipe.class;
    }

    @Override
    protected IEdgePredicateOld<?> getPredicate(Cover thisCover, Cover neighbourCover) {
        FluidEdgePredicate predicate = new FluidEdgePredicate();
        if (thisCover instanceof CoverFluidFilter filter &&
                filter.getFilterMode() != FluidFilterMode.FILTER_FILL) {
            predicate.setSourceFilter(filter.getFilterContainer());
        }
        if (neighbourCover instanceof CoverFluidFilter filter &&
                filter.getFilterMode() != FluidFilterMode.FILTER_DRAIN) {
            predicate.setTargetFilter(filter.getFilterContainer());
        }
        if (thisCover instanceof CoverPump pump) {
            if (pump.getManualImportExportMode() == ManualImportExportMode.DISABLED) {
                predicate.setShutteredSource(true);
            } else if (pump.getManualImportExportMode() == ManualImportExportMode.FILTERED) {
                predicate.setSourceFilter(pump.getFluidFilterContainer());
            }
        }
        if (neighbourCover instanceof CoverPump pump) {
            if (pump.getManualImportExportMode() == ManualImportExportMode.DISABLED) {
                predicate.setShutteredTarget(true);
            } else if (pump.getManualImportExportMode() == ManualImportExportMode.FILTERED) {
                predicate.setTargetFilter(pump.getFluidFilterContainer());
            }
        }
        // TODO should fluid regulators apply rate limits to edge predicates?
        return shutterify(predicate, thisCover, neighbourCover);
    }

    @Override
    public @NotNull NetLogicData getDefaultNodeData() {
        return new FluidPipeProperties();
    }
}
