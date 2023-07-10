package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.laser.LaserPipeProperties;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class LaserPipeNet extends PipeNet<LaserPipeProperties> {

    private final Map<BlockPos, LaserData> netData = new Object2ObjectOpenHashMap<>();

    public LaserPipeNet(WorldPipeNet<LaserPipeProperties, ? extends PipeNet<LaserPipeProperties>> world) {
        super(world);
    }

    @Nullable
    public LaserData getNetData(BlockPos pipePos, EnumFacing facing) {
        LaserData data = netData.get(pipePos);
        if (data == null) {
            data = LaserNetWalker.createNetData(getWorldData(), pipePos, facing);
            if (data == null) {
                // walker failed, don't cache, so it tries again on next insertion
                return null;
            }

            netData.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        netData.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        netData.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<LaserPipeProperties>> transferredNodes, PipeNet<LaserPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        netData.clear();
        ((LaserPipeNet) parentNet).netData.clear();
    }

    @Override
    protected void writeNodeData(LaserPipeProperties nodeData, NBTTagCompound tagCompound) {

    }

    @Override
    protected LaserPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new LaserPipeProperties();
    }

    // jabel moment
    public static class LaserData {

        private final BlockPos pipePos;
        private final EnumFacing faceToHandler;
        private final int distance;
        private final LaserPipeProperties properties;

        public LaserData(BlockPos pipePos, EnumFacing faceToHandler, int distance, LaserPipeProperties properties) {
            this.pipePos = pipePos;
            this.faceToHandler = faceToHandler;
            this.distance = distance;
            this.properties = properties;
        }

        /**
         * Gets the current position of the pipe
         * @return The position of the pipe
         */
        @Nonnull
        public BlockPos getPipePos() {
            return pipePos;
        }

        /**
         * Gets the current face to handler
         * @return The face to handler
         */
        @Nonnull
        public EnumFacing getFaceToHandler() {
            return faceToHandler;
        }


        /**
         * Gets the manhattan distance traveled during walking
         * @return The distance in blocks
         */
        public int getDistance() {
            return distance;
        }

        /**
         * Gets the laser pipe properties of the current pipe
         * @return The properties of the pipe.
         */
        @Nonnull
        public LaserPipeProperties getProperties() {
            return properties;
        }

        /**
         * @return The position of where the handler would be
         */
        @Nonnull
        public BlockPos getHandlerPos() {
            return pipePos.offset(faceToHandler);
        }

        /**
         * Gets the handler if it exists
         * @param world the world to get the handler from
         * @return the handler
         */
        @Nullable
        public ILaserContainer getHandler(@Nonnull World world) {
            TileEntity tile = world.getTileEntity(getHandlerPos());
            if (tile != null) {
                return tile.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, faceToHandler.getOpposite());
            }
            return null;
        }
    }
}
