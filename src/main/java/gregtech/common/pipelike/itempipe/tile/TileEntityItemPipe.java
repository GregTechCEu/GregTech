package gregtech.common.pipelike.itempipe.tile;

import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.util.FacingPos;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.net.ItemNetHandler;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class TileEntityItemPipe extends TileEntityMaterialPipeBase<ItemPipeType, ItemPipeProperties> {

    private final EnumMap<EnumFacing, ItemNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private final Map<FacingPos, Integer> transferred = new HashMap<>();
    private ItemNetHandler defaultHandler;
    // the ItemNetHandler can only be created on the server so we have a empty placeholder for the client
    private final IItemHandler clientCapability = new ItemStackHandler(0);
    private WeakReference<ItemPipeNet> currentPipeNet = new WeakReference<>(null);

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    private void initHandlers() {
        ItemPipeNet net = getItemPipeNet();
        if (net == null) {
            return;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            handlers.put(facing, new ItemNetHandler(net, this, facing));
        }
        defaultHandler = new ItemNetHandler(net, this, null);
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {

            if (world.isRemote)
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(clientCapability);

            if (handlers.size() == 0)
                initHandlers();
            checkNetwork();
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            ItemPipeNet current = getItemPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (ItemNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    public ItemPipeNet getItemPipeNet() {
        if (world == null || world.isRemote)
            return null;
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

    public void resetTransferred() {
        transferred.clear();
    }

    public Map<FacingPos, Integer> getTransferred() {
        return transferred;
    }

    @Override
    public void transferDataFrom(IPipeTile<ItemPipeType, ItemPipeProperties> tileEntity) {
        super.transferDataFrom(tileEntity);
        if (getItemPipeNet() == null)
            return;
        TileEntityItemPipe itemPipe = (TileEntityItemPipe) tileEntity;
        if (!itemPipe.handlers.isEmpty() && itemPipe.defaultHandler != null) {
            // take handlers from old pipe
            handlers.clear();
            for (Map.Entry<EnumFacing, ItemNetHandler> entry : itemPipe.handlers.entrySet()) {
                handlers.put(entry.getKey(), entry.getValue());
            }
            defaultHandler = itemPipe.defaultHandler;
            checkNetwork();
        } else {
            // create new handlers
            initHandlers();
        }
    }
}
