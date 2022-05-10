package gregtech.api.unification.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.*;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.api.unification.crafttweaker.CTMaterialHelpers.checkFrozen;
import static gregtech.api.unification.crafttweaker.CTMaterialHelpers.validateFluidTypeNoPlasma;

@ZenExpansion("mods.gregtech.material.Material")
@ZenRegister
@SuppressWarnings("unused")
public class MaterialPropertyExpansion {

    /////////////////////////////////////
    //        Property Checkers        //
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
    public static boolean hasFluidPipes(Material m) {
        return m.hasProperty(PropertyKey.FLUID_PIPE);
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
    public static boolean hasItemPipes(Material m) {
        return m.hasProperty(PropertyKey.ITEM_PIPE);
    }

    @ZenMethod
    public static boolean hasOre(Material m) {
        return m.hasProperty(PropertyKey.ORE);
    }

    @ZenMethod
    public static boolean hasPlasma(Material m) {
        return m.hasProperty(PropertyKey.PLASMA);
    }

    @ZenMethod
    public static boolean hasTools(Material m) {
        return m.hasProperty(PropertyKey.TOOL);
    }

    @ZenMethod
    public static boolean hasWires(Material m) {
        return m.hasProperty(PropertyKey.WIRE);
    }

    ////////////////////////////////////
    //        Property Setters        //
    ////////////////////////////////////

    @ZenMethod
    public static void addBlastTemp(Material m, int blastTemp) {
        if (checkFrozen("add blast temperature")) return;
        if (m.hasProperty(PropertyKey.BLAST)) m.getProperty(PropertyKey.BLAST).setBlastTemperature(blastTemp);
        else m.setProperty(PropertyKey.BLAST, new BlastProperty(blastTemp));
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
    public static void addFluidPipes(Material m, int maxFluidTemperature, int throughput, boolean gasProof) {
        addFluidPipes(m, maxFluidTemperature, throughput, gasProof, false, false, false);
    }

    @ZenMethod
    public static void addFluidPipes(Material m, int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        if (checkFrozen("add fluid pipes to a material")) return;
        if (m.hasProperty(PropertyKey.FLUID_PIPE)) {
            m.getProperty(PropertyKey.FLUID_PIPE).setMaxFluidTemperature(maxFluidTemperature);
            m.getProperty(PropertyKey.FLUID_PIPE).setThroughput(throughput);
            m.getProperty(PropertyKey.FLUID_PIPE).setGasProof(gasProof);
            m.getProperty(PropertyKey.FLUID_PIPE).setAcidProof(acidProof);
            m.getProperty(PropertyKey.FLUID_PIPE).setCryoProof(cryoProof);
            m.getProperty(PropertyKey.FLUID_PIPE).setPlasmaProof(plasmaProof);
        } else {
            m.setProperty(PropertyKey.FLUID_PIPE, new FluidPipeProperties(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof));
        }
    }

    @ZenMethod
    public static void addFluid(Material m) {
        if (checkFrozen("add a Fluid to a material")) return;
        if (m.hasProperty(PropertyKey.FLUID)) {
            m.getProperty(PropertyKey.FLUID).setIsGas(false);
            m.getProperty(PropertyKey.FLUID).setHasBlock(false);
        } else m.setProperty(PropertyKey.FLUID, new FluidProperty());
    }

    @ZenMethod
    public static void addFluid(Material m, @Optional String fluidTypeName, @Optional boolean hasBlock) {
        if (checkFrozen("add a Fluid to a material")) return;
        FluidType type = validateFluidTypeNoPlasma(fluidTypeName);
        if (m.hasProperty(PropertyKey.FLUID)) {
            m.getProperty(PropertyKey.FLUID).setIsGas(type == FluidTypes.GAS);
            m.getProperty(PropertyKey.FLUID).setHasBlock(hasBlock);
        } else m.setProperty(PropertyKey.FLUID, new FluidProperty(type, hasBlock));
    }

    @ZenMethod
    public static void addFluid(Material m, @Optional FluidType fluidType, @Optional boolean hasBlock) {
        if (checkFrozen("add a Fluid to a material")) return;
        FluidType type = validateFluidTypeNoPlasma(fluidType == null ? null : fluidType.getName());
        if (m.hasProperty(PropertyKey.FLUID)) {
            m.getProperty(PropertyKey.FLUID).setIsGas(type == FluidTypes.GAS);
            m.getProperty(PropertyKey.FLUID).setHasBlock(hasBlock);
        } else m.setProperty(PropertyKey.FLUID, new FluidProperty(type, hasBlock));
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
    public static void addOre(Material m, @Optional int oreMultiplier, @Optional int byproductMultiplier, @Optional boolean emissive) {
        if (checkFrozen("add an Ore to a material")) return;
        oreMultiplier = oreMultiplier == 0 ? 1 : oreMultiplier;
        byproductMultiplier = byproductMultiplier == 0 ? 1 : byproductMultiplier;
        if (m.hasProperty(PropertyKey.ORE)) {
            m.getProperty(PropertyKey.ORE).setOreMultiplier(oreMultiplier);
            m.getProperty(PropertyKey.ORE).setByProductMultiplier(byproductMultiplier);
            m.getProperty(PropertyKey.ORE).setEmissive(emissive);
        }
        else m.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
    }

    @ZenMethod
    public static void addItemPipes(Material m, int priority, float transferRate) {
        if (checkFrozen("add Item Pipes to a material")) return;
        if (m.hasProperty(PropertyKey.ITEM_PIPE)) {
            m.getProperty(PropertyKey.ITEM_PIPE).setPriority(priority);
            m.getProperty(PropertyKey.ITEM_PIPE).setTransferRate(transferRate);
        } else m.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, transferRate));
    }

    @ZenMethod
    public static void addPlasma(Material m) {
        if (checkFrozen("add a Plasma to a material")) return;
        if (!m.hasProperty(PropertyKey.PLASMA)) m.setProperty(PropertyKey.PLASMA, new PlasmaProperty());
    }

    @ZenMethod
    public static void addTools(Material m, float toolSpeed, float toolAttackDamage, int toolDurability, @Optional int toolEnchantability, @Optional boolean shouldIgnoreCraftingTools) {
        if (checkFrozen("add Tools to a material")) return;
        if (toolEnchantability == 0) toolEnchantability = 10;
        if (m.hasProperty(PropertyKey.TOOL)) {
            m.getProperty(PropertyKey.TOOL).setToolSpeed(toolSpeed);
            m.getProperty(PropertyKey.TOOL).setToolAttackDamage(toolAttackDamage);
            m.getProperty(PropertyKey.TOOL).setToolDurability(toolDurability);
            m.getProperty(PropertyKey.TOOL).setToolEnchantability(toolEnchantability);
            m.getProperty(PropertyKey.TOOL).setShouldIgnoreCraftingTools(shouldIgnoreCraftingTools);
        } else m.setProperty(PropertyKey.TOOL, new ToolProperty(toolSpeed, toolAttackDamage, toolDurability, toolEnchantability, shouldIgnoreCraftingTools));
    }

    @ZenMethod
    public static void addWires(Material m, int voltage, int baseAmperage, int lossPerBlock, @Optional boolean isSuperCon, @Optional int criticalTemp) {
        if (checkFrozen("add Wires to a material")) return;
        if (m.hasProperty(PropertyKey.WIRE)) {
            m.getProperty(PropertyKey.WIRE).setVoltage(voltage);
            m.getProperty(PropertyKey.WIRE).setAmperage(baseAmperage);
            m.getProperty(PropertyKey.WIRE).setLossPerBlock(lossPerBlock);
            m.getProperty(PropertyKey.WIRE).setSuperconductor(isSuperCon);
            m.getProperty(PropertyKey.WIRE).setSuperconductorCriticalTemperature(criticalTemp);
        } else m.setProperty(PropertyKey.WIRE, new WireProperties(voltage, baseAmperage, lossPerBlock, isSuperCon, criticalTemp));
    }
}
