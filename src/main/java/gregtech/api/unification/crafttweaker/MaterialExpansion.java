package gregtech.api.unification.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.enchantments.IEnchantment;
import crafttweaker.api.liquid.ILiquidDefinition;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.*;
import net.minecraft.enchantment.Enchantment;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.api.unification.crafttweaker.CTMaterialHelpers.checkFrozen;
import static gregtech.api.unification.crafttweaker.CTMaterialHelpers.logError;

@ZenExpansion("mods.gregtech.material.Material")
@ZenRegister
@SuppressWarnings("unused")
public class MaterialExpansion {

    ////////////////////////////////////
    //        Material Methods        //
    ////////////////////////////////////

    @ZenMethod
    public static void setFormula(Material m, String formula, @Optional boolean withFormatting) {
        if (checkFrozen("set material chemical formula")) return;
        m.setFormula(formula, withFormatting);
    }

    @ZenMethod
    public static boolean hasFlag(Material m, String flagName) {
        return m.hasFlag(MaterialFlag.getByName(flagName));
    }

    @ZenMethod
    public static void setIconSet(Material m, String iconSetName) {
        if (checkFrozen("set material icon set")) return;
        m.setMaterialIconSet(MaterialIconSet.getByName(iconSetName));
    }

    @ZenGetter("iconSet")
    public static String getIconSet(Material m) {
        return m.getMaterialIconSet().getName();
    }

    ////////////////////////////////////
    //         Fluid Property         //
    ////////////////////////////////////

    @ZenGetter
    public static boolean isGaseous(Material m) {
        FluidProperty prop = m.getProperty(PropertyKey.FLUID);
        return prop != null && prop.isGas();
    }

    @ZenMethod
    public static void setFluidTemperature(Material m, int fluidTemperature) {
        if (checkFrozen("set fluid temperature")) return;
        FluidProperty prop = m.getProperty(PropertyKey.FLUID);
        if (prop != null) {
            prop.setFluidTemperature(fluidTemperature);
        } else logError(m, "set temperature", "Fluid");
    }

    @ZenGetter("fluidTemperature") // todo is this allowed here?
    public static int fluidTemperature(Material m) {
        FluidProperty prop = m.getProperty(PropertyKey.FLUID);
        if (prop != null) {
            return prop.getFluidTemperature();
        } else logError(m, "get temperature", "Fluid");
        return 0;
    }

    // TODO May need to move this to Material
    @ZenGetter("fluid")
    @net.minecraftforge.fml.common.Optional.Method(modid = GTValues.MODID_CT)
    public static ILiquidDefinition getFluid(Material m) {
        FluidProperty prop = m.getProperty(PropertyKey.FLUID);
        if (prop != null) {
            return CraftTweakerMC.getILiquidDefinition(prop.getFluid());
        } else logError(m, "get a Fluid", "Fluid");
        return null;
    }

    ///////////////////////////////////
    //         Dust Property         //
    ///////////////////////////////////

