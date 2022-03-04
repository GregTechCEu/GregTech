package gregtech.api.items.toolitem;

import com.google.common.collect.ImmutableList;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class ToolDefinitionBuilder {

    private List<IItemComponent> components = new ArrayList<>();
    private int damagePerAction = 1;
    private boolean suitableForBlockBreaking = false;
    private boolean suitableForAttacking = false;
    private boolean suitableForCrafting = false;
    private int baseDurability = 0;
    private int baseQuality = 0;
    private float attackDamage = 0F;
    private float efficiency = 4F;
    private boolean isEnchantable;
    private BiPredicate<ItemStack, Enchantment> canApplyEnchantment;
    private float attackSpeed = 0F;
    private boolean sneakBypassUse = false;
    private Supplier<ItemStack> brokenStack = () -> ItemStack.EMPTY;

    public ToolDefinitionBuilder component(IItemComponent... components) {
        Collections.addAll(this.components, components);
        return this;
    }

    public ToolDefinitionBuilder damagePerAction(int damagePerAction) {
        this.damagePerAction = damagePerAction;
        return this;
    }

    public ToolDefinitionBuilder suitableForBlockBreaking() {
        this.suitableForBlockBreaking = true;
        return this;
    }

    public ToolDefinitionBuilder suitableForAttacking() {
        this.suitableForAttacking = true;
        return this;
    }

    public ToolDefinitionBuilder suitableForCrafting() {
        this.suitableForCrafting = true;
        return this;
    }

    public ToolDefinitionBuilder baseDurability(int baseDurability) {
        this.baseDurability = baseDurability;
        return this;
    }

    public ToolDefinitionBuilder baseQuality(int baseQuality) {
        this.baseQuality = baseQuality;
        return this;
    }

    public ToolDefinitionBuilder baseQuality() {
        baseQuality(0);
        return this;
    }

    public ToolDefinitionBuilder attackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
        return this;
    }

    public ToolDefinitionBuilder efficiency(float efficiency) {
        this.efficiency = efficiency;
        return this;
    }

    public ToolDefinitionBuilder noEnchant() {
        this.isEnchantable = false;
        return this;
    }

    public ToolDefinitionBuilder canApplyEnchantment(BiPredicate<ItemStack, Enchantment> canApplyEnchantment) {
        this.canApplyEnchantment = canApplyEnchantment;
        return this;
    }

    public ToolDefinitionBuilder canApplyEnchantment(EnumEnchantmentType... enchantmentTypes) {
        this.canApplyEnchantment = (stack, enchantment) -> {
            for (EnumEnchantmentType type : enchantmentTypes) {
                if (type == enchantment.type) {
                    return true;
                }
            }
            return false;
        };
        return this;
    }

    public ToolDefinitionBuilder attackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
        return this;
    }

    public ToolDefinitionBuilder sneakBypassUse() {
        this.sneakBypassUse = true;
        return this;
    }

    public ToolDefinitionBuilder brokenStack(Supplier<ItemStack> brokenStack) {
        this.brokenStack = brokenStack;
        return this;
    }

    public IGTToolDefinition build() {
        return new IGTToolDefinition() {

            private final List<IItemComponent> components = ImmutableList.copyOf(ToolDefinitionBuilder.this.components);
            private final int damagePerAction = ToolDefinitionBuilder.this.damagePerAction;
            private final boolean suitableForBlockBreaking = ToolDefinitionBuilder.this.suitableForBlockBreaking;
            private final boolean suitableForAttacking = ToolDefinitionBuilder.this.suitableForAttacking;
            private final boolean suitableForCrafting = ToolDefinitionBuilder.this.suitableForCrafting;
            private final int baseDurability = ToolDefinitionBuilder.this.baseDurability;
            private final int baseQuality = ToolDefinitionBuilder.this.baseQuality;
            private final float attackDamage = ToolDefinitionBuilder.this.attackDamage;
            private final float efficiency = ToolDefinitionBuilder.this.efficiency;
            private final boolean isEnchantable = ToolDefinitionBuilder.this.isEnchantable;
            private final BiPredicate<ItemStack, Enchantment> canApplyEnchantment = ToolDefinitionBuilder.this.canApplyEnchantment;
            private final float attackSpeed = ToolDefinitionBuilder.this.attackSpeed;
            private final boolean sneakBypassUse = ToolDefinitionBuilder.this.sneakBypassUse;
            private final Supplier<ItemStack> brokenStack = ToolDefinitionBuilder.this.brokenStack;

            @Override
            public List<IItemComponent> getComponents() {
                return components;
            }

            @Override
            public int getDamagePerAction(ItemStack stack) {
                return damagePerAction;
            }

            @Override
            public boolean isSuitableForBlockBreak(ItemStack stack) {
                return suitableForBlockBreaking;
            }

            @Override
            public boolean isSuitableForAttacking(ItemStack stack) {
                return suitableForAttacking;
            }

            @Override
            public boolean isSuitableForCrafting(ItemStack stack) {
                return suitableForCrafting;
            }

            @Override
            public int getBaseDurability(ItemStack stack) {
                return baseDurability;
            }

            @Override
            public int getBaseQuality(ItemStack stack) {
                return baseQuality;
            }

            @Override
            public float getBaseDamage(ItemStack stack) {
                return attackDamage;
            }

            @Override
            public float getBaseEfficiency(ItemStack stack) {
                return efficiency;
            }

            @Override
            public boolean isEnchantable(ItemStack stack) {
                return isEnchantable;
            }

            @Override
            public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
                return canApplyEnchantment.test(stack, enchantment);
            }

            @Override
            public float getAttackSpeed(ItemStack stack) {
                return attackSpeed;
            }

            @Override
            public boolean doesSneakBypassUse() {
                return sneakBypassUse;
            }

            @Override
            public ItemStack getBrokenStack() {
                return brokenStack.get();
            }
        };
    }

}
