package gregtech.common.pipelike.itempipe.net;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;

public class ItemTransferNetwork {

    private final Map<SidedBlockPos, IItemHandler> ITEMHANDLERS = new HashMap<>();

    private final World world;

    public ItemTransferNetwork(World world) {
        this.world = world;
    }

    public ItemStack requestItemTransfer(ItemStack stack, BlockPos sourcePos, boolean simulate) {
        double maxDist = 0;
        Map.Entry<SidedBlockPos, IItemHandler> handler = null;
        for(Map.Entry<SidedBlockPos, IItemHandler> entry : ITEMHANDLERS.entrySet()) {
            if(handler == null) {
                handler = entry;
                maxDist = entry.getKey().blockPos.distanceSq(sourcePos);
                continue;
            }
            double dist = entry.getKey().blockPos.distanceSq(sourcePos);
            if(dist > maxDist) {
                handler = entry;
                maxDist = dist;
            }
        }
        if(handler != null) {
            //IItemHandler inv = handler.getValue();
            TileEntity tile = world.getTileEntity(handler.getKey().getBlockPos().offset(handler.getKey().getAccessSide()));
            if(tile != null) {
                IItemHandler inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if(inv != null) {
                    return ItemHandlerHelper.insertItemStacked(inv, stack, simulate);
                }
            }

        }
        return stack;
    }

    public void transferItemHandlers(Collection<BlockPos> nodePositions, ItemTransferNetwork destNetwork) {
        List<Map.Entry<SidedBlockPos, IItemHandler>> moving = new ArrayList<>();
        for(Map.Entry<SidedBlockPos, IItemHandler> entry : ITEMHANDLERS.entrySet()) {
            if(nodePositions.contains(entry.getKey().blockPos)) {
                moving.add(entry);
            }
        }
        moving.forEach(entry -> {
            removeItemHandler(entry.getKey());
            destNetwork.addItemHandler(entry.getKey(), entry.getValue());
        });
    }

    public void handleBlockedConnectionChange(BlockPos nodePos, EnumFacing side, boolean isBlockedNow) {
        if (isBlockedNow) {
            SidedBlockPos blockPos = new SidedBlockPos(nodePos, side);
            removeItemHandler(blockPos);
        } else {
            //just add unchecked item handler, addItemHandler will refuse
            //to add item handler if it's updateCache will return UpdateResult.INVALID
            //avoids duplicating logic here
            tryAddItemHandler(nodePos, side);
        }
    }

    public void checkForItemHandlers(BlockPos nodePos, int blockedConnections) {
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            //skip sides reported as blocked by pipe network
            if ((blockedConnections & 1 << accessSide.getIndex()) > 0) continue;
            //check for existing item handler
            SidedBlockPos blockPos = new SidedBlockPos(nodePos, accessSide);
            if (!ITEMHANDLERS.containsKey(blockPos)) {
                //just add unchecked item handler, addItemHandler will refuse
                //to add item handler if it's updateCache will return UpdateResult.INVALID
                //avoids duplicating logic here
                tryAddItemHandler(nodePos, accessSide);
            }
        }
    }

    public void removeItemHandler(SidedBlockPos sidedBlockPos) {
        ITEMHANDLERS.remove(sidedBlockPos);
    }

    public void tryAddItemHandler(BlockPos pos, EnumFacing facing) {
        TileEntity tile = world.getTileEntity(pos);
        IItemHandler iItemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
        if(iItemHandler != null) {
            addItemHandler(new SidedBlockPos(pos, facing), iItemHandler);
        }
    }

    public void addItemHandler(SidedBlockPos sidedBlockPos, IItemHandler itemHandler) {
        ITEMHANDLERS.put(sidedBlockPos, itemHandler);
    }

    private static class SidedBlockPos {
        private final BlockPos blockPos;
        private final EnumFacing accessSide;

        public SidedBlockPos(BlockPos blockPos, EnumFacing accessSide) {
            this.blockPos = blockPos;
            this.accessSide = accessSide;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public EnumFacing getAccessSide() {
            return accessSide;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SidedBlockPos)) return false;
            SidedBlockPos that = (SidedBlockPos) o;
            return Objects.equals(blockPos, that.blockPos) &&
                    accessSide == that.accessSide;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockPos, accessSide);
        }
    }
}
