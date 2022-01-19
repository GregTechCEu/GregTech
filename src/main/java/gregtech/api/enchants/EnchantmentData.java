package gregtech.api.enchants;

import crafttweaker.api.enchantments.IEnchantmentDefinition;
import crafttweaker.mc1120.enchantments.MCEnchantmentDefinition;
import gregtech.api.GTValues;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.Optional.Method;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenClass("mods.gregtech.EnchantmentData")
public class EnchantmentData {

    public final Enchantment enchantment;
    public final int level;

    public EnchantmentData(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    @ZenGetter("level")
    public Enchantment getEnchantment() {
        return enchantment;
    }

    @ZenGetter("enchantment")
    @Method(modid = GTValues.MODID_CT)
    @SuppressWarnings("unused")
    public IEnchantmentDefinition ctGetEnchantment() {
        return new MCEnchantmentDefinition(enchantment);
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnchantmentData that = (EnchantmentData) o;

        if (level != that.level) return false;
        return enchantment.equals(that.enchantment);
    }

    @Override
    public int hashCode() {
        int result = enchantment.hashCode();
        result = 31 * result + level;
        return result;
    }

}
