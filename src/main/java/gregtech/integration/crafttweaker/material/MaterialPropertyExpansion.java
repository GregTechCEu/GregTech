package gregtech.integration.crafttweaker.material;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.*;
import gregtech.common.pipelike.handlers.properties.MaterialEnergyProperties;
import gregtech.common.pipelike.handlers.properties.MaterialFluidProperties;
import gregtech.common.pipelike.handlers.properties.MaterialItemProperties;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.integration.crafttweaker.material.CTMaterialHelpers.checkFrozen;
import static gregtech.integration.crafttweaker.material.CTMaterialHelpers.validateFluidState;

@ZenExpansion("mods.gregtech.material.Material")
@ZenRegister
@SuppressWarnings("unused")
public class MaterialPropertyExpansion {

    /////////////////////////////////////
    // Property Checkers //
    /////////////////////////////////////

    @ZenMethod
    public static boolean hasBlastTemp(Material m) {
        return m.hasProperty(PropertyKey.BLAST);
    }

    @ZenMethod
    public static boolean hasDust(Material m) {
        return m.hasProperty(PropertyKey.DUST);
    }

    @ZenMethod
    public static boolean hasFluid(Material m) {
        return m.hasProperty(PropertyKey.FLUID);
    }

    @ZenMethod
    public static boolean hasGem(Material m) {
        return m.hasProperty(PropertyKey.GEM);
    }

    @ZenMethod
    public static boolean hasIngot(Material m) {
        return m.hasProperty(PropertyKey.INGOT);
    }

    @ZenMethod
    public static boolean hasOre(Material m) {
        return m.hasProperty(PropertyKey.ORE);
    }

    @ZenMethod
    public static boolean hasTools(Material m) {
        return m.hasProperty(PropertyKey.TOOL);
    }

    @ZenMethod
    public static boolean hasFluidPipes(Material m) {
        PipeNetProperties properties = m.getProperty(PropertyKey.PIPENET_PROPERTIES);
        return properties != null && properties.hasProperty(MaterialFluidProperties.KEY);
    }

    @ZenMethod
    public static boolean hasItemPipes(Material m) {
        PipeNetProperties properties = m.getProperty(PropertyKey.PIPENET_PROPERTIES);
        return properties != null && properties.hasProperty(MaterialItemProperties.KEY);
    }

    @ZenMethod
    public static boolean hasWires(Material m) {
        PipeNetProperties properties = m.getProperty(PropertyKey.PIPENET_PROPERTIES);
        return properties != null && properties.hasProperty(MaterialEnergyProperties.KEY);
    }

    ////////////////////////////////////
    // Property Setters //
    ////////////////////////////////////

    @ZenMethod
    public static void addBlastTemp(Material m, int blastTemp) {
        if (checkFrozen("add blast temperature")) return;
        if (m.hasProperty(PropertyKey.BLAST)) m.getProperty(PropertyKey.BLAST).setBlastTemperature(blastTemp);
        else m.setProperty(PropertyKey.BLAST, new BlastProperty(blastTemp));
    }

    @ZenMethod
    public static void addBlastProperty(Material m, int blastTemp, @Optional String gasTier,
                                        @Optional int durationOverride, @Optional int eutOverride,
                                        @Optional int vacuumDurationOverride, @Optional int vacuumEUtOverride) {
        if (checkFrozen("add blast property")) return;
        if (m.hasProperty(PropertyKey.BLAST)) {
            BlastProperty property = m.getProperty(PropertyKey.BLAST);
            property.setBlastTemperature(blastTemp);
            if (gasTier != null) property.setGasTier(BlastProperty.validateGasTier(gasTier));
            if (durationOverride != 0) property.setDurationOverride(durationOverride);
            if (eutOverride != 0) property.setEutOverride(eutOverride);
            if (vacuumDurationOverride != 0) property.setVacuumDurationOverride(vacuumDurationOverride);
            if (vacuumEUtOverride != 0) property.setVacuumEutOverride(vacuumEUtOverride);
        } else {
            BlastProperty.Builder builder = new BlastProperty.Builder();
            builder.temp(blastTemp,
                    gasTier == null ? BlastProperty.GasTier.LOW : BlastProperty.validateGasTier(gasTier));
            builder.blastStats(durationOverride == 0 ? -1 : durationOverride, eutOverride == 0 ? -1 : eutOverride);
            builder.vacuumStats(vacuumEUtOverride == 0 ? -1 : vacuumEUtOverride,
                    vacuumDurationOverride == 0 ? -1 : vacuumDurationOverride);
            m.setProperty(PropertyKey.BLAST, builder.build());
        }
    }

