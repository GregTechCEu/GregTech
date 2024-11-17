package gregtech.integration.groovy;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.GemProperty;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.unification.material.properties.WoodProperty;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.GroovyLog;

import static gregtech.integration.groovy.GroovyScriptModule.checkFrozen;

@SuppressWarnings("unused")
public class MaterialPropertyExpansion {

    /////////////////////////////////////
    // Property Checkers //
    /////////////////////////////////////

    public static boolean hasBlastTemp(Material m) {
        return m.hasProperty(PropertyKey.BLAST);
    }

    public static boolean hasDust(Material m) {
        return m.hasProperty(PropertyKey.DUST);
    }

    public static boolean hasFluidPipes(Material m) {
        return m.hasProperty(PropertyKey.FLUID_PIPE);
    }

    public static boolean hasFluid(Material m) {
        return m.hasProperty(PropertyKey.FLUID);
    }

    public static boolean hasGem(Material m) {
        return m.hasProperty(PropertyKey.GEM);
    }

    public static boolean hasIngot(Material m) {
        return m.hasProperty(PropertyKey.INGOT);
    }

    public static boolean hasItemPipes(Material m) {
        return m.hasProperty(PropertyKey.ITEM_PIPE);
    }

    public static boolean hasOre(Material m) {
        return m.hasProperty(PropertyKey.ORE);
    }

    public static boolean hasTools(Material m) {
        return m.hasProperty(PropertyKey.TOOL);
    }

    public static boolean hasWires(Material m) {
        return m.hasProperty(PropertyKey.WIRE);
    }

    ////////////////////////////////////
    // Property Setters //
    ////////////////////////////////////

    public static void addBlastTemp(Material m, int blastTemp) {
        if (checkFrozen("add blast temperature")) return;
        if (m.hasProperty(PropertyKey.BLAST)) m.getProperty(PropertyKey.BLAST).setBlastTemperature(blastTemp);
        else m.setProperty(PropertyKey.BLAST, new BlastProperty(blastTemp));
    }

    public static void addBlastProperty(Material m, int blastTemp) {
        addBlastProperty(m, blastTemp, null, 0, 0, 0, 0);
    }

    public static void addBlastProperty(Material m, int blastTemp, String gasTier) {
        addBlastProperty(m, blastTemp, gasTier, 0, 0, 0, 0);
    }

    public static void addBlastProperty(Material m, int blastTemp, String gasTier,
                                        int durationOverride) {
        addBlastProperty(m, blastTemp, gasTier, durationOverride, 0, 0, 0);
    }

    public static void addBlastProperty(Material m, int blastTemp, String gasTier,
                                        int durationOverride, int eutOverride) {
        addBlastProperty(m, blastTemp, gasTier, durationOverride, eutOverride, 0, 0);
    }

