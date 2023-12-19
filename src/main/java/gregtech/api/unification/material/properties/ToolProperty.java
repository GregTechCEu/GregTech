package gregtech.api.unification.material.properties;

import gregtech.api.items.toolitem.EnchantmentLevel;

import net.minecraft.enchantment.Enchantment;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

public class ToolProperty implements IMaterialProperty {

    /**
     * Harvest speed of tools made from this Material.
     * <p>
     * Default: 1.0F
     */
    private float harvestSpeed;

    /**
     * Attack damage of tools made from this Material
     * <p>
     * Default: 1.0F
     */
    private float attackDamage;

    /**
     * Attack speed of tools made from this Material
     * <p>
     * Default: 0.0F
     */
    private float attackSpeed;

    /**
     * Durability of tools made from this Material.
     * <p>
     * Default: 100
     */
    private int durability;

    /**
     * Harvest level of tools made of this Material.
     * <p>
     * Default: 2 (Iron).
     */
    private int harvestLevel;

    /**
     * Enchantability of tools made from this Material.
     * <p>
     * Default: 10
     */
    private int enchantability = 10;

    /**
     * If crafting tools should not be made from this material
     */
    private boolean ignoreCraftingTools;

    /**
     * If tools made of this material should be unbreakable and ignore durability checks.
     */
    private boolean isUnbreakable;

    /**
     * If tools made of this material should be "magnetic," meaning items go
     * directly into the player's inventory instead of dropping on the ground.
     */
    private boolean isMagnetic;

    /**
     * A multiplier to the base durability for this material
     * Mostly for modpack makers
     */
    private int durabilityMultiplier = 1;

    /**
     * Enchantment to be applied to tools made from this Material.
     */
    private final Object2ObjectMap<Enchantment, EnchantmentLevel> enchantments = new Object2ObjectArrayMap<>();

    public ToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
        this.harvestSpeed = harvestSpeed;
        this.attackDamage = attackDamage;
        this.durability = durability;
        this.harvestLevel = harvestLevel;
    }

    public ToolProperty() {
        this(1.0F, 1.0F, 100, 2);
    }

    public float getToolSpeed() {
        return harvestSpeed;
    }

    public void setToolSpeed(float toolSpeed) {
        this.harvestSpeed = toolSpeed;
    }

    public float getToolAttackDamage() {
        return attackDamage;
    }

    public void setToolAttackDamage(float toolAttackDamage) {
        this.attackDamage = toolAttackDamage;
    }

    public float getToolAttackSpeed() {
        return attackSpeed;
    }

    public void setToolAttackSpeed(float toolAttackSpeed) {
        this.attackSpeed = toolAttackSpeed;
    }

    public int getToolDurability() {
        return durability;
    }

    public void setToolDurability(int toolDurability) {
        this.durability = toolDurability;
    }

    public int getToolHarvestLevel() {
        return this.harvestLevel;
    }

    public void setToolHarvestLevel(int toolHarvestLevel) {
        this.harvestLevel = toolHarvestLevel;
    }

    public int getToolEnchantability() {
        return enchantability;
    }

    public void setToolEnchantability(int toolEnchantability) {
        this.enchantability = toolEnchantability;
    }

    public boolean getShouldIgnoreCraftingTools() {
        return ignoreCraftingTools;
    }

    public void setShouldIgnoreCraftingTools(boolean ignore) {
        this.ignoreCraftingTools = ignore;
    }

    public boolean getUnbreakable() {
        return isUnbreakable;
    }

    public void setUnbreakable(boolean isUnbreakable) {
        this.isUnbreakable = isUnbreakable;
    }

    public Object2ObjectMap<Enchantment, EnchantmentLevel> getEnchantments() {
        return enchantments;
    }

    public void setMagnetic(boolean isMagnetic) {
        this.isMagnetic = isMagnetic;
    }

    public boolean isMagnetic() {
        return isMagnetic;
    }

    public void setDurabilityMultiplier(int multiplier) {
        this.durabilityMultiplier = multiplier;
    }

    public int getDurabilityMultiplier() {
        return durabilityMultiplier;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.GEM)) properties.ensureSet(PropertyKey.INGOT, true);
    }

    public void addEnchantmentForTools(Enchantment enchantment, int level) {
        this.addEnchantmentForTools(enchantment, level, 0);
    }

    public void addEnchantmentForTools(Enchantment enchantment, double level, double levelGrowth) {
        enchantments.put(enchantment, new EnchantmentLevel(level, levelGrowth));
    }

    public static class Builder {

        private final ToolProperty toolProperty;

        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel);
        }

        private Builder(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            toolProperty = new ToolProperty(harvestSpeed, attackDamage, durability, harvestLevel);
        }

        public Builder enchantability(int enchantability) {
            toolProperty.setToolEnchantability(enchantability);
            return this;
        }

        public Builder attackSpeed(float attackSpeed) {
            toolProperty.setToolAttackSpeed(attackSpeed);
            return this;
        }

        public Builder ignoreCraftingTools() {
            toolProperty.setShouldIgnoreCraftingTools(true);
            return this;
        }

        public Builder unbreakable() {
            toolProperty.setUnbreakable(true);
            return this;
        }

        public Builder enchantment(Enchantment enchantment, int level) {
            toolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        public Builder magnetic() {
            toolProperty.setMagnetic(true);
            return this;
        }

        public Builder durabilityMultiplier(int multiplier) {
            toolProperty.setDurabilityMultiplier(multiplier);
            return this;
        }

        public ToolProperty build() {
            return toolProperty;
        }
    }
}
