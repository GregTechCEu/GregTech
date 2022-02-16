package gregtech.api.items.toolitem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class TooDefinitionBuilder {

    private int damagePerBlockBreak = 2;
    private int damagePerCraft = 1;
    private int damagePerAttack = 2;
    private int baseQuality = 0;
    private float attackDamage = 0F;
    private float efficiency = 4F;
    private boolean isEnchantable;
    private BiPredicate<ItemStack, Enchantment> canApplyEnchantment;
    private float attackSpeed = 0F;
    private boolean sneakBypassUse = false;
    private Supplier<ItemStack> brokenStack = () -> ItemStack.EMPTY;

    public TooDefinitionBuilder damagePerBlockBreak(int damagePerBlockBreak) {
        this.damagePerBlockBreak = damagePerBlockBreak;
        return this;
    }

    public TooDefinitionBuilder damagePerBlockBreak() {
        damagePerBlockBreak(1);
        return this;
    }

    public TooDefinitionBuilder damagePerCraft(int damagePerCraft) {
        this.damagePerCraft = damagePerCraft;
        return this;
    }

    public TooDefinitionBuilder damagePerCraft() {
        damagePerCraft(1);
        return this;
    }

    public TooDefinitionBuilder damagePerAttack(int damagePerAttack) {
        this.damagePerAttack = damagePerAttack;
        return this;
    }

    public TooDefinitionBuilder usedForAttacking() {
        damagePerAttack(1);
        return this;
    }

    public TooDefinitionBuilder baseQuality(int baseQuality) {
        this.baseQuality = baseQuality;
        return this;
    }

    public TooDefinitionBuilder baseQuality() {
        baseQuality(0);
        return this;
    }

    public TooDefinitionBuilder attackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
        return this;
    }

    public TooDefinitionBuilder efficiency(float efficiency) {
        this.efficiency = efficiency;
        return this;
    }

    public TooDefinitionBuilder noEnchant() {
        this.isEnchantable = false;
        return this;
    }

    public TooDefinitionBuilder canApplyEnchantment(BiPredicate<ItemStack, Enchantment> canApplyEnchantment) {
        this.canApplyEnchantment = canApplyEnchantment;
        return this;
    }

    public TooDefinitionBuilder canApplyEnchantment(EnumEnchantmentType... enchantmentTypes) {
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

    public TooDefinitionBuilder attackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
        return this;
    }

    public TooDefinitionBuilder sneakBypassUse() {
        this.sneakBypassUse = true;
        return this;
    }

    public TooDefinitionBuilder brokenStack(Supplier<ItemStack> brokenStack) {
        this.brokenStack = brokenStack;
        return this;
    }

    public IGTToolDefinition build() {
        return new IGTToolDefinition() {

            private final int damagePerBlockBreak = TooDefinitionBuilder.this.damagePerBlockBreak;
            private final int damagePerCraft = TooDefinitionBuilder.this.damagePerCraft;
            private final int damagePerAttack = TooDefinitionBuilder.this.damagePerAttack;
            private final int baseQuality = TooDefinitionBuilder.this.baseQuality;
            private final float attackDamage = TooDefinitionBuilder.this.attackDamage;
            private final float efficiency = TooDefinitionBuilder.this.efficiency;
            private final boolean isEnchantable = TooDefinitionBuilder.this.isEnchantable;
            private final BiPredicate<ItemStack, Enchantment> canApplyEnchantment = TooDefinitionBuilder.this.canApplyEnchantment;
            private final float attackSpeed = TooDefinitionBuilder.this.attackSpeed;
            private final boolean sneakBypassUse = TooDefinitionBuilder.this.sneakBypassUse;
            private final Supplier<ItemStack> brokenStack = TooDefinitionBuilder.this.brokenStack;

            @Override
            public int getToolDamagePerBlockBreak(ItemStack stack) {
                return damagePerBlockBreak;
            }

            @Override
            public int getToolDamagePerContainerCraft(ItemStack stack) {
                return damagePerCraft;
            }

            @Override
            public int getToolDamagePerEntityAttack(ItemStack stack) {
                return damagePerAttack;
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
