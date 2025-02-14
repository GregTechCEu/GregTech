package gregtech.api.unification.material.properties;

import net.minecraft.enchantment.Enchantment;

public class MaterialToolProperty extends ToolProperty implements IMaterialProperty {

    public MaterialToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
        super(harvestSpeed, attackDamage, durability, harvestLevel);
        this.setToolEnchantability(10);
        this.setDurabilityMultiplier(1);
    }

    public MaterialToolProperty() {
        this(1.0F, 1.0F, 100, 2);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.GEM)) properties.ensureSet(PropertyKey.INGOT, true);
    }

    public static class Builder {

        private final MaterialToolProperty materialToolProperty;

        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel);
        }

        private Builder(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            materialToolProperty = new MaterialToolProperty(harvestSpeed, attackDamage, durability, harvestLevel);
        }

        public Builder enchantability(int enchantability) {
            materialToolProperty.setToolEnchantability(enchantability);
            return this;
        }

        public Builder attackSpeed(float attackSpeed) {
            materialToolProperty.setToolAttackSpeed(attackSpeed);
            return this;
        }

        public Builder ignoreCraftingTools() {
            materialToolProperty.setShouldIgnoreCraftingTools(true);
            return this;
        }

        public Builder unbreakable() {
            materialToolProperty.setUnbreakable(true);
            return this;
        }

        public Builder enchantment(Enchantment enchantment, int level) {
            materialToolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        public Builder magnetic() {
            materialToolProperty.setMagnetic(true);
            return this;
        }

        public Builder durabilityMultiplier(int multiplier) {
            materialToolProperty.setDurabilityMultiplier(multiplier);
            return this;
        }

        public MaterialToolProperty build() {
            return materialToolProperty;
        }
    }
}
