package gregtech.api.items.toolitem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ToolStatsBuilder {

    private BiConsumer<ItemStack, EntityPlayer> onToolCrafted;
    private int damagePerBlockBreak = 0;
    private int damagePerCraft = 0;
    private int damagePerAttack = 0;
    private int baseQuality = 0;
    private float attackDamage = 0F;
    private float efficiency = 4F;
    private BiPredicate<ItemStack, Enchantment> canApplyEnchantment;
    private float attackSpeed = 0F;

    public ToolStatsBuilder onToolCrafted(BiConsumer<ItemStack, EntityPlayer> onToolCrafted) {
        this.onToolCrafted = onToolCrafted;
        return this;
    }

    public ToolStatsBuilder damagePerBlockBreak(int damagePerBlockBreak) {
        this.damagePerBlockBreak = damagePerBlockBreak;
        return this;
    }

    public ToolStatsBuilder damagePerBlockBreak() {
        damagePerBlockBreak(1);
        return this;
    }

    public ToolStatsBuilder damagePerCraft(int damagePerCraft) {
        this.damagePerCraft = damagePerCraft;
        return this;
    }

    public ToolStatsBuilder damagePerCraft() {
        damagePerCraft(1);
        return this;
    }

    public ToolStatsBuilder damagePerAttack(int damagePerAttack) {
        this.damagePerAttack = damagePerAttack;
        return this;
    }

    public ToolStatsBuilder usedForAttacking() {
        damagePerAttack(1);
        return this;
    }

    public ToolStatsBuilder baseQuality(int baseQuality) {
        this.baseQuality = baseQuality;
        return this;
    }

    public ToolStatsBuilder baseQuality() {
        baseQuality(0);
        return this;
    }

    public ToolStatsBuilder attackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
        return this;
    }

    public ToolStatsBuilder efficiency(float efficiency) {
        this.efficiency = efficiency;
        return this;
    }

    public ToolStatsBuilder canApplyEnchantment(BiPredicate<ItemStack, Enchantment> canApplyEnchantment) {
        this.canApplyEnchantment = canApplyEnchantment;
        return this;
    }

    public ToolStatsBuilder canApplyEnchantment(EnumEnchantmentType... enchantmentTypes) {
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

    public ToolStatsBuilder attackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
        return this;
    }

    public IToolStats build() {
        return new IToolStats() {

            private final BiConsumer<ItemStack, EntityPlayer> onToolCrafted = ToolStatsBuilder.this.onToolCrafted;
            private int damagePerBlockBreak = ToolStatsBuilder.this.damagePerBlockBreak;
            private int damagePerCraft = ToolStatsBuilder.this.damagePerCraft;
            private int damagePerAttack = ToolStatsBuilder.this.damagePerAttack;
            private int baseQuality = ToolStatsBuilder.this.baseQuality;
            private float attackDamage = ToolStatsBuilder.this.attackDamage;
            private float efficiency = ToolStatsBuilder.this.efficiency;
            private BiPredicate<ItemStack, Enchantment> canApplyEnchantment = ToolStatsBuilder.this.canApplyEnchantment;
            private float attackSpeed = ToolStatsBuilder.this.attackSpeed;

            @Override
            public void onToolCrafted(ItemStack stack, EntityPlayer player) {
                if (onToolCrafted == null) {
                    onToolCrafted.accept(stack, player);
                }
            }

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
            public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
                return canApplyEnchantment.test(stack, enchantment);
            }

            @Override
            @Deprecated
            public boolean canMineBlock(IBlockState block, ItemStack stack) {
                return true;
            }

            @Override
            public float getAttackSpeed(ItemStack stack) {
                return attackSpeed;
            }

        };
    }

}
