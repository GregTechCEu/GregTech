package gregtech.common.pipelikeold.cable.net;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.graphnet.AbstractGroupData;
import gregtech.api.graphnet.pipenetold.WorldPipeNetComplex;
import gregtech.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.common.pipelikeold.cable.Insulation;
import gregtech.common.pipelikeold.cable.tile.TileEntityCable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class WorldEnergyNet extends WorldPipeNetComplex<WireProperties, Insulation, NetFlowEdge> {

    private static final String DATA_ID_BASE = "gregtech.e_net";

    public static WorldEnergyNet getWorldEnergyNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldEnergyNet eNetWorldData = (WorldEnergyNet) world.loadData(WorldEnergyNet.class, DATA_ID);
        if (eNetWorldData == null) {
            eNetWorldData = new WorldEnergyNet(DATA_ID);
            world.setData(DATA_ID, eNetWorldData);
        }
        eNetWorldData.setWorldAndInit(world);
        return eNetWorldData;
    }

    public WorldEnergyNet(String name) {
        super(name, true, () -> new NetFlowEdge(1), DynamicWeightsShortestPathsAlgorithm::new);
    }

    @Override
    protected boolean needsDynamicWeights() {
        return true;
    }

    @Override
    protected Capability<?>[] getConnectionCapabilities() {
        return new Capability[] { GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER };
    }

    @Override
    protected Class<? extends IPipeTile<Insulation, WireProperties, NetFlowEdge>> getBasePipeClass() {
        return TileEntityCable.class;
    }

    @Override
    public AbstractGroupData getBlankGroupData() {
        return new EnergyGroupData();
    }

    @Override
    public void writeNodeData(WireProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("voltage", nodeData.getVoltage());
        tagCompound.setInteger("amperage", nodeData.getAmperage());
        tagCompound.setInteger("loss", nodeData.getLoss());
        tagCompound.setInteger("meltTemperature", nodeData.getMeltTemperature());
        tagCompound.setInteger("critical", nodeData.getSuperconductorCriticalTemperature());
        tagCompound.setBoolean("supercond", nodeData.isSuperconductor());
    }

    @Override
    protected WireProperties readNodeData(NBTTagCompound tagCompound) {
        int voltage = tagCompound.getInteger("voltage");
        int amperage = tagCompound.getInteger("amperage");
        int lossPerBlock = tagCompound.getInteger("loss");
        int meltTemperature = tagCompound.getInteger("meltTemperature");
        int critical = tagCompound.getInteger("critical");
        boolean supercond = tagCompound.getBoolean("supercond");
        return new WireProperties(voltage, amperage, lossPerBlock, meltTemperature, supercond, critical);
    }
}
