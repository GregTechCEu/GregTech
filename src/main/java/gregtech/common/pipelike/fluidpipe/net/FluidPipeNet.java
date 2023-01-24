package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.fluids.info.FluidTag;
import gregtech.api.fluids.info.FluidTags;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.Set;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> {

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.setInteger("throughput", nodeData.getThroughput());
        tagCompound.setBoolean("liquid_proof", nodeData.isLiquidProof());
        tagCompound.setBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.setBoolean("acid_proof", nodeData.isAcidProof());
        tagCompound.setBoolean("superacid_proof", nodeData.isSuperAcidProof());
        tagCompound.setBoolean("cryo_proof", nodeData.isCryoProof());
        tagCompound.setBoolean("plasma_proof", nodeData.isPlasmaProof());
        tagCompound.setInteger("channels", nodeData.getTanks());
        Collection<FluidTag> allowedTags = nodeData.getAllowedTags();
        if (allowedTags != null) {
            NBTTagCompound data = new NBTTagCompound();
            for (FluidTag fluidTag : allowedTags) {
                if (fluidTag.requiresChecking()) {
                    data.setBoolean(fluidTag.getName(), true);
                }
            }
            tagCompound.setTag("allowed_tags", data);
        }
    }

    @Override
    protected FluidPipeProperties readNodeData(NBTTagCompound tagCompound) {
        int maxTemperature = tagCompound.getInteger("max_temperature");
        int throughput = tagCompound.getInteger("throughput");
        boolean liquidProof = !tagCompound.hasKey("liquid_proof") || tagCompound.getBoolean("liquid_proof");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean superacidProof = !tagCompound.hasKey("superacid_proof") || tagCompound.getBoolean("superacid_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int channels = tagCompound.getInteger("channels");
        Set<FluidTag> tags;
        if (tagCompound.hasKey("allowed_tags")) {
            tags = new ObjectOpenHashSet<>();
            NBTTagCompound allowedTags = tagCompound.getCompoundTag("allowed_tags");
            for (String key : allowedTags.getKeySet()) {
                if (allowedTags.getBoolean(key)) {
                    tags.add(FluidTags.getDataByName(key));
                }
            }
        } else tags = null;

        return new FluidPipeProperties(channels, throughput, liquidProof, gasProof,
                plasmaProof,cryoProof, acidProof, superacidProof,
                maxTemperature, tags);
    }
}
