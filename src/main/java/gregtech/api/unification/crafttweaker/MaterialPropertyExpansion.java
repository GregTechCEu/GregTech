package gregtech.api.unification.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.*;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.api.unification.crafttweaker.CTMaterialHelpers.*;

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
        if (checkFrozen("add fluid pipes to a material")) return;
        if (m.hasProperty(PropertyKey.FLUID_PIPE)) {
            m.getProperty(PropertyKey.FLUID_PIPE).setMaxFluidTemperature(maxFluidTemperature);;
            m.getProperty(PropertyKey.FLUID_PIPE).setThroughput(throughput);
            m.getProperty(PropertyKey.FLUID_PIPE).setGasProof(gasProof);
        } else m.setProperty(PropertyKey.FLUID_PIPE, new FluidPipeProperties(maxFluidTemperature, throughput, gasProof));
    }

    @ZenMethod
    public static void addFluid(Material m, @Optional String fluidTypeName, @Optional boolean hasBlock) {
        if (checkFrozen("add a Fluid to a material")) return;
        Material.FluidType type = validateFluidType(fluidTypeName);
        if (m.hasProperty(PropertyKey.FLUID)) {
            m.getProperty(PropertyKey.FLUID).setIsGas(type == Material.FluidType.GAS);
            m.getProperty(PropertyKey.FLUID).setHasBlock(hasBlock);
        } else m.setProperty(PropertyKey.FLUID, new FluidProperty(type == Material.FluidType.GAS, hasBlock));
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
    public static void addItemPipes(Material m, int priority, float transferRate) {
        if (checkFrozen("add Item Pipes to a material")) return;
        if (m.hasProperty(PropertyKey.ITEM_PIPE)) {
            m.getProperty(PropertyKey.ITEM_PIPE).priority = priority;
            m.getProperty(PropertyKey.ITEM_PIPE).transferRate = transferRate;
        } else m.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, transferRate));
    }

    @ZenMethod
    public static void addPlasma(Material m) {
        if (checkFrozen("add a Plasma to a material")) return;
        if (!m.hasProperty(PropertyKey.PLASMA)) m.setProperty(PropertyKey.PLASMA, new PlasmaProperty());
    }

    @ZenMethod
    public static void addTools(Material m, float toolSpeed, float toolAttackDamage, int toolDurability, @Optional int toolEnchantability) {
        if (checkFrozen("add Tools to a material")) return;
        if (toolEnchantability == 0) toolEnchantability = 10;
        if (m.hasProperty(PropertyKey.TOOL)) {
            m.getProperty(PropertyKey.TOOL).setToolSpeed(toolSpeed);
            m.getProperty(PropertyKey.TOOL).setToolAttackDamage(toolAttackDamage);
            m.getProperty(PropertyKey.TOOL).setToolDurability(toolDurability);
            m.getProperty(PropertyKey.TOOL).setToolEnchantability(toolEnchantability);
        } else m.setProperty(PropertyKey.TOOL, new ToolProperty(toolSpeed, toolAttackDamage, toolDurability, toolEnchantability));
    }

    @ZenMethod
    public static void addWires(Material m, int voltage, int baseAmperage, int lossPerBlock, @Optional boolean isSuperCon) {
        if (checkFrozen("add Wires to a material")) return;
        if (m.hasProperty(PropertyKey.WIRE)) {
            m.getProperty(PropertyKey.WIRE).voltage = voltage;
            m.getProperty(PropertyKey.WIRE).amperage = baseAmperage;
            m.getProperty(PropertyKey.WIRE).lossPerBlock = lossPerBlock;
            m.getProperty(PropertyKey.WIRE).isSuperconductor = isSuperCon;
        } else m.setProperty(PropertyKey.WIRE, new WireProperties(voltage, baseAmperage, lossPerBlock, isSuperCon));
    }
}
