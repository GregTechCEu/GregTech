package gregtech.api.unification.material.properties;

import net.minecraft.enchantment.Enchantment;

import org.jetbrains.annotations.Nullable;

import javax.tools.Tool;
import java.util.HashMap;
import java.util.Map;

public class ExtraToolProperty implements IMaterialProperty {

    /**
     * Special override for certain kinds of tools.
     */
    private final Map<String, OverrideToolProperty> overrideMap;

    public static class OverrideToolProperty extends SimpleToolProperty {

        public OverrideToolProperty() {
            this.setToolSpeed(Float.NaN);
            this.setToolAttackSpeed(Float.NaN);
            this.setToolAttackDamage(Float.NaN);
            this.setToolDurability(-1);
            super.setToolHarvestLevel(-1);
            this.setToolEnchantability(-1);
            this.setDurabilityMultiplier(-1);
        }

        // It does not make much sense to set these overrides:
        public void setToolHarvestLevel(int harvestLevel) {
            throw new UnsupportedOperationException();
        }

        public void setShouldIgnoreCraftingTools(boolean ignore) {
            throw new UnsupportedOperationException();
        }

        public void setUnbreakable(boolean isUnbreakable) {
            throw new UnsupportedOperationException();
        }

        public boolean isMagnetic() {
            throw new UnsupportedOperationException();
        }

        public SimpleToolProperty override(SimpleToolProperty property) {
            // Set the floating point number fields
            if (!Float.isNaN(this.getToolSpeed()))
                property.setToolSpeed(this.getToolSpeed());
            if (!Float.isNaN(this.getToolAttackSpeed()))
                property.setToolAttackSpeed(this.getToolAttackSpeed());
            if (!Float.isNaN(this.getToolAttackDamage()))
                property.setToolAttackDamage(this.getToolAttackDamage());

            // Set the integer fields
            if (this.getToolDurability() != -1)
                property.setToolDurability(this.getToolDurability());
            if (this.getToolEnchantability() != -1)
                property.setToolEnchantability(this.getToolEnchantability());
            if (this.getDurabilityMultiplier() != -1)
                property.setDurabilityMultiplier(this.getDurabilityMultiplier());

            // Merge the enchantment map
            property.getEnchantments().putAll(this.getEnchantments());
            return property;
        }
    }

    public ExtraToolProperty() {
        this.overrideMap = new HashMap<>();
    }

    public void setOverrideProperty(String toolClass, OverrideToolProperty overrideProperty) {
        this.overrideMap.put(toolClass, overrideProperty);
    }

    @Nullable
    public OverrideToolProperty getOverrideProperty(String toolClass) {
        return this.overrideMap.get(toolClass);
    }

    public boolean hasOverrideProperty(String toolClass) {
        return getOverrideProperty(toolClass) != null;
    }

    public SimpleToolProperty getOverriddenResult(ToolProperty toolProperty, String toolClass){
        return overrideMap.getOrDefault(toolClass, new OverrideToolProperty()).override((SimpleToolProperty) toolProperty);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        // No check here, since these recipes should be self-generated.
        // If they are overriding ToolProperty, then it is already generated.
    }

    public static class Overrider {

        private final OverrideToolProperty toolProperty;

        public static Overrider of() {
            return new Overrider();
        }

        public static Overrider of(float harvestSpeed, float attackDamage, int durability) {
            Overrider overrider = new Overrider();
            overrider.harvestSpeed(harvestSpeed).attackDamage(attackDamage).durability(durability);
            return overrider;
        }

        public Overrider() {
            this.toolProperty = new OverrideToolProperty();
        }

        public Overrider harvestSpeed(float harvestSpeed) {
            toolProperty.setToolSpeed(harvestSpeed);
            return this;
        }

        public Overrider attackDamage(float attackDamage) {
            toolProperty.setToolAttackDamage(attackDamage);
            return this;
        }

        public Overrider durability(int durability) {
            toolProperty.setToolDurability(durability);
            return this;
        }

        public Overrider attackSpeed(float attackSpeed) {
            toolProperty.setToolAttackSpeed(attackSpeed);
            return this;
        }

        public Overrider enchantment(Enchantment enchantment, int level) {
            toolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        public Overrider durabilityMultiplier(int multiplier) {
            toolProperty.setDurabilityMultiplier(multiplier);
            return this;
        }
    }
}
