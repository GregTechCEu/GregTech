package gregtech.api.items.toolitem;

import com.google.common.collect.ImmutableList;
import gregtech.api.items.toolitem.aoe.AoEChained;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behaviour.IToolBehaviour;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ToolDefinitionBuilder {

    private final List<IToolBehaviour> behaviours = new ArrayList<>();
    private int damagePerAction = 1;
    private boolean suitableForBlockBreaking = false;
    private boolean suitableForAttacking = false;
    private boolean suitableForCrafting = false;
    private int baseDurability = 0;
    private int baseQuality = 0;
    private float attackDamage = 0F;
    private float baseEfficiency = 4F;
    private float efficiencyMultiplier = 1.0F;
    private boolean isEnchantable;
    private BiPredicate<ItemStack, Enchantment> canApplyEnchantment;
    private float attackSpeed = 0F;
    private boolean sneakBypassUse = false;
    private Supplier<ItemStack> brokenStack = () -> ItemStack.EMPTY;
    private AoESymmetrical aoeSymmetrical = AoESymmetrical.none();
    private AoEChained aoeChained = AoEChained.none();
    private final Set<Block> effectiveBlocks = new ObjectOpenHashSet<>();
    private final Set<Material> effectiveMaterials = new ObjectOpenHashSet<>();
    private Predicate<IBlockState> effectiveStates;

    public ToolDefinitionBuilder behaviours(IToolBehaviour... behaviours) {
        Collections.addAll(this.behaviours, behaviours);
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

    public ToolDefinitionBuilder baseEfficiency(float baseEfficiency) {
        this.baseEfficiency = baseEfficiency;
        return this;
    }

    public ToolDefinitionBuilder efficiencyMultiplier(float efficiencyMultiplier) {
        this.efficiencyMultiplier = efficiencyMultiplier;
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

    public ToolDefinitionBuilder aoeSymmetrical(AoESymmetrical aoeSymmetrical) {
        this.aoeSymmetrical = aoeSymmetrical;
        return this;
    }

    public ToolDefinitionBuilder aoeSymmetrical(int column, int row, int layer) {
        return aoeSymmetrical(AoESymmetrical.of(column, row, layer));
    }

    public ToolDefinitionBuilder aoeChained(AoEChained aoeChained) {
        this.aoeChained = aoeChained;
        return this;
    }

    public ToolDefinitionBuilder aoeChained(int limit) {
        return aoeChained(AoEChained.of(limit));
    }

    public ToolDefinitionBuilder effectiveBlocks(Block... blocks) {
        Collections.addAll(this.effectiveBlocks, blocks);
        return this;
    }

    public ToolDefinitionBuilder effectiveMaterials(Material... materials) {
        Collections.addAll(this.effectiveMaterials, materials);
        return this;
    }

    public ToolDefinitionBuilder effectiveStates(Predicate<IBlockState> effectiveStates) {
        this.effectiveStates = effectiveStates;
        return this;
    }

    public IGTToolDefinition build() {
        return new IGTToolDefinition() {

            private final List<IToolBehaviour> behaviours = ImmutableList.copyOf(ToolDefinitionBuilder.this.behaviours);
            private final int damagePerAction = ToolDefinitionBuilder.this.damagePerAction;
            private final boolean suitableForBlockBreaking = ToolDefinitionBuilder.this.suitableForBlockBreaking;
            private final boolean suitableForAttacking = ToolDefinitionBuilder.this.suitableForAttacking;
            private final boolean suitableForCrafting = ToolDefinitionBuilder.this.suitableForCrafting;
            private final int baseDurability = ToolDefinitionBuilder.this.baseDurability;
            private final int baseQuality = ToolDefinitionBuilder.this.baseQuality;
            private final float attackDamage = ToolDefinitionBuilder.this.attackDamage;
            private final float baseEfficiency = ToolDefinitionBuilder.this.baseEfficiency;
            private final float efficiencyMultiplier = ToolDefinitionBuilder.this.efficiencyMultiplier;
            private final boolean isEnchantable = ToolDefinitionBuilder.this.isEnchantable;
            private final BiPredicate<ItemStack, Enchantment> canApplyEnchantment = ToolDefinitionBuilder.this.canApplyEnchantment;
            private final float attackSpeed = ToolDefinitionBuilder.this.attackSpeed;
            private final boolean sneakBypassUse = ToolDefinitionBuilder.this.sneakBypassUse;
            private final Supplier<ItemStack> brokenStack = ToolDefinitionBuilder.this.brokenStack;
            private final AoESymmetrical aoeSymmetrical = ToolDefinitionBuilder.this.aoeSymmetrical;
            private final AoEChained aoeChained = ToolDefinitionBuilder.this.aoeChained;
            private final Predicate<IBlockState> effectiveStatePredicate;

            {
                Set<Block> effectiveBlocks = ToolDefinitionBuilder.this.effectiveBlocks;
                Set<Material> effectiveMaterials = ToolDefinitionBuilder.this.effectiveMaterials;
                Predicate<IBlockState> effectiveStates = ToolDefinitionBuilder.this.effectiveStates;
                Predicate<IBlockState> effectiveStatePredicate = null;
                if (!effectiveBlocks.isEmpty()) {
                    effectiveStatePredicate = state -> effectiveBlocks.contains(state.getBlock());
                }
                if (!effectiveMaterials.isEmpty()) {
                    effectiveStatePredicate = effectiveStatePredicate == null ?
                            state -> effectiveMaterials.contains(state.getMaterial()) :
                            effectiveStatePredicate.or(state -> effectiveMaterials.contains(state.getMaterial()));
                }
                if (effectiveStates != null) {
                    effectiveStatePredicate = effectiveStatePredicate == null ? effectiveStates : effectiveStatePredicate.or(effectiveStates);
                }
                this.effectiveStatePredicate = effectiveStatePredicate == null ? state -> false : effectiveStatePredicate;
            }

            @Override
            public List<IToolBehaviour> getBehaviours() {
                return behaviours;
            }

            @Override
            public boolean isToolEffective(IBlockState state) {
                return effectiveStatePredicate.test(state);
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
                return baseEfficiency;
            }

            @Override
            public float getEfficiencyMultiplier(ItemStack stack) {
                return efficiencyMultiplier;
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

            @Override
            public AoESymmetrical getAoEDefinition(ItemStack stack) {
                return aoeSymmetrical;
            }

            @Override
            public AoEChained getAoEChainedDefinition(ItemStack stack) {
                return aoeChained;
            }
        };
    }

}