    @ZenGetter("harvestLevel")
    public static int harvestLevel(Material m) {
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            return prop.getHarvestLevel();
        } else logError(m, "get the harvest level", "Dust");
        return 0;
    }

    @ZenGetter("burnTime")
    public static int burnTime(Material m) {
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            return prop.getBurnTime();
        } else logError(m, "get the burn time", "Dust");
        return 0;
    }

    @ZenMethod
    public static void setHarvestLevel(Material m, int harvestLevel) {
        if (checkFrozen("set harvest level")) return;
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            prop.setHarvestLevel(harvestLevel);
        } else logError(m, "set the harvest level", "Dust");
    }

    @ZenMethod
    public static void setBurnTime(Material m, int burnTime) {
        if (checkFrozen("set burn time")) return;
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            prop.setBurnTime(burnTime);
        } else logError(m, "set the burn time", "Dust");
    }

    ////////////////////////////////////
    //         Ingot Property         //
    ////////////////////////////////////

    // Plasma Property
    @ZenGetter("plasma")
    @net.minecraftforge.fml.common.Optional.Method(modid = GTValues.MODID_CT)
    public static ILiquidDefinition getPlasma(Material m) {
        PlasmaProperty prop = m.getProperty(PropertyKey.PLASMA);
        if (prop != null) {
            return CraftTweakerMC.getILiquidDefinition(prop.getPlasma());
        } else logError(m, "get a Plasma", "Plasma");
        return null;
    }

    ///////////////////////////////////
    //         Tool Property         //
    ///////////////////////////////////

    @ZenGetter("toolSpeed")
    public static float toolSpeed(Material m) {
        ToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolSpeed();
        } else logError(m, "get the tool speed", "Tool");
        return 0;
    }

    @ZenGetter("toolAttackDamage")
    public static float attackDamage(Material m) {
        ToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolAttackDamage();
        } else logError(m, "get the tool attack damage", "Tool");
        return 0;
    }

    @ZenGetter("toolDurability")
    public static int toolDurability(Material m) {
        ToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolDurability();
        } else logError(m, "get the tool durability", "Tool");
        return 0;
    }

    @ZenGetter("toolEnchantability")
    public static int toolEnchant(Material m) {
        ToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolEnchantability();
        } else logError(m, "get the tool enchantability", "Tool");
        return 0;
    }

    @ZenMethod
    @net.minecraftforge.fml.common.Optional.Method(modid = GTValues.MODID_CT)
    public static void addToolEnchantment(Material m, IEnchantment enchantment) {
        if (checkFrozen("add tool enchantment")) return;
        ToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            Enchantment enchantmentType = (Enchantment) enchantment.getDefinition().getInternal();
            prop.addEnchantmentForTools(enchantmentType, enchantment.getLevel());
        } else logError(m, "change tool enchantments", "Tool");
    }

    @ZenMethod
    public static void setToolStats(Material m, float toolSpeed, float toolAttackDamage, int toolDurability, @Optional int enchantability, @Optional boolean shouldIngoreCraftingTools) {
        if (checkFrozen("set tool stats")) return;
        ToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            prop.setToolSpeed(toolSpeed);
            prop.setToolAttackDamage(toolAttackDamage);
            prop.setToolDurability(toolDurability);
            prop.setToolEnchantability(enchantability == 0 ? 10 : enchantability);
            prop.setShouldIgnoreCraftingTools(shouldIngoreCraftingTools);
        } else logError(m, "change tool stats", "Tool");
    }

    // Wire/Item Pipe/Fluid Pipe stuff?

    ////////////////////////////////////
    //         Blast Property         //
    ////////////////////////////////////

    @ZenMethod
    public static void setBlastTemp(Material m, int blastTemp) {
        if (checkFrozen("set blast temperature")) return;
        if (blastTemp <= 0) {
            CraftTweakerAPI.logError("Blast Temperature must be greater than zero! Material: " + m.getUnlocalizedName());
            return;
        }
        BlastProperty prop = m.getProperty(PropertyKey.BLAST);
        if (prop != null) prop.setBlastTemperature(blastTemp);
        else m.setProperty(PropertyKey.BLAST, new BlastProperty(blastTemp));
    }

    @ZenGetter
    public static int blastTemp(Material m) {
        BlastProperty prop = m.getProperty(PropertyKey.BLAST);
        if (prop != null) {
            return prop.getBlastTemperature();
        } else logError(m, "get blast temperature", "Blast");
        return 0;
    }

    ////////////////////////////////////
    //          Ore Property          //
    ////////////////////////////////////

    @ZenGetter
    public static int oreMultiplier(Material m) {
        OreProperty prop = m.getProperty(PropertyKey.ORE);
        if (prop != null) {
            return prop.getOreMultiplier();
        } else logError(m, "get ore multiplier", "Ore");
        return 0;
    }
}
