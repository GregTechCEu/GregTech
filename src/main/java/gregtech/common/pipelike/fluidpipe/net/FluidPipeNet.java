package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import net.minecraft.nbt.NBTTagCompound;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> {

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
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
        return new FluidPipeProperties(maxTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, channels);
    }
}
