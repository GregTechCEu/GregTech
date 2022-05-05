package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.util.FacingPos;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;
import java.util.function.Predicate;

public class ItemPipeNet extends PipeNet<ItemPipeProperties> {

    private final Map<BlockPos, List<Inventory>> NET_DATA = new HashMap<>();

    public ItemPipeNet(WorldPipeNet<ItemPipeProperties, ? extends PipeNet<ItemPipeProperties>> world) {
        super(world);
    }

    public List<Inventory> getNetData(BlockPos pipePos, EnumFacing facing) {
        List<Inventory> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = ItemNetWalker.createNetData(getWorldData(), pipePos, facing);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(inv -> inv.properties.getPriority()));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<ItemPipeProperties>> transferredNodes, PipeNet<ItemPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((ItemPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(ItemPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("Resistance", nodeData.getPriority());
        tagCompound.setFloat("Rate", nodeData.getTransferRate());
    }

    @Override
    protected ItemPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new ItemPipeProperties(tagCompound.getInteger("Range"), tagCompound.getFloat("Rate"));
    }

    public static class Inventory {
        private final BlockPos pipePos;
        private final EnumFacing faceToHandler;
        private final int distance;
        private final ItemPipeProperties properties;
        private final List<Predicate<ItemStack>> filters;

        public Inventory(BlockPos pipePos, EnumFacing facing, int distance, ItemPipeProperties properties, List<Predicate<ItemStack>> filters) {
            this.pipePos = pipePos;
            this.faceToHandler = facing;
            this.distance = distance;
            this.properties = properties;
            this.filters = filters;
        }

        public BlockPos getPipePos() {
            return pipePos;
        }

        public EnumFacing getFaceToHandler() {
            return faceToHandler;
        }

        public int getDistance() {
            return distance;
        }

        public ItemPipeProperties getProperties() {
            return properties;
        }

        public List<Predicate<ItemStack>> getFilters() {
            return filters;
        }

        public boolean matchesFilters(ItemStack stack) {
            for (Predicate<ItemStack> filter : filters) {
                if (!filter.test(stack)) {
                    return false;
                }
            }
            return true;
        }

        public BlockPos getHandlerPos() {
            return pipePos.offset(faceToHandler);
        }

        public IItemHandler getHandler(World world) {
            TileEntity tile = world.getTileEntity(getHandlerPos());
            if (tile != null)
                return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, faceToHandler.getOpposite());
            return null;
        }

        public FacingPos toFacingPos() {
            return new FacingPos(pipePos, faceToHandler);
        }
    }
}
