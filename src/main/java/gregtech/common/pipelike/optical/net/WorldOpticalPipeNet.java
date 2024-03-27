package gregtech.common.pipelike.optical.net;

import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.OpticalPipeType;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class WorldOpticalPipeNet extends WorldPipeNetG<OpticalPipeProperties, OpticalPipeType> {

    private static final String DATA_ID = "gregtech.optical_pipe_net";

    public WorldOpticalPipeNet(String name) {
        super(name, false, true);
    }

    @Override
    protected Class<? extends IPipeTile<OpticalPipeType, OpticalPipeProperties>> getBasePipeClass() {
        return TileEntityOpticalPipe.class;
    }

    @Override
    protected void writeNodeData(OpticalPipeProperties nodeData, NBTTagCompound tagCompound) {}

    @Override
    protected OpticalPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new OpticalPipeProperties();
    }

    @NotNull
    public static WorldOpticalPipeNet getWorldPipeNet(@NotNull World world) {
        WorldOpticalPipeNet netWorldData = (WorldOpticalPipeNet) world.loadData(WorldOpticalPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldOpticalPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }
}