    @ZenMethod
    public static void addDust(Material m, @Optional int harvestLevel, @Optional int burnTime) {
        if (checkFrozen("add a dust to a material")) return;
        if (harvestLevel == 0) harvestLevel = 2;
        if (m.hasProperty(PropertyKey.DUST)) {
            m.getProperty(PropertyKey.DUST).setHarvestLevel(harvestLevel);
            m.getProperty(PropertyKey.DUST).setBurnTime(burnTime);
        } else m.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
    }

    @ZenMethod
    public static void addFluid(Material m) {
        if (checkFrozen("add a Fluid to a material")) return;
        if (!m.hasProperty(PropertyKey.FLUID)) {
            FluidProperty property = new FluidProperty();
            property.enqueueRegistration(FluidStorageKeys.LIQUID, new FluidBuilder());
            m.setProperty(PropertyKey.FLUID, property);
        }
    }

    @ZenMethod
    public static void addFluid(Material m, @Optional String fluidTypeName, @Optional boolean hasBlock) {
        if (checkFrozen("add a Fluid to a material")) return;
        FluidState type = validateFluidState(fluidTypeName);
        FluidProperty property = m.getProperty(PropertyKey.FLUID);
        if (property == null) {
            property = new FluidProperty();
            m.setProperty(PropertyKey.FLUID, property);
        }

        FluidBuilder builder = switch (type) {
            case LIQUID -> property.getQueuedBuilder(FluidStorageKeys.LIQUID);
            case GAS -> property.getQueuedBuilder(FluidStorageKeys.GAS);
            case PLASMA -> property.getQueuedBuilder(FluidStorageKeys.PLASMA);
        };
        if (builder == null) {
            builder = new FluidBuilder();
            switch (type) {
                case LIQUID -> property.enqueueRegistration(FluidStorageKeys.LIQUID, builder);
                case GAS -> property.enqueueRegistration(FluidStorageKeys.GAS, builder.state(FluidState.GAS));
                case PLASMA -> property.enqueueRegistration(FluidStorageKeys.PLASMA, builder.state(FluidState.PLASMA));
            }
        }
        if (hasBlock) builder.block();
    }

    @ZenMethod
    public static void addGem(Material m) {
        if (checkFrozen("add a Gem to a material")) return;
        if (!m.hasProperty(PropertyKey.GEM)) m.setProperty(PropertyKey.GEM, new GemProperty());
    }

    @ZenMethod
    public static void addIngot(Material m) {
        if (checkFrozen("add an Ingot to a material")) return;
        if (!m.hasProperty(PropertyKey.INGOT)) m.setProperty(PropertyKey.INGOT, new IngotProperty());
    }

    @ZenMethod
    public static void addOre(Material m, @Optional int oreMultiplier, @Optional int byproductMultiplier,
                              @Optional boolean emissive) {
        if (checkFrozen("add an Ore to a material")) return;
        oreMultiplier = oreMultiplier == 0 ? 1 : oreMultiplier;
        byproductMultiplier = byproductMultiplier == 0 ? 1 : byproductMultiplier;
        if (m.hasProperty(PropertyKey.ORE)) {
            m.getProperty(PropertyKey.ORE).setOreMultiplier(oreMultiplier);
            m.getProperty(PropertyKey.ORE).setByProductMultiplier(byproductMultiplier);
            m.getProperty(PropertyKey.ORE).setEmissive(emissive);
        } else m.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
    }

    @ZenMethod
    public static void addPlasma(Material m) {
        if (checkFrozen("add a Plasma to a material")) return;
        if (!m.hasProperty(PropertyKey.FLUID)) {
            FluidProperty property = new FluidProperty();
            property.enqueueRegistration(FluidStorageKeys.PLASMA,
                    new FluidBuilder().state(FluidState.PLASMA));
            m.setProperty(PropertyKey.FLUID, property);
        }
    }

