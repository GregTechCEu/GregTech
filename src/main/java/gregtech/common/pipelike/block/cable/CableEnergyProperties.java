package gregtech.common.pipelike.block.cable;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.logic.TemperatureLogic;
import gregtech.api.graphnet.logic.TemperatureRestorationFunction;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.pipelike.net.energy.LossAbsoluteLogic;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PipeNetProperties;

import gregtech.common.pipelike.net.energy.SuperconductorLogic;
import gregtech.common.pipelike.net.energy.WorldEnergyNet;

import gregtech.common.pipelike.net.energy.VoltageLimitLogic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FOIL;

public class CableEnergyProperties implements PipeNetProperties.IPipeNetMaterialProperty {

    public static final int INSULATION_BURN_TEMP = 1000;

    private final long voltageLimit;
    private final long amperageLimit;
    private int temperatureLimit;
    private final long lossPerAmp;
    /**
     *
     */
    private final int superconductorTemperature;

    /**
     * Generate a CableEnergyProperties
     * @param voltageLimit the voltage limit for the cable
     * @param amperageLimit the base amperage for the cable.
     * @param temperatureLimit the melt temperature of the cable. If zero, autogeneration will be attempted.
     * @param lossPerAmp the base loss per amp per block traveled.
     * @param superconductorTemperature the superconductor temperature. When the temperature is at or below
     *                                  superconductor temperature, loss will be treated as zero. A superconductor
     *                                  temperature of 0 or less will be treated as not a superconductor.
     */
    public CableEnergyProperties(long voltageLimit, long amperageLimit, int temperatureLimit, long lossPerAmp, int superconductorTemperature) {
        this.voltageLimit = voltageLimit;
        this.amperageLimit = amperageLimit;
        this.temperatureLimit = temperatureLimit;
        this.lossPerAmp = lossPerAmp;
        this.superconductorTemperature = superconductorTemperature;
    }

    public boolean isSuperconductor() {
        return this.superconductorTemperature > 1;
    }

    @Override
    public @NotNull String getName() {
        return "cableEnergy";
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.INGOT)) {
            // Ensure all Materials with Cables and voltage tier IV or above have a Foil for recipe generation
            Material thisMaterial = properties.getMaterial();
            if (!isSuperconductor() && voltageLimit >= GTValues.V[GTValues.IV] && !thisMaterial.hasFlag(GENERATE_FOIL)) {
                thisMaterial.addFlags(GENERATE_FOIL);
            }
        }
        if (this.temperatureLimit == 0 && properties.hasProperty(PropertyKey.FLUID)) {
            // autodetermine melt temperature from registered fluid
            FluidProperty prop = properties.getProperty(PropertyKey.FLUID);
            Fluid fluid = prop.getStorage().get(FluidStorageKeys.LIQUID);
            if (fluid == null) {
                FluidBuilder builder = prop.getStorage().getQueuedBuilder(FluidStorageKeys.LIQUID);
                if (builder != null) {
                    this.temperatureLimit = builder.currentTemp();
                }
            } else {
                this.temperatureLimit = fluid.getTemperature();
            }
        }
    }

    @Override
    public void addToNet(World world, BlockPos pos, IPipeStructure structure) {
        assert structure instanceof CableStructure;
        CableStructure cable = (CableStructure) structure;
        long loss = lossPerAmp * cable.costFactor();
        long amperage = amperageLimit * cable.material();
        boolean insulated = cable.partialBurnStructure() != null;
        // insulated cables cool down half as fast
        float coolingFactor = (float) (Math.sqrt(cable.material()) / (insulated ? 8 : 4));
        WorldPipeNetNode node = WorldEnergyNet.getWorldNet(world).getOrCreateNode(pos);
        NetLogicData newData = node.getData();
        newData.setLogicEntry(LossAbsoluteLogic.INSTANCE.getWith(loss))
                .setLogicEntry(WeightFactorLogic.INSTANCE.getWith(loss + 0.001 / amperage))
                .setLogicEntry(ThroughputLogic.INSTANCE.getWith(amperage))
                .setLogicEntry(VoltageLimitLogic.INSTANCE.getWith(voltageLimit))
                .setLogicEntry(TemperatureLogic.INSTANCE
                        .getWith(TemperatureRestorationFunction.GEOMETRIC_ARITHMETIC, coolingFactor, temperatureLimit,
                                insulated ? INSULATION_BURN_TEMP : null, 1, 100 * cable.material()));
        if (superconductorTemperature > 0) {
            newData.setLogicEntry(SuperconductorLogic.INSTANCE.getWith(superconductorTemperature));
        }
    }

    @Override
    public WorldPipeNetNode getFromNet(World world, BlockPos pos, IPipeStructure structure) {
        return WorldEnergyNet.getWorldNet(world).getNode(pos);
    }

    @Override
    public void removeFromNet(World world, BlockPos pos, IPipeStructure structure) {
        WorldEnergyNet net = WorldEnergyNet.getWorldNet(world);
        NetNode node = net.getNode(pos);
        if (node != null) net.removeNode(node);
    }

    @Override
    public boolean supportedStructure(IPipeStructure structure) {
        return structure.getClass() == CableStructure.class;
    }
}