    public static void addBlastProperty(Material m, int blastTemp, String gasTier,
                                        int durationOverride, int eutOverride,
                                        int vacuumDurationOverride, int vacuumEUtOverride) {
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

    public static void addDust(Material m) {
        addDust(m, 0, 0);
    }

    public static void addDust(Material m, int harvestLevel) {
        addDust(m, harvestLevel, 0);
    }

    public static void addDust(Material m, int harvestLevel, int burnTime) {
        if (checkFrozen("add a dust to a material")) return;
        if (harvestLevel == 0) harvestLevel = 2;
        if (m.hasProperty(PropertyKey.DUST)) {
            m.getProperty(PropertyKey.DUST).setHarvestLevel(harvestLevel);
            m.getProperty(PropertyKey.DUST).setBurnTime(burnTime);
        } else m.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
    }

    public static void addWood(Material m) {
        if (checkFrozen("add a wood to a material")) return;
        if (!m.hasProperty(PropertyKey.WOOD)) {
            m.setProperty(PropertyKey.WOOD, new WoodProperty());
        }
    }

    public static void addFluidPipes(Material m, int maxFluidTemperature, int throughput, boolean gasProof) {
        addFluidPipes(m, maxFluidTemperature, throughput, gasProof, false, false, false);
    }

    public static void addFluidPipes(Material m, int maxFluidTemperature, int throughput, boolean gasProof,
                                     boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        if (checkFrozen("add fluid pipes to a material")) return;
        if (m.hasProperty(PropertyKey.FLUID_PIPE)) {
            m.getProperty(PropertyKey.FLUID_PIPE).setMaxFluidTemperature(maxFluidTemperature);
            m.getProperty(PropertyKey.FLUID_PIPE).setThroughput(throughput);
            m.getProperty(PropertyKey.FLUID_PIPE).setGasProof(gasProof);
            m.getProperty(PropertyKey.FLUID_PIPE).setCanContain(FluidAttributes.ACID, acidProof);
            m.getProperty(PropertyKey.FLUID_PIPE).setCryoProof(cryoProof);
            m.getProperty(PropertyKey.FLUID_PIPE).setPlasmaProof(plasmaProof);
        } else {
            m.setProperty(PropertyKey.FLUID_PIPE, new FluidPipeProperties(maxFluidTemperature, throughput, gasProof,
                    acidProof, cryoProof, plasmaProof));
        }
    }

    @GroovyBlacklist
    private static void addFluidInternal(Material m, FluidStorageKey key, FluidBuilder builder) {
        if (checkFrozen("add a Fluid to a material")) return;
        FluidProperty property = m.getProperty(PropertyKey.FLUID);
        if (property == null) {
            property = new FluidProperty();
            m.setProperty(PropertyKey.FLUID, property);
        }
        property.enqueueRegistration(key, builder);
    }

    public static void addLiquid(Material m, FluidBuilder builder) {
        addFluidInternal(m, FluidStorageKeys.LIQUID, builder.state(FluidState.LIQUID));
    }

    public static void addLiquid(Material m) {
        addLiquid(m, new FluidBuilder());
    }

    public static void addFluid(Material m) {
        addLiquid(m);
    }

    @Deprecated
    public static void addFluid(Material m, String fluidTypeName) {
        addFluid(m, fluidTypeName, false);
    }

    @Deprecated
    public static void addFluid(Material m, String fluidTypeName, boolean hasBlock) {
        GroovyLog.get().error("The usage of `material.addFluid(String, boolean)` is strongly discouraged. " +
                "Please use `addLiquid()`, `addGas()` or `addPlasma()` with or without `FluidBuilder`.");
        if (checkFrozen("add a Fluid to a material")) return;
        FluidState type = GroovyScriptModule.parseAndValidateEnumValue(FluidState.class, fluidTypeName, "fluid type",
                true);
        FluidStorageKey storageKey = switch (type) {
            case LIQUID -> FluidStorageKeys.LIQUID;
            case GAS -> FluidStorageKeys.GAS;
            case PLASMA -> FluidStorageKeys.PLASMA;
        };
        FluidBuilder builder = new FluidBuilder();
        builder.state(type);
        if (hasBlock) builder.block();
        addFluidInternal(m, storageKey, builder);
    }

    public static void addGas(Material m, FluidBuilder builder) {
        addFluidInternal(m, FluidStorageKeys.GAS, builder.state(FluidState.GAS));
    }

    public static void addGas(Material m) {
        addGas(m, new FluidBuilder());
    }

    public static void addPlasma(Material m, FluidBuilder builder) {
        addFluidInternal(m, FluidStorageKeys.PLASMA, builder.state(FluidState.PLASMA));
    }

    public static void addPlasma(Material m) {
        addPlasma(m, new FluidBuilder());
    }

    public static void addGem(Material m) {
        if (checkFrozen("add a Gem to a material")) return;
        if (!m.hasProperty(PropertyKey.GEM)) m.setProperty(PropertyKey.GEM, new GemProperty());
    }

    public static void addIngot(Material m) {
        if (checkFrozen("add an Ingot to a material")) return;
        if (!m.hasProperty(PropertyKey.INGOT)) m.setProperty(PropertyKey.INGOT, new IngotProperty());
    }

    public static void addOre(Material m) {
        addOre(m, false);
    }

    public static void addOre(Material m, boolean emissive) {
        addOre(m, 0, 0, emissive);
    }

    public static void addOre(Material m, int oreMultiplier, int byproductMultiplier) {
        addOre(m, oreMultiplier, byproductMultiplier, false);
    }

    public static void addOre(Material m, int oreMultiplier, int byproductMultiplier, boolean emissive) {
        if (checkFrozen("add an Ore to a material")) return;
        oreMultiplier = oreMultiplier == 0 ? 1 : oreMultiplier;
        byproductMultiplier = byproductMultiplier == 0 ? 1 : byproductMultiplier;
        if (m.hasProperty(PropertyKey.ORE)) {
            m.getProperty(PropertyKey.ORE).setOreMultiplier(oreMultiplier);
            m.getProperty(PropertyKey.ORE).setByProductMultiplier(byproductMultiplier);
            m.getProperty(PropertyKey.ORE).setEmissive(emissive);
        } else m.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
    }

    public static void addItemPipes(Material m, int priority, float transferRate) {
        if (checkFrozen("add Item Pipes to a material")) return;
        if (m.hasProperty(PropertyKey.ITEM_PIPE)) {
            m.getProperty(PropertyKey.ITEM_PIPE).setPriority(priority);
            m.getProperty(PropertyKey.ITEM_PIPE).setTransferRate(transferRate);
        } else m.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, transferRate));
    }

    public static void addTools(Material m, ToolProperty.Builder builder) {
        addTools(m, builder.build());
    }

    public static void addTools(Material m, ToolProperty prop) {
        if (checkFrozen("add Tools to a material")) return;
        ToolProperty property = m.getProperty(PropertyKey.TOOL);
        if (property != null) {
            property.setToolSpeed(prop.getToolSpeed());
            property.setToolAttackDamage(prop.getToolAttackDamage());
            property.setToolDurability(prop.getToolDurability());
            property.setToolHarvestLevel(prop.getToolHarvestLevel());
            property.setToolAttackSpeed(prop.getToolAttackSpeed());
            property.setToolEnchantability(prop.getToolEnchantability());
            property.setMagnetic(prop.isMagnetic());
            property.setUnbreakable(prop.getUnbreakable());
            property.setShouldIgnoreCraftingTools(prop.getShouldIgnoreCraftingTools());
            property.setDurabilityMultiplier(prop.getDurabilityMultiplier());
        } else {
            m.setProperty(PropertyKey.TOOL, prop);
        }
    }

    public static void addTools(Material m, float toolSpeed, float toolAttackDamage, float toolAttackSpeed,
                                int toolDurability) {
        addTools(m, toolSpeed, toolAttackDamage, toolAttackSpeed, toolDurability, 0, 10, 1);
    }

    public static void addTools(Material m, float toolSpeed, float toolAttackDamage, float toolAttackSpeed,
                                int toolDurability, int toolHarvestLevel) {
        addTools(m, toolSpeed, toolAttackDamage, toolAttackSpeed, toolDurability, toolHarvestLevel, 10, 1);
    }

    public static void addTools(Material m, float toolSpeed, float toolAttackDamage, float toolAttackSpeed,
                                int toolDurability, int toolHarvestLevel, int toolEnchantability) {
        addTools(m, toolSpeed, toolAttackDamage, toolAttackSpeed, toolDurability, toolHarvestLevel, toolEnchantability,
                1);
    }

    public static void addTools(Material m, float toolSpeed, float toolAttackDamage, float toolAttackSpeed,
                                int toolDurability, int toolHarvestLevel, int toolEnchantability,
                                int durabilityMultiplier) {
        if (toolEnchantability == 0) toolEnchantability = 10;
        if (durabilityMultiplier <= 0) durabilityMultiplier = 1;
        addTools(m, ToolProperty.Builder.of(toolSpeed, toolAttackDamage, toolDurability, toolHarvestLevel)
                .attackSpeed(toolAttackSpeed)
                .enchantability(toolEnchantability)
                .durabilityMultiplier(durabilityMultiplier));
    }

    public static void addWires(Material m, int voltage, int baseAmperage, int lossPerBlock) {
        addWires(m, voltage, baseAmperage, lossPerBlock, false, 0);
    }

    public static void addWires(Material m, int voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon) {
        addWires(m, voltage, baseAmperage, lossPerBlock, isSuperCon, 0);
    }

    public static void addWires(Material m, int voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon,
                                int criticalTemp) {
        if (checkFrozen("add Wires to a material")) return;
        if (m.hasProperty(PropertyKey.WIRE)) {
            m.getProperty(PropertyKey.WIRE).setVoltage(voltage);
            m.getProperty(PropertyKey.WIRE).setAmperage(baseAmperage);
            m.getProperty(PropertyKey.WIRE).setLossPerBlock(lossPerBlock);
            m.getProperty(PropertyKey.WIRE).setSuperconductor(isSuperCon);
            m.getProperty(PropertyKey.WIRE).setSuperconductorCriticalTemperature(criticalTemp);
        } else m.setProperty(PropertyKey.WIRE,
                new WireProperties(voltage, baseAmperage, lossPerBlock, isSuperCon, criticalTemp));
    }

    public static void addCables(Material m, int voltage, int baseAmperage, int lossPerBlock) {
        addWires(m, voltage, baseAmperage, lossPerBlock, false, 0);
    }

    public static void addCables(Material m, int voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon) {
        addWires(m, voltage, baseAmperage, lossPerBlock, isSuperCon, 0);
    }

    public static void addCables(Material m, int voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon,
                                 int criticalTemp) {
        addWires(m, voltage, baseAmperage, lossPerBlock, isSuperCon, criticalTemp);
    }
}
