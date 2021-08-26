package gregtech.common.pipelike.itempipe.tile;

import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.util.GTLog;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.net.ItemNetHandler;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class TileEntityItemPipe extends TileEntityMaterialPipeBase<ItemPipeType, ItemPipeProperties> {

    private WeakReference<ItemPipeNet> currentPipeNet = new WeakReference<>(null);

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            ItemPipeNet net = (ItemPipeNet) getPipeNet();
            if (net == null)
                GTLog.logger.error("PipeNet can't be null");
            else
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemNetHandler(net, this, facing));
        }
        return super.getCapabilityInternal(capability, facing);
    }
}
