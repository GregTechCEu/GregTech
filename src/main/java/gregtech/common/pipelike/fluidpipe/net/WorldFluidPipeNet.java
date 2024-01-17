package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.WorldPipeFlowNetG;
import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldFluidPipeNet extends WorldPipeFlowNetG<FluidPipeProperties, FluidPipeType> {

    private static final String DATA_ID_BASE = "gregtech.fluid_pipe_net";

    public static WorldFluidPipeNet getWorldPipeNet(World world) {
        String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldFluidPipeNet netWorldData = (WorldFluidPipeNet) world.loadData(WorldFluidPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldFluidPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldFluidPipeNet(String name) {
        super(name, true);
    }

    @Override
    protected Class<? extends IPipeTile<FluidPipeType, FluidPipeProperties>> getBasePipeClass() {
        return TileEntityFluidPipe.class;
    }

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.setInteger("throughput", nodeData.getThroughput());
        tagCompound.setBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.setBoolean("acid_proof", nodeData.isAcidProof());
        tagCompound.setBoolean("cryo_proof", nodeData.isCryoProof());
        tagCompound.setBoolean("plasma_proof", nodeData.isPlasmaProof());
        tagCompound.setInteger("channels", nodeData.getTanks());
    }

    @Override
    protected FluidPipeProperties readNodeData(NBTTagCompound tagCompound) {
        int maxTemperature = tagCompound.getInteger("max_temperature");
        int throughput = tagCompound.getInteger("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int channels = tagCompound.getInteger("channels");
        return new FluidPipeProperties(maxTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof,
                channels);
    }
}
