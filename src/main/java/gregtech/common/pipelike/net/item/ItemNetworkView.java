package gregtech.common.pipelike.net.item;

import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.graphnet.net.NetNode;

import net.minecraftforge.items.IItemHandler;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

@Desugar
public record ItemNetworkView(ItemHandlerList handler, BiMap<IItemHandler, NetNode> handlerNetNodeBiMap) {

    public static final ItemNetworkView EMPTY = ItemNetworkView.of(ImmutableBiMap.of());

    public static ItemNetworkView of(BiMap<IItemHandler, NetNode> handlerNetNodeBiMap) {
        return new ItemNetworkView(new ItemHandlerList(handlerNetNodeBiMap.keySet()), handlerNetNodeBiMap);
    }
}
