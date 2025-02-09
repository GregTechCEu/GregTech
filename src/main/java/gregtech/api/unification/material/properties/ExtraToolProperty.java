package gregtech.api.unification.material.properties;

import net.minecraft.enchantment.Enchantment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ExtraToolProperty implements IMaterialProperty {

    /**
     * Special override for certain kinds of tools.
     */
    private final Map<String, OverrideToolProperty> overrideMap;

    public static class OverrideToolProperty extends ToolProperty {

        public OverrideToolProperty() {
            this.setToolSpeed(Float.NaN);
            this.setToolAttackSpeed(Float.NaN);
            this.setToolAttackDamage(Float.NaN);
            this.setToolDurability(-1);
            this.setToolHarvestLevel(-1);
            this.setToolEnchantability(-1);
            this.setDurabilityMultiplier(-1);
        }

        // It does not make much sense to set these overrides:
        public void setShouldIgnoreCraftingTools(boolean ignore) {
            throw new UnsupportedOperationException();
        }

        public void setUnbreakable(boolean isUnbreakable) {
            throw new UnsupportedOperationException();
        }

        public boolean isMagnetic() {
            throw new UnsupportedOperationException();
        }

        private ToolProperty override(@NotNull ToolProperty property) {
            // copy to prevent the previous map is produced
            ToolProperty result = new ToolProperty(property);

            // Set the floating point number fields
            if (!Float.isNaN(this.getToolSpeed()))
                result.setToolSpeed(this.getToolSpeed());
            if (!Float.isNaN(this.getToolAttackSpeed()))
                result.setToolAttackSpeed(this.getToolAttackSpeed());
            if (!Float.isNaN(this.getToolAttackDamage()))
                result.setToolAttackDamage(this.getToolAttackDamage());

            // Set the integer fields
            if (this.getToolDurability() != -1)
                result.setToolDurability(this.getToolDurability());
            if (this.getToolHarvestLevel() != -1)
                result.setToolHarvestLevel(this.getToolHarvestLevel());
            if (this.getToolEnchantability() != -1)
                result.setToolEnchantability(this.getToolEnchantability());
            if (this.getDurabilityMultiplier() != -1)
                result.setDurabilityMultiplier(this.getDurabilityMultiplier());

            // Merge the enchantment map
            result.getEnchantments().putAll(this.getEnchantments());
            return result;
        }
    }

    public ExtraToolProperty() {
        this.overrideMap = new HashMap<>();
    }

    public void setOverrideProperty(String toolId, OverrideToolProperty overrideProperty) {
        this.overrideMap.put(toolId, overrideProperty);
    }

    @Nullable
    public OverrideToolProperty getOverrideProperty(String toolId) {
        return this.overrideMap.get(toolId);
    }

    public boolean hasOverrideProperty(String toolId) {
        return getOverrideProperty(toolId) != null;
    }

    public ToolProperty getOverriddenResult(String toolId, @Nullable MaterialToolProperty materialToolProperty) {
        if (materialToolProperty == null) materialToolProperty = new MaterialToolProperty();
        return overrideMap.getOrDefault(toolId, new OverrideToolProperty())
                .override(materialToolProperty);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        // No check here, since these recipes should be self-generated.
        // If they are overriding ToolProperty, then it is already generated.
    }

    public static class Builder {

        private final OverrideToolProperty toolProperty;

        public static Builder of() {
            return new Builder();
        }

        public static Builder of(int durability) {
            Builder builder = new Builder();
            builder.durability(durability);
            return builder;
        }

        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            Builder builder = new Builder();
            builder.harvestSpeed(harvestSpeed).attackDamage(attackDamage).durability(durability)
                    .harvestLevel(harvestLevel);
            return builder;
        }

        public Builder() {
            this.toolProperty = new OverrideToolProperty();
        }

        public Builder harvestSpeed(float harvestSpeed) {
            toolProperty.setToolSpeed(harvestSpeed);
            return this;
        }

        public Builder attackDamage(float attackDamage) {
            toolProperty.setToolAttackDamage(attackDamage);
            return this;
        }

        public Builder durability(int durability) {
            toolProperty.setToolDurability(durability);
            return this;
        }

        public Builder attackSpeed(float attackSpeed) {
            toolProperty.setToolAttackSpeed(attackSpeed);
            return this;
        }

        public Builder harvestLevel(int harvestLevel) {
            toolProperty.setToolHarvestLevel(harvestLevel);
            return this;
        }

        public Builder enchantment(Enchantment enchantment, int level) {
            toolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        public Builder durabilityMultiplier(int multiplier) {
            toolProperty.setDurabilityMultiplier(multiplier);
            return this;
        }

        public OverrideToolProperty build() {
            return this.toolProperty;
        }
    }
}
