package gregtech.common.pipelike.net.fluid;

import gregtech.api.capability.impl.FluidHandlerList;
import gregtech.api.graphnet.net.NetNode;

import net.minecraftforge.fluids.capability.IFluidHandler;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

@Desugar
public record FluidNetworkView(FluidHandlerList handler, BiMap<IFluidHandler, NetNode> handlerNetNodeBiMap) {

    public static final FluidNetworkView EMPTY = FluidNetworkView.of(ImmutableBiMap.of());

    public static FluidNetworkView of(BiMap<IFluidHandler, NetNode> handlerNetNodeBiMap) {
        return new FluidNetworkView(new FluidHandlerList(handlerNetNodeBiMap.keySet()), handlerNetNodeBiMap);
    }
}
