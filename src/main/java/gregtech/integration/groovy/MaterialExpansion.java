package gregtech.integration.groovy;

import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.ExtraToolProperty;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.MaterialToolProperty;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import net.minecraft.enchantment.Enchantment;

import com.cleanroommc.groovyscript.api.GroovyLog;

import static gregtech.integration.groovy.GroovyScriptModule.checkFrozen;
import static gregtech.integration.groovy.GroovyScriptModule.logError;

@SuppressWarnings("unused")
public class MaterialExpansion {

    ////////////////////////////////////
    // Material Methods //
    ////////////////////////////////////

    public static Material setFormula(Material m, String formula) {
        return setFormula(m, formula, false);
    }

    public static Material setFormula(Material m, String formula, boolean withFormatting) {
        if (checkFrozen("set material chemical formula")) return m;
        return m.setFormula(formula, withFormatting);
    }

    public static boolean hasFlag(Material m, String flagName) {
        return m.hasFlag(MaterialFlag.getByName(flagName));
    }

    public static void setIconSet(Material m, String iconSetName) {
        if (checkFrozen("set material icon set")) return;
        m.setMaterialIconSet(MaterialIconSet.getByName(iconSetName));
    }

    public static String getIconSet(Material m) {
        return m.getMaterialIconSet().getName();
    }

    ////////////////////////////////////
    // Fluid Property //
    ////////////////////////////////////

    public static boolean isGaseous(Material m) {
        FluidProperty prop = m.getProperty(PropertyKey.FLUID);
        return prop != null && prop.get(FluidStorageKeys.GAS) != null;
    }

    ///////////////////////////////////
    // Dust Property //
    ///////////////////////////////////