    @ZenMethod
    public static void addTools(Material m, float toolSpeed, float toolAttackDamage, float toolAttackSpeed,
                                int toolDurability, @Optional int toolHarvestLevel, @Optional int toolEnchantability,
                                @Optional int durabilityMultiplier) {
        if (checkFrozen("add Tools to a material")) return;
        if (toolEnchantability == 0) toolEnchantability = 10;
        if (durabilityMultiplier <= 0) durabilityMultiplier = 1;
        if (m.hasProperty(PropertyKey.TOOL)) {
            m.getProperty(PropertyKey.TOOL).setToolSpeed(toolSpeed);
            m.getProperty(PropertyKey.TOOL).setToolAttackDamage(toolAttackDamage);
            m.getProperty(PropertyKey.TOOL).setToolAttackSpeed(toolAttackSpeed);
            m.getProperty(PropertyKey.TOOL).setToolDurability(toolDurability);
            m.getProperty(PropertyKey.TOOL).setToolHarvestLevel(toolHarvestLevel);
            m.getProperty(PropertyKey.TOOL).setToolEnchantability(toolEnchantability);
            m.getProperty(PropertyKey.TOOL).setDurabilityMultiplier(durabilityMultiplier);
        } else m.setProperty(PropertyKey.TOOL,
                ToolProperty.Builder.of(toolSpeed, toolAttackDamage, toolDurability, toolHarvestLevel)
                        .attackSpeed(toolAttackSpeed).enchantability(toolEnchantability)
                        .durabilityMultiplier(durabilityMultiplier).build());
    }

    @ZenMethod
    public static void addFluidPipes(Material m, int maxFluidTemperature, int throughput, boolean gasProof) {
        addFluidPipes(m, maxFluidTemperature, throughput, gasProof, false, false);
    }

    @ZenMethod
    public static void addFluidPipes(Material m, int maxFluidTemperature, int throughput, boolean gasProof,
                                     boolean acidProof, boolean plasmaProof) {
        addFluidPipes(m, maxFluidTemperature, FluidConstants.CRYOGENIC_FLUID_THRESHOLD + 1, throughput, gasProof,
                acidProof, plasmaProof);
    }

    @ZenMethod
    public static void addFluidPipes(Material m, int maxFluidTemperature, int minFluidTemperature, int throughput,
                                     boolean gasProof,
                                     boolean acidProof, boolean plasmaProof) {
        if (checkFrozen("add fluid pipes to a material")) return;
        PipeNetProperties properties = m.getProperty(PropertyKey.PIPENET_PROPERTIES);
        if (properties == null) {
            properties = new PipeNetProperties();
            m.setProperty(PropertyKey.PIPENET_PROPERTIES, properties);
        }
        properties.setProperty(new MaterialFluidProperties(throughput, maxFluidTemperature, minFluidTemperature)
                .setContain(FluidState.GAS, gasProof).setContain(FluidAttributes.ACID, acidProof)
                .setContain(FluidState.PLASMA, plasmaProof));
    }

    @ZenMethod
    public static void addItemPipes(Material m, int priority, float transferRate) {
        if (checkFrozen("add Item Pipes to a material")) return;
        PipeNetProperties properties = m.getProperty(PropertyKey.PIPENET_PROPERTIES);
        if (properties == null) {
            properties = new PipeNetProperties();
            m.setProperty(PropertyKey.PIPENET_PROPERTIES, properties);
        }
        properties.setProperty(new MaterialItemProperties((long) (transferRate * 16), priority));
    }

    @ZenMethod
    public static void addWires(Material m, long voltage, long baseAmperage, long lossPerBlock,
                                @Optional boolean superconductor) {
        if (checkFrozen("add Wires to a material")) return;
        PipeNetProperties properties = m.getProperty(PropertyKey.PIPENET_PROPERTIES);
        if (properties == null) {
            properties = new PipeNetProperties();
            m.setProperty(PropertyKey.PIPENET_PROPERTIES, properties);
        }
        properties.setProperty(MaterialEnergyProperties.create(voltage, baseAmperage, lossPerBlock,
                superconductor));
    }
}
