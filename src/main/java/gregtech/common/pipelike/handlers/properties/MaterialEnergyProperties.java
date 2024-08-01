package gregtech.common.pipelike.handlers.properties;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.logic.TemperatureLossFunction;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PipeNetProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.IOreRegistrationHandler;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.TriConsumer;
import gregtech.common.pipelike.block.cable.CableStructure;
import gregtech.common.pipelike.block.pipe.PipeStructure;
import gregtech.common.pipelike.net.energy.VoltageLossLogic;
import gregtech.common.pipelike.net.energy.SuperconductorLogic;
import gregtech.common.pipelike.net.energy.VoltageLimitLogic;
import gregtech.common.pipelike.net.energy.WorldEnergyNet;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FOIL;
import static gregtech.api.unification.material.info.MaterialFlags.NO_UNIFICATION;

public final class MaterialEnergyProperties implements PipeNetProperties.IPipeNetMaterialProperty {

    public static final MaterialPropertyKey<MaterialEnergyProperties> KEY = new MaterialPropertyKey<>();

    private final long voltageLimit;
    private final long amperageLimit;
    private int temperatureLimit;
    private final long lossPerAmp;
    private final int superconductorCriticalTemperature;

    /**
     * Generate a MaterialEnergyProperties
     * 
     * @param voltageLimit                      the voltage limit for the cable
     * @param amperageLimit                     the base amperage for the cable.
     * @param lossPerAmp                        the base loss per amp per block traveled.
     * @param temperatureLimit                  the melt temperature of the cable. If zero, autogeneration will be
     *                                          attempted.
     * @param superconductorCriticalTemperature the superconductor temperature. When the temperature is at or below
     *                                          superconductor temperature, loss will be treated as zero. A
     *                                          superconductor
     *                                          temperature of 0 or less will be treated as not a superconductor.
     */
    public MaterialEnergyProperties(long voltageLimit, long amperageLimit, long lossPerAmp, int temperatureLimit,
                                    int superconductorCriticalTemperature) {
        this.voltageLimit = voltageLimit;
        this.amperageLimit = amperageLimit;
        this.temperatureLimit = temperatureLimit;
        this.lossPerAmp = lossPerAmp;
        this.superconductorCriticalTemperature = superconductorCriticalTemperature;
    }

    public long getVoltageLimit() {
        return voltageLimit;
    }

    public static MaterialEnergyProperties createT(long voltageLimit, long amperageLimit, long lossPerAmp,
                                                   int temperatureLimit) {
        return new MaterialEnergyProperties(voltageLimit, amperageLimit, lossPerAmp, temperatureLimit, 0);
    }

    public static MaterialEnergyProperties createS(long voltageLimit, long amperageLimit, long lossPerAmp,
                                                   int superconductorCriticalTemperature) {
        return new MaterialEnergyProperties(voltageLimit, amperageLimit, lossPerAmp, 0,
                superconductorCriticalTemperature);
    }

    public static MaterialEnergyProperties create(long voltageLimit, long amperageLimit, long lossPerAmp) {
        return new MaterialEnergyProperties(voltageLimit, amperageLimit, lossPerAmp, 0, 0);
    }

    public static IOreRegistrationHandler registrationHandler(TriConsumer<OrePrefix, Material, MaterialEnergyProperties> handler) {
        return (orePrefix, material) -> {
            if (material.hasProperty(PropertyKey.PIPENET_PROPERTIES) && !material.hasFlag(NO_UNIFICATION) &&
                    material.getProperty(PropertyKey.PIPENET_PROPERTIES).hasProperty(KEY)) {
                handler.accept(orePrefix, material,
                        material.getProperty(PropertyKey.PIPENET_PROPERTIES).getProperty(KEY));
            }
        };
    }

    public boolean isSuperconductor() {
        return this.superconductorCriticalTemperature > 1;
    }

