package gregtech.common.pipelike.cable.net;

import gregtech.api.pipenet.AbstractGroupData;
import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldEnergyNet extends WorldPipeNetG<WireProperties, Insulation> {

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
        super(name);
    }

    @Override
    protected Class<? extends IPipeTile<Insulation, WireProperties>> getBasePipeClass() {
        return TileEntityCable.class;
    }

    @Override
    protected AbstractGroupData<Insulation, WireProperties> getBlankGroupData() {
        return new EnergyGroupData();
    }

    @Override
    protected void writeNodeData(WireProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("voltage", nodeData.getVoltage());
        tagCompound.setInteger("amperage", nodeData.getAmperage());
        tagCompound.setInteger("loss", nodeData.getLossPerBlock());
    }

    @Override
    protected WireProperties readNodeData(NBTTagCompound tagCompound) {
        int voltage = tagCompound.getInteger("voltage");
        int amperage = tagCompound.getInteger("amperage");
        int lossPerBlock = tagCompound.getInteger("loss");
        return new WireProperties(voltage, amperage, lossPerBlock);
    }
}
