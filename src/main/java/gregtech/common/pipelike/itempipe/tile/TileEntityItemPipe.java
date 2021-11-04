package gregtech.common.pipelike.itempipe.tile;

import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.net.ItemNetHandler;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class TileEntityItemPipe extends TileEntityMaterialPipeBase<ItemPipeType, ItemPipeProperties> {

    private WeakReference<ItemPipeNet> currentPipeNet = new WeakReference<>(null);

    private final Map<String, String> transferred = new HashMap<>();

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
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemNetHandler(getItemPipeNet(), this, facing));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public ItemPipeNet getItemPipeNet() {
        ItemPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() &&
                currentPipeNet.containsNode(getPipePos()))
            return currentPipeNet; //if current net is valid and does contain position, return it
        WorldItemPipeNet worldFluidPipeNet = (WorldItemPipeNet) getPipeBlock().getWorldPipeNet(getPipeWorld());
        currentPipeNet = worldFluidPipeNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    public void transferTo(BlockPos pos, EnumFacing facing) {
        ItemNetHandler.transferTo(transferred, pos, facing);
    }

    public boolean didTransferTo(BlockPos pos, EnumFacing facing) {
        return ItemNetHandler.didTransferTo(transferred, pos, facing);
    }

    public void resetTransferred() {
        transferred.clear();
    }
}