    public static int harvestLevel(Material m) {
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            return prop.getHarvestLevel();
        } else logError(m, "get the harvest level", "Dust");
        return 0;
    }

    public static int burnTime(Material m) {
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            return prop.getBurnTime();
        } else logError(m, "get the burn time", "Dust");
        return 0;
    }

    public static Material setHarvestLevel(Material m, int harvestLevel) {
        if (checkFrozen("set harvest level")) return m;
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            prop.setHarvestLevel(harvestLevel);
        } else logError(m, "set the harvest level", "Dust");
        return m;
    }

    public static Material setBurnTime(Material m, int burnTime) {
        if (checkFrozen("set burn time")) return m;
        DustProperty prop = m.getProperty(PropertyKey.DUST);
        if (prop != null) {
            prop.setBurnTime(burnTime);
        } else logError(m, "set the burn time", "Dust");
        return m;
    }

    ///////////////////////////////////
    // Tool Property //
    ///////////////////////////////////
    public static float toolSpeed(Material m) {
        MaterialToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolSpeed();
        } else logError(m, "get the tool speed", "Tool");
        return 0;
    }

    public static float attackDamage(Material m) {
        MaterialToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolAttackDamage();
        } else logError(m, "get the tool attack damage", "Tool");
        return 0;
    }

    public static int toolDurability(Material m) {
        MaterialToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolDurability();
        } else logError(m, "get the tool durability", "Tool");
        return 0;
    }

    public static int toolHarvestLevel(Material m) {
        MaterialToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolHarvestLevel();
        } else logError(m, "get the tool harvest level", "Tool");
        return 0;
    }

    public static int toolEnchantability(Material m) {
        MaterialToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            return prop.getToolEnchantability();
        } else logError(m, "get the tool enchantability", "Tool");
        return 0;
    }

    public static Material addToolEnchantment(Material m, Enchantment enchantment, int level) {
        return addScaledToolEnchantment(m, enchantment, level, 0);
    }

    public static Material addScaledToolEnchantment(Material m, Enchantment enchantment, int level,
                                                    double levelGrowth) {
        if (checkFrozen("add tool enchantment")) return m;
        MaterialToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            prop.addEnchantmentForTools(enchantment, level, levelGrowth);
        } else logError(m, "change tool enchantments", "Tool");
        return m;
    }

    public static Material setToolStats(Material m, float toolSpeed, float toolAttackDamage, int toolDurability,
                                        boolean shouldIngoreCraftingTools) {
        return setToolStats(m, toolSpeed, toolAttackDamage, toolDurability, 0, 0, shouldIngoreCraftingTools);
    }

    public static Material setToolStats(Material m, float toolSpeed, float toolAttackDamage, int toolDurability,
                                        int enchantability, boolean shouldIngoreCraftingTools) {
        return setToolStats(m, toolSpeed, toolAttackDamage, toolDurability, enchantability, 0,
                shouldIngoreCraftingTools);
    }

    public static Material setToolStats(Material m, float toolSpeed, float toolAttackDamage, int toolDurability) {
        return setToolStats(m, toolSpeed, toolAttackDamage, toolDurability, 0, 0, false);
    }

    public static Material setToolStats(Material m, float toolSpeed, float toolAttackDamage, int toolDurability,
                                        int enchantability) {
        return setToolStats(m, toolSpeed, toolAttackDamage, toolDurability, enchantability, 0, false);
    }

    public static Material setToolStats(Material m, float toolSpeed, float toolAttackDamage, int toolDurability,
                                        int enchantability, int toolHarvestLevel) {
        return setToolStats(m, toolSpeed, toolAttackDamage, toolDurability, enchantability, toolHarvestLevel, false);
    }

    public static Material setToolStats(Material m, float toolSpeed, float toolAttackDamage, int toolDurability,
                                        int enchantability, int toolHarvestLevel,
                                        boolean shouldIngoreCraftingTools) {
        if (checkFrozen("set tool stats")) return m;
        MaterialToolProperty prop = m.getProperty(PropertyKey.TOOL);
        if (prop != null) {
            prop.setToolSpeed(toolSpeed);
            prop.setToolAttackDamage(toolAttackDamage);
            prop.setToolDurability(toolDurability);
            prop.setToolHarvestLevel(toolHarvestLevel == 0 ? 2 : toolHarvestLevel);
            prop.setToolEnchantability(enchantability == 0 ? 10 : enchantability);
            prop.setShouldIgnoreCraftingTools(shouldIngoreCraftingTools);
        } else logError(m, "change tool stats", "Tool");
        return m;
    }

    ////////////////////////////////////
    // Extra Tool Property //
    ////////////////////////////////////

    public static Material setOverrideToolStats(Material m, String toolId, ExtraToolProperty.Builder overrideBuilder) {
        if (checkFrozen("set overriding tool stats")) return m;
        m.getProperties().ensureSet(PropertyKey.EXTRATOOL);
        ExtraToolProperty prop = m.getProperty(PropertyKey.EXTRATOOL);
        if (prop != null) {
            prop.setOverrideProperty(toolId, overrideBuilder.build());
        } else logError(m, "change tool stats", "Tool");
        return m;
    }

    // Wire/Item Pipe/Fluid Pipe stuff?

    ////////////////////////////////////
    // Blast Property //
    ////////////////////////////////////

    public static Material setBlastTemp(Material m, int blastTemp) {
        if (checkFrozen("set blast temperature")) return m;
        if (blastTemp <= 0) {
            GroovyLog.get().error("Blast Temperature must be greater than zero! Material: " + m.getUnlocalizedName());
            return m;
        }
        BlastProperty prop = m.getProperty(PropertyKey.BLAST);
        if (prop != null) prop.setBlastTemperature(blastTemp);
        else m.setProperty(PropertyKey.BLAST, new BlastProperty(blastTemp));
        return m;
    }

    public static int blastTemp(Material m) {
        BlastProperty prop = m.getProperty(PropertyKey.BLAST);
        if (prop != null) {
            return prop.getBlastTemperature();
        } else logError(m, "get blast temperature", "Blast");
        return 0;
    }

    ////////////////////////////////////
    // Ore Property //
    ////////////////////////////////////

    public static int oreMultiplier(Material m) {
        OreProperty prop = m.getProperty(PropertyKey.ORE);
        if (prop != null) {
            return prop.getOreMultiplier();
        } else logError(m, "get ore multiplier", "Ore");
        return 0;
    }
}
