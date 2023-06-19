package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.util.FacingPos;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OpticalPipeNet extends PipeNet<OpticalPipeProperties> {

    private final Map<BlockPos, List<OpticalInventory>> NET_DATA = new Object2ObjectOpenHashMap<>();

    public OpticalPipeNet(WorldPipeNet<OpticalPipeProperties, ? extends PipeNet<OpticalPipeProperties>> world) {
        super(world);
    }

    public List<OpticalInventory> getNetData(BlockPos pipePos, EnumFacing facing) {
        List<OpticalInventory> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = OpticalNetWalker.createNetData(getWorldData(), pipePos, facing);
            if (data == null) {
                // walker failed, don't cache, so it tries again on next insertion
                return Collections.emptyList();
            }

            // only allow one other connection
            if (data.size() == 1) {
                NET_DATA.put(pipePos, data);
            } else {
                return Collections.emptyList();
            }
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
    protected void transferNodeData(Map<BlockPos, Node<OpticalPipeProperties>> transferredNodes, PipeNet<OpticalPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((OpticalPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(OpticalPipeProperties nodeData, NBTTagCompound tagCompound) {

    }

    @Override
    protected OpticalPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new OpticalPipeProperties();
    }

    public static class OpticalInventory {

        private final BlockPos pipePos;
        private final EnumFacing faceToHandler;
        private final int distance;
        private final OpticalPipeProperties properties;

        public OpticalInventory(BlockPos pipePos, EnumFacing faceToHandler, int distance, OpticalPipeProperties properties) {
            this.pipePos = pipePos;
            this.faceToHandler = faceToHandler;
            this.distance = distance;
            this.properties = properties;
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

        public OpticalPipeProperties getProperties() {
            return properties;
        }

        public BlockPos getHandlerPos() {
            return pipePos.offset(faceToHandler);
        }

        public IOpticalDataAccessHatch getHandler(@Nonnull World world) {
            TileEntity tile = world.getTileEntity(getHandlerPos());
            if (tile != null) {
                IDataAccessHatch hatch = tile.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, faceToHandler.getOpposite());
                return hatch instanceof IOpticalDataAccessHatch opticalHatch ? opticalHatch : null;
            }
            return null;
        }

        @Nonnull
        public FacingPos toFacingPos() {
            return new FacingPos(pipePos, faceToHandler);
        }
    }
}
