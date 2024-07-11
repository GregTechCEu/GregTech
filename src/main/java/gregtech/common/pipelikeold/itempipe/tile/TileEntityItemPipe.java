package gregtech.common.pipelikeold.itempipe.tile;

import gregtech.api.graphnet.pipenetold.block.material.TileEntityMaterialPipeBase;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.util.FacingPos;
import gregtech.common.pipelikeold.itempipe.ItemPipeType;
import gregtech.common.pipelikeold.itempipe.net.ItemNetHandler;
import gregtech.common.pipelikeold.itempipe.net.WorldItemPipeNet;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class TileEntityItemPipe extends TileEntityMaterialPipeBase<ItemPipeType, ItemPipeProperties, NetEdge> {

    private final EnumMap<EnumFacing, ItemNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private final Object2IntMap<FacingPos> transferred = new Object2IntOpenHashMap<>();
    private ItemNetHandler defaultHandler;
    // the ItemNetHandler can only be created on the server so we have a empty placeholder for the client
    private final IItemHandler clientCapability = new ItemStackHandler(0);

    private int transferredItems = 0;
    private long timer = 0;

    public long getWorldTime() {
        return hasWorld() ? getWorld().getTotalWorldTime() : 0L;
    }

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    private void initHandlers() {
        WorldItemPipeNet net = WorldItemPipeNet.getWorldPipeNet(getPipeWorld());
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
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void resetTransferred() {
        transferred.clear();
    }

    public Object2IntMap<FacingPos> getTransferred() {
        return transferred;
    }

    @Override
    public void transferDataFrom(IPipeTile<ItemPipeType, ItemPipeProperties, NetEdge> tileEntity) {
        super.transferDataFrom(tileEntity);
        TileEntityItemPipe itemPipe = (TileEntityItemPipe) tileEntity;
        // take handlers from old pipe
        if (!itemPipe.handlers.isEmpty()) {
            this.handlers.clear();
            for (ItemNetHandler handler : itemPipe.handlers.values()) {
                handler.updatePipe(this);
                this.handlers.put(handler.getFacing(), handler);
            }
        }
        if (itemPipe.defaultHandler != null) {
            itemPipe.defaultHandler.updatePipe(this);
            this.defaultHandler = itemPipe.defaultHandler;
        }
    }

    // every time the transferred variable is accessed this method should be called
    // if 20 ticks passed since the last access it will reset it
    // this method is equal to
    // if (++time % 20 == 0) {
    // this.transferredItems = 0;
    // }
    // if it was in a ticking TileEntity
    private void updateTransferredState() {
        long currentTime = getWorldTime();
        long dif = currentTime - this.timer;
        if (dif >= 20 || dif < 0) {
            this.transferredItems = 0;
            this.timer = currentTime;
        }
    }

    public void addTransferredItems(int amount) {
        updateTransferredState();
        this.transferredItems += amount;
    }

    public int getTransferredItems() {
        updateTransferredState();
        return this.transferredItems;
    }
}
