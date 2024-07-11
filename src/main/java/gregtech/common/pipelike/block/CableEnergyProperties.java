package gregtech.common.pipelike.block;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.logic.LossAbsoluteLogic;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.block.CableStructure;
import gregtech.api.graphnet.pipenet.block.IPipeStructure;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PipeNetProperties;

import gregtech.common.pipelike.net.EnergyNet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class CableEnergyProperties implements PipeNetProperties.IPipeNetMaterialProperty {

    private final long voltageLimit;
    private final long amperageLimit;
    private final int temperatureLimit;
    private final int lossPerAmp;

    public CableEnergyProperties(long voltageLimit, long amperageLimit, int temperatureLimit, int lossPerAmp) {
        this.voltageLimit = voltageLimit;
        this.amperageLimit = amperageLimit;
        this.temperatureLimit = temperatureLimit;
        this.lossPerAmp = lossPerAmp;
    }

    @Override
    public @NotNull String getName() {
        return "cableEnergy";
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}

    @Override
    public void addToNet(World world, BlockPos pos, IPipeStructure structure) {
        assert structure instanceof CableStructure;
        NetLogicData newData = EnergyNet.getWorldNet(world).getOrCreateNode(pos).getData();
        newData.setLogicEntry(new LossAbsoluteLogic().setValue(lossPerAmp));
    }

    @Override
    public void removeFromNet(World world, BlockPos pos) {
        EnergyNet net = EnergyNet.getWorldNet(world);
        NetNode node = net.getNode(pos);
        if (node != null) net.removeNode(node);
    }

    @Override
    public boolean supportedStructure(IPipeStructure structure) {
        return structure.getClass() == CableStructure.class;
    }
}
