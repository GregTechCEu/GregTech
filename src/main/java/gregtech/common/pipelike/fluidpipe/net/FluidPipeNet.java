package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.GTLog;
import gregtech.common.pipelike.fluidpipe.tile.Wave;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> {

    private final Map<Integer, Wave> waves = new HashMap<>();
    private int currentWaveId = 0;

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    private int generateWaveId() {
        if (currentWaveId == 2000000000) {// this will go up relatively fast, I hope it wont cause issues with resetting
            currentWaveId = 0;
            GTLog.logger.info("resetted waves!");
        }
        return currentWaveId++;
    }

    public Wave createWave() {
        Wave wave = new Wave(generateWaveId());
        wave.addUser();
        GTLog.logger.info("Created wave {}", wave.getId());
        waves.put(wave.getId(), wave);
        worldData.markDirty();
        return wave;
    }

    public Wave getWave(int id) {
        return waves.get(id);
    }

    public void killWave(int id) {
        waves.remove(id);
        worldData.markDirty();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<FluidPipeProperties>> transferredNodes, PipeNet<FluidPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        /*for(BlockPos pos : transferredNodes.keySet()) {
            TileEntityFluidPipeTickable pipe = (TileEntityFluidPipeTickable) getWorldData().getTileEntity(pos);
        }*/

        FluidPipeNet fluidNet = (FluidPipeNet) parentNet;
        for (Wave wave : fluidNet.waves.values()) {
            if (waves.containsKey(wave.getId())) {
                wave.reassignId(generateWaveId());
            }
            waves.put(wave.getId(), wave);
        }
        fluidNet.waves.clear();
    }

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.setInteger("throughput", nodeData.getThroughput());
        tagCompound.setBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.setInteger("channels", nodeData.getTanks());
    }

    @Override
    protected FluidPipeProperties readNodeData(NBTTagCompound tagCompound) {
        int maxTemperature = tagCompound.getInteger("max_temperature");
        int throughput = tagCompound.getInteger("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        int channels = tagCompound.getInteger("channels");
        return new FluidPipeProperties(maxTemperature, throughput, gasProof, channels);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setInteger("LastID", currentWaveId);
        NBTTagList wavesNbt = new NBTTagList();
        for (Wave wave : waves.values()) {
            NBTTagCompound waveNbt = new NBTTagCompound();
            waveNbt.setInteger("ID", wave.getId());
            waveNbt.setInteger("UseCount", wave.getUseCount());
            wavesNbt.appendTag(waveNbt);
        }
        nbt.setTag("Waves", wavesNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        GTLog.logger.info("Read net data");
        super.deserializeNBT(nbt);
        if (nbt.hasKey("LastID") && nbt.hasKey("Waves")) { // prevents crash with existing pipes
            currentWaveId = nbt.getInteger("LastID");
            waves.clear();
            NBTTagList wavesNbt = nbt.getTagList("Waves", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < wavesNbt.tagCount(); i++) {
                NBTTagCompound waveNbt = wavesNbt.getCompoundTagAt(i);
                Wave wave = new Wave(waveNbt.getInteger("ID"), waveNbt.getInteger("UseCount"));
                waves.put(wave.getId(), wave);
            }
        }
    }
}
