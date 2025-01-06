package gregtech.common.pipelike.handlers.properties;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
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
import gregtech.common.pipelike.block.pipe.MaterialPipeStructure;
import gregtech.common.pipelike.net.energy.AmperageLimitLogic;
import gregtech.common.pipelike.net.energy.SuperconductorLogic;
import gregtech.common.pipelike.net.energy.VoltageLimitLogic;
import gregtech.common.pipelike.net.energy.VoltageLossLogic;
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

    public static final MaterialPropertyKey<MaterialEnergyProperties> KEY = new MaterialPropertyKey<>(
            "EnergyProperties");

    private static final int MINIMUM_MELT_TEMPERATURE = 1500;

    private final long voltageLimit;
    private final long amperageLimit;
    private int materialMeltTemperature;
    private final long lossPerAmp;
    private final boolean superconductor;

    /**
     * Generate a MaterialEnergyProperties
     * 
     * @param voltageLimit   the voltage limit for the cable
     * @param amperageLimit  the base amperage for the cable.
     * @param lossPerAmp     the base loss per amp per block traveled.
     * @param superconductor whether the material will be treated as a superconductor. Does not override loss.
     */
    public MaterialEnergyProperties(long voltageLimit, long amperageLimit, long lossPerAmp,
                                    boolean superconductor) {
        this.voltageLimit = voltageLimit;
        this.amperageLimit = amperageLimit;
        this.lossPerAmp = lossPerAmp;
        this.superconductor = superconductor;
    }

    public long getVoltageLimit() {
        return voltageLimit;
    }

    public static MaterialEnergyProperties create(long voltageLimit, long amperageLimit, long lossPerAmp,
                                                  boolean superconductor) {
        return new MaterialEnergyProperties(voltageLimit, amperageLimit, lossPerAmp, superconductor);
    }

    public static MaterialEnergyProperties create(long voltageLimit, long amperageLimit, long lossPerAmp) {
        return new MaterialEnergyProperties(voltageLimit, amperageLimit, lossPerAmp, false);
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
        return superconductor;
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

        long loss = getLoss(structure);
        tooltip.add(I18n.format("gregtech.cable.loss_per_block", loss));
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
        this.materialMeltTemperature = computeMaterialMeltTemperature(properties);
    }

    private static int computeMaterialMeltTemperature(@NotNull MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.FLUID)) {
            // autodetermine melt temperature from registered fluid
            FluidProperty prop = properties.getProperty(PropertyKey.FLUID);
            Fluid fluid = prop.get(FluidStorageKeys.LIQUID);
            if (fluid == null) {
                FluidBuilder builder = prop.getQueuedBuilder(FluidStorageKeys.LIQUID);
                if (builder != null) {
                    return Math.max(MINIMUM_MELT_TEMPERATURE,
                            builder.getDeterminedTemperature(properties.getMaterial(), FluidStorageKeys.LIQUID));
                }
            } else {
                return Math.max(MINIMUM_MELT_TEMPERATURE, fluid.getTemperature());
            }
        }
        return MINIMUM_MELT_TEMPERATURE;
    }

    @Override
    @Nullable
    public WorldPipeNode getOrCreateFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure) {
            WorldPipeNode node = WorldEnergyNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
            return node;
        } else if (structure instanceof MaterialPipeStructure pipe) {
            long amperage = amperageLimit * pipe.material() / 2;
            if (amperage == 0) return null; // skip pipes that are too small
            WorldPipeNode node = WorldEnergyNet.getWorldNet(world).getOrCreateNode(pos);
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
            TemperatureLogic existing = data.getLogicEntryNullable(TemperatureLogic.TYPE);
            float energy = existing == null ? 0 : existing.getThermalEnergy();
            data.setLogicEntry(VoltageLossLogic.TYPE.getWith(loss))
                    .setLogicEntry(WeightFactorLogic.TYPE.getWith(loss + 0.001 / amperage))
                    .setLogicEntry(AmperageLimitLogic.TYPE.getWith(amperage))
                    .setLogicEntry(VoltageLimitLogic.TYPE.getWith(voltageLimit))
                    .setLogicEntry(TemperatureLogic.TYPE
                            .getWith(TemperatureLossFunction.getOrCreateCable(coolingFactor), materialMeltTemperature,
                                    1,
                                    100 * cable.material(), cable.partialBurnThreshold())
                            .setInitialThermalEnergy(energy));
            if (superconductor) {
                data.setLogicEntry(SuperconductorLogic.TYPE.getNew());
            }
        } else if (structure instanceof MaterialPipeStructure pipe) {
            long amperage = getAmperage(structure);
            if (amperage == 0) return; // skip pipes that are too small
            long loss = getLoss(structure);
            float coolingFactor = (float) Math.sqrt((double) pipe.material() / (4 + pipe.channelCount()));
            TemperatureLogic existing = data.getLogicEntryNullable(TemperatureLogic.TYPE);
            float energy = existing == null ? 0 : existing.getThermalEnergy();
            data.setLogicEntry(VoltageLossLogic.TYPE.getWith(loss))
                    .setLogicEntry(WeightFactorLogic.TYPE.getWith(loss + 0.001 / amperage))
                    .setLogicEntry(AmperageLimitLogic.TYPE.getWith(amperage))
                    .setLogicEntry(VoltageLimitLogic.TYPE.getWith(voltageLimit))
                    .setLogicEntry(TemperatureLogic.TYPE
                            .getWith(TemperatureLossFunction.getOrCreatePipe(coolingFactor), materialMeltTemperature, 1,
                                    50 * pipe.material(), null)
                            .setInitialThermalEnergy(energy));
            if (superconductor) {
                data.setLogicEntry(SuperconductorLogic.TYPE.getNew());
            }
        }
    }

    private long getLoss(IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            return lossPerAmp * cable.costFactor();
        } else if (structure instanceof MaterialPipeStructure pipe) {
            return lossPerAmp * (pipe.material() > 6 ? 3 : 2);
        } else return lossPerAmp;
    }

    private long getAmperage(IPipeStructure structure) {
        if (structure instanceof CableStructure cable) {
            return amperageLimit * cable.material();
        } else if (structure instanceof MaterialPipeStructure pipe) {
            return amperageLimit * pipe.material() / 2;
        } else return amperageLimit;
    }

    @Override
    @Nullable
    public WorldPipeNode getFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure || structure instanceof MaterialPipeStructure)
            return WorldEnergyNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof CableStructure || structure instanceof MaterialPipeStructure) {
            WorldEnergyNet net = WorldEnergyNet.getWorldNet(world);
            NetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public boolean generatesStructure(IPipeStructure structure) {
        return structure instanceof CableStructure cable && (!isSuperconductor() || !cable.isInsulated());
    }

    @Override
    public boolean supportsStructure(IPipeStructure structure) {
        return structure instanceof CableStructure /* || structure instanceof MaterialPipeStructure */;
    }
}
