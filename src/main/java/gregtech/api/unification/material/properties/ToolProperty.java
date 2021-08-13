package gregtech.api.unification.material.properties;

import crafttweaker.api.enchantments.IEnchantment;
import gregtech.api.GTValues;
import gregtech.api.enchants.EnchantmentData;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayList;
import java.util.List;

public class ToolProperty implements IMaterialProperty<ToolProperty> {

    /**
     * Speed of tools made from this Material.
     * <p>
     * Default:
     */
    //@ZenProperty
    public final float toolSpeed;

    /**
     * Attack damage of tools made from this Material
     * <p>
     * Default:
     */
    //@ZenProperty
    public final float toolAttackDamage;

    /**
     * Durability of tools made from this Material.
     * <p>
     * Default:
     */
    //@ZenProperty
    public final int toolDurability;

    /**
     * Enchantability of tools made from this Material.
     * <p>
     * Default:
     */
    //@ZenProperty
    public final int toolEnchantability;

    /**
     * Enchantment to be applied to tools made from this Material.
     * <p>
     * Default: none.
     */
    //@ZenProperty
    public final List<EnchantmentData> toolEnchantments = new ArrayList<>();

    public ToolProperty(float toolSpeed, float toolAttackDamage, int toolDurability, int toolEnchantability) {
        this.toolSpeed = toolSpeed;
        this.toolAttackDamage = toolAttackDamage;
        this.toolDurability = toolDurability;
        this.toolEnchantability = toolEnchantability;
    }

    /**
     * Default values constructor.
     */
    public ToolProperty() {
        this(1.0f, 1.0f, 100, 10);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.GEM)) properties.ensureSet(PropertyKey.INGOT, true);
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
