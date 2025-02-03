package gregtech.api.unification.material.properties;

import net.minecraft.enchantment.Enchantment;

public class ToolProperty extends SimpleToolProperty implements IMaterialProperty {

    public ToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
        super(harvestSpeed, attackDamage, durability, harvestLevel);
        this.setToolEnchantability(10);
        this.setDurabilityMultiplier(1);
    }

    public ToolProperty() {
        this(1.0F, 1.0F, 100, 2);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.GEM)) properties.ensureSet(PropertyKey.INGOT, true);
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
