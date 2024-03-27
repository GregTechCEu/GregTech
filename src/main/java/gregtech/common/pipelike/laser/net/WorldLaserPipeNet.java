package gregtech.common.pipelike.laser.net;

import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.laser.LaserPipeProperties;
import gregtech.common.pipelike.laser.LaserPipeType;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class WorldLaserPipeNet extends WorldPipeNetG<LaserPipeProperties, LaserPipeType> {

    private static final String DATA_ID = "gregtech.laser_pipe_net";

    public WorldLaserPipeNet(String name) {
        super(name, false, true);
    }

    @Override
    protected Class<? extends IPipeTile<LaserPipeType, LaserPipeProperties>> getBasePipeClass() {
        return TileEntityLaserPipe.class;
    }

    @Override
    protected void writeNodeData(LaserPipeProperties nodeData, NBTTagCompound tagCompound) {}

    @Override
    protected LaserPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new LaserPipeProperties();
    }

    @NotNull
    public static WorldLaserPipeNet getWorldPipeNet(@NotNull World world) {
        WorldLaserPipeNet netWorldData = (WorldLaserPipeNet) world.loadData(WorldLaserPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldLaserPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }
}
