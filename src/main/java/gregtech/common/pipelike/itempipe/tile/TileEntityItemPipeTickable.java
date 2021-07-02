package gregtech.common.pipelike.itempipe.tile;

import gregtech.api.util.GTFluidUtils;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.lang.ref.WeakReference;

public class TileEntityItemPipeTickable extends TileEntityItemPipe implements ITickable {

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public void update() {
        getCoverableImplementation().update();
        if(isActive) {
            //pushItemsFromInventory();
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    /*public void pushItemsFromInventory() {
        ItemPipeNet net = getItemPipeNet();
        net.getAllNodes()


        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
        int blockedConnections = getBlockedConnections();
        BlockItemPipe blockFluidPipe = (BlockItemPipe) getPipeBlock();
        for (EnumFacing side : EnumFacing.VALUES) {
            if ((blockedConnections & 1 << side.getIndex()) > 0) {
                continue; //do not dispatch energy to blocked sides
            }
            blockPos.setPos(getPipePos()).move(side);
            if (!getPipeWorld().isBlockLoaded(blockPos)) {
                continue; //do not allow cables to load chunks
            }
            TileEntity tileEntity = getPipeWorld().getTileEntity(blockPos);
            if (tileEntity == null) {
                continue; //do not emit into multiparts or other fluid pipes
            }
            IItemHandler sourceHandler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            IItemHandler receiverHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
            if (sourceHandler != null && receiverHandler != null) {
                GTFluidUtils.transferFluids(sourceHandler, receiverHandler, Integer.MAX_VALUE);
            }
        }
        blockPos.release();
    }*/
}