    @Override
    public MaterialPropertyKey<?> getKey() {
        return KEY;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeMaterialStructure structure) {

        int tier = GTUtility.getTierByVoltage(voltageLimit);
        if (isSuperconductor())
            tooltip.add(I18n.format("gregtech.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(I18n.format("gregtech.cable.voltage", voltageLimit, GTValues.VNF[tier]));
        tooltip.add(I18n.format("gregtech.cable.amperage", getAmperage(structure)));
        tooltip.add(I18n.format("gregtech.cable.loss_per_block", getLoss(structure)));
        if (isSuperconductor())
            tooltip.add(I18n.format("gregtech.cable.superconductor_loss", superconductorCriticalTemperature));
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.INGOT)) {
            // Ensure all Materials with Cables and voltage tier IV or above have a Foil for recipe generation
            Material thisMaterial = properties.getMaterial();
            if (!isSuperconductor() && voltageLimit >= GTValues.V[GTValues.IV] &&
                    !thisMaterial.hasFlag(GENERATE_FOIL)) {
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
    @Nullable
    public WorldPipeNetNode getOrCreateFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure) {
            WorldPipeNetNode node = WorldEnergyNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
            return node;
        } else if (structure instanceof PipeStructure pipe) {
            long amperage = amperageLimit * pipe.material() / 2;
            if (amperage == 0) return null; // skip pipes that are too small
            WorldPipeNetNode node = WorldEnergyNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), pipe);
            return node;
        }
        return null;
    }

    @Override
    public void mutateData(NetLogicData data, IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            long loss = getLoss(structure);
            long amperage = getAmperage(structure);
            boolean insulated = cable.partialBurnStructure() != null;
            // insulated cables cool down half as fast
            float coolingFactor = (float) (Math.sqrt(cable.material()) / (insulated ? 8 : 4));
            data.setLogicEntry(VoltageLossLogic.INSTANCE.getWith(loss))
                    .setLogicEntry(WeightFactorLogic.INSTANCE.getWith(loss + 0.001 / amperage))
                    .setLogicEntry(ThroughputLogic.INSTANCE.getWith(amperage))
                    .setLogicEntry(VoltageLimitLogic.INSTANCE.getWith(voltageLimit))
                    .setLogicEntry(TemperatureLogic.INSTANCE
                            .getWith(TemperatureLossFunction.getOrCreateCable(coolingFactor), temperatureLimit, 1,
                                    100 * cable.material(), cable.partialBurnThreshold()));
            if (superconductorCriticalTemperature > 0) {
                data.setLogicEntry(SuperconductorLogic.INSTANCE.getWith(superconductorCriticalTemperature));
            }
        } else if (structure instanceof PipeStructure pipe) {
            long amperage = getAmperage(structure);
            if (amperage == 0) return; // skip pipes that are too small
            long loss = getLoss(structure);
            float coolingFactor = (float) Math.sqrt((double) pipe.material() / (4 + pipe.channelCount()));
            data.setLogicEntry(VoltageLossLogic.INSTANCE.getWith(loss))
                    .setLogicEntry(WeightFactorLogic.INSTANCE.getWith(loss + 0.001 / amperage))
                    .setLogicEntry(ThroughputLogic.INSTANCE.getWith(amperage))
                    .setLogicEntry(VoltageLimitLogic.INSTANCE.getWith(voltageLimit))
                    .setLogicEntry(TemperatureLogic.INSTANCE
                            .getWith(TemperatureLossFunction.getOrCreatePipe(coolingFactor), temperatureLimit, 1,
                                    50 * pipe.material(), null));
            if (superconductorCriticalTemperature > 0) {
                data.setLogicEntry(SuperconductorLogic.INSTANCE.getWith(superconductorCriticalTemperature));
            }
        }
    }

    private long getLoss(IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            return lossPerAmp * cable.costFactor();
        } else if (structure instanceof PipeStructure pipe) {
            return lossPerAmp * (pipe.material() > 6 ? 3 : 2);
        } else return lossPerAmp;
    }

    private long getAmperage(IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            return amperageLimit * cable.material();
        } else if (structure instanceof PipeStructure pipe) {
            return amperageLimit * pipe.material() / 2;
        } else return amperageLimit;
    }

    @Override
    @Nullable
    public WorldPipeNetNode getFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure || structure instanceof PipeStructure)
            return WorldEnergyNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure || structure instanceof PipeStructure) {
            WorldEnergyNet net = WorldEnergyNet.getWorldNet(world);
            NetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public boolean generatesStructure(IPipeStructure structure) {
        return structure.getClass() == CableStructure.class;
    }

    @Override
    public boolean supportsStructure(IPipeStructure structure) {
        return structure instanceof CableStructure || structure instanceof PipeStructure;
    }
}
