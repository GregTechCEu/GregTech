package gregtech.api.items.toolitem;

import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ToolDefinitionBuilder {

    private final List<IToolBehavior> behaviours = new ArrayList<>();
    private int damagePerAction = 1;
    private int damagePerCraftingAction = 1;
    private boolean suitableForBlockBreaking = false;
    private boolean suitableForAttacking = false;
    private boolean suitableForCrafting = false;
    private int baseDurability = 0;
    private float durabilityMultiplier = 1.0f;
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
    private final Set<Block> effectiveBlocks = new ObjectOpenHashSet<>();
    private final Set<Material> effectiveMaterials = new ObjectOpenHashSet<>();
    private Predicate<IBlockState> effectiveStates;
    private Object2ObjectMap<Enchantment, EnchantmentLevel> defaultEnchantments = new Object2ObjectArrayMap<>();

    public ToolDefinitionBuilder behaviors(IToolBehavior... behaviours) {
        Collections.addAll(this.behaviours, behaviours);
        return this;
    }

    public ToolDefinitionBuilder damagePerAction(int damagePerAction) {
        this.damagePerAction = damagePerAction;
        return this;
    }

    public ToolDefinitionBuilder damagePerCraftingAction(int damagePerCraftingAction) {
        this.damagePerCraftingAction = damagePerCraftingAction;
        return this;
    }

    public ToolDefinitionBuilder blockBreaking() {
        this.suitableForBlockBreaking = true;
        return this;
    }

    public ToolDefinitionBuilder attacking() {
        this.suitableForAttacking = true;
        return this;
    }

    public ToolDefinitionBuilder crafting() {
        this.suitableForCrafting = true;
        return this;
    }

    public ToolDefinitionBuilder baseDurability(int baseDurability) {
        this.baseDurability = baseDurability;
        return this;
    }

    public ToolDefinitionBuilder durabilityMultiplier(float multiplier) {
        this.durabilityMultiplier = multiplier;
        return this;
    }

    public ToolDefinitionBuilder baseQuality(int baseQuality) {
        this.baseQuality = baseQuality;
        return this;
    }

    public ToolDefinitionBuilder baseQuality() {
        return baseQuality(0);
    }

    public ToolDefinitionBuilder attackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
        return this;
    }

    /**
     * Sets the attack to the lowest possible value.
     * Attack in-game will always result in 0 no matter the
     * material stats, which MC will not see as a valid weapon.
     */
    public ToolDefinitionBuilder cannotAttack() {
        this.attackDamage = Float.MIN_VALUE;
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
        this.isEnchantable = true;
        this.canApplyEnchantment = canApplyEnchantment;
        return this;
    }

    public ToolDefinitionBuilder canApplyEnchantment(EnumEnchantmentType... enchantmentTypes) {
        this.isEnchantable = true;
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

    public ToolDefinitionBuilder aoe(AoESymmetrical aoeSymmetrical) {
        this.aoeSymmetrical = aoeSymmetrical;
        return this;
    }

    public ToolDefinitionBuilder aoe(int additionalColumns, int additionalRows, int additionalDepth) {
        return aoe(AoESymmetrical.of(additionalColumns, additionalRows, additionalDepth));
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

    public ToolDefinitionBuilder defaultEnchantment(Enchantment enchantment, int level) {
        return this.defaultEnchantment(enchantment, level, 0);
    }

    public ToolDefinitionBuilder defaultEnchantment(Enchantment enchantment, double level, double growth) {
        this.defaultEnchantments.put(enchantment, new EnchantmentLevel(level, growth));
        return this;
    }

    public IGTToolDefinition build() {
        return new IGTToolDefinition() {

            private final List<IToolBehavior> behaviors = ImmutableList.copyOf(ToolDefinitionBuilder.this.behaviours);
            private final int damagePerAction = ToolDefinitionBuilder.this.damagePerAction;
            private final int damagePerCraftingAction = ToolDefinitionBuilder.this.damagePerCraftingAction;
            private final boolean suitableForBlockBreaking = ToolDefinitionBuilder.this.suitableForBlockBreaking;
            private final boolean suitableForAttacking = ToolDefinitionBuilder.this.suitableForAttacking;
            private final boolean suitableForCrafting = ToolDefinitionBuilder.this.suitableForCrafting;
            private final int baseDurability = ToolDefinitionBuilder.this.baseDurability;
            private final float durabilityMultiplier = ToolDefinitionBuilder.this.durabilityMultiplier;
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
            private final Predicate<IBlockState> effectiveStatePredicate;
            private final Object2ObjectMap<Enchantment, EnchantmentLevel> defaultEnchantments = ToolDefinitionBuilder.this.defaultEnchantments;

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
                    effectiveStatePredicate = effectiveStatePredicate == null ? effectiveStates :
                            effectiveStatePredicate.or(effectiveStates);
                }
                this.effectiveStatePredicate = effectiveStatePredicate == null ? state -> false :
                        effectiveStatePredicate;
            }

            @Override
            public List<IToolBehavior> getBehaviors() {
                return behaviors;
            }

            @Override
            public boolean isToolEffective(IBlockState state) {
                return effectiveStatePredicate.test(state);
            }

            @Override
            public int getDamagePerCraftingAction(ItemStack stack) {
                return damagePerCraftingAction;
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
            public float getDurabilityMultiplier(ItemStack stack) {
                return durabilityMultiplier;
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
            public Object2ObjectMap<Enchantment, EnchantmentLevel> getDefaultEnchantments(ItemStack stack) {
                return Object2ObjectMaps.unmodifiable(this.defaultEnchantments);
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
        };
    }
}
