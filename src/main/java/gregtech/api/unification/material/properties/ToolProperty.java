package gregtech.api.unification.material.properties;

import crafttweaker.api.enchantments.IEnchantment;
import gregtech.api.GTValues;
import gregtech.api.enchants.EnchantmentData;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayList;
import java.util.List;

public class ToolProperty implements IMaterialProperty {

    /**
     * Speed of tools made from this material
     */
    //@ZenProperty
    public final float toolSpeed;

    /**
     * Attack damage of tools made from this material
     * Usually equal to material's harvest level
     */
    //@ZenProperty
    public final float toolAttackDamage;

    /**
     * Durability of tools made from this material
     */
    //@ZenProperty
    public final int toolDurability;

    /**
     * Enchantment to be applied to tools made from this material
     */
    //@ZenProperty
    public final List<EnchantmentData> toolEnchantments = new ArrayList<>();

    public ToolProperty(float toolSpeed, float toolAttackDamage, int toolDurability) {
        this.toolSpeed = toolSpeed;
        this.toolAttackDamage = toolAttackDamage;
        this.toolDurability = toolDurability;
    }

    @Override
    public void verifyProperty(Properties properties) {
        if (properties.getIngotProperty() == null && properties.getGemProperty() == null) {
            properties.setIngotProperty(new IngotProperty()); // default to Ingot if not specified
            properties.verify();
        }
    }

    public void addEnchantmentForTools(Enchantment enchantment, int level) {
        toolEnchantments.add(new EnchantmentData(enchantment, level));
    }

    //@ZenMethod("addToolEnchantment")
    @Optional.Method(modid = GTValues.MODID_CT)
    public void ctAddEnchantmentForTools(IEnchantment enchantment) {
        Enchantment enchantmentType = (Enchantment) enchantment.getDefinition().getInternal();
        toolEnchantments.add(new EnchantmentData(enchantmentType, enchantment.getLevel()));
    }
}
