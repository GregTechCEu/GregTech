package gregtech.api.items.toolitem;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import crazypants.enderio.api.tool.ITool;
import forestry.api.arboriculture.IToolGrafter;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.IToolItem;
import gregtech.api.items.ToolDictNames;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.items.metaitem.stats.IItemContainerItemProvider;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.common.ConfigHolder;
import gregtech.common.tools.DamageValues;
import gregtech.common.tools.ToolWrench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ToolMetaItem is item that can have up to Short.MAX_VALUE tools inside it
 * These tools can be made from different materials, have special behaviours, and basically do everything that standard MetaItem can do.
 * <p>
 * Tool behaviours are implemented by {@link IToolStats} objects
 * <p>
 * As example, with this code you can add LV electric drill tool:
 * {@code addItem(0, "test_item").addStats(new ElectricStats(10000, 1, true, false)).setToolStats(new ToolStatsExampleDrill()) }
 *
 * @see IToolStats
 * @see MetaItem
 */
@InterfaceList({
        @Interface(modid = GTValues.MODID_FR, iface = "forestry.api.arboriculture.IToolGrafter"),
        @Interface(modid = GTValues.MODID_BC, iface = "buildcraft.api.tools.IToolWrench"),
        @Interface(modid = GTValues.MODID_EIO, iface = "crazypants.enderio.api.tool.ITool"),
        @Interface(modid = GTValues.MODID_COFH, iface = "cofh.api.item.IToolHammer"),
        @Interface(modid = GTValues.MODID_APPENG, iface = "appeng.api.implementations.items.IAEWrench")})
public class ToolMetaItem<T extends ToolMetaItem<?>.MetaToolValueItem> extends MetaItem<T> implements IToolItem, IAOEItem, IToolGrafter, IToolWrench, ITool, IToolHammer, IAEWrench {

    public ToolMetaItem() {
        super((short) 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T constructMetaValueItem(short metaValue, String unlocalizedName) {
        return (T) new MetaToolValueItem(metaValue, unlocalizedName);
    }

    @Override
    protected String formatModelPath(T metaValueItem) {
        String name = metaValueItem.unlocalizedName;
        return "tools/" + (name.indexOf('.') == -1 ? name : name.substring(name.indexOf(".") + 1));
    }

    @Override
    protected short formatRawItemDamage(short metaValue) {
        return (short) (metaValue % 16000);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        T item = getItem(stack);
        if (item == null) {
            return 0xFFFFFF;
        }
        if (item.getColorProvider() != null) {
            return item.getColorProvider().getItemStackColor(stack, tintIndex);
        }
        IToolStats toolStats = item.getToolStats();
        return toolStats.getColor(stack, tintIndex);
    }

    @Override
    public boolean doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return true; //required for machine wrenching
    }

    @Override
    public boolean showDurabilityBar(@Nonnull ItemStack stack) {
        T item = getItem(stack);
        if (item != null && item.getDurabilityManager() != null) {
            return item.getDurabilityManager().showsDurabilityBar(stack);
        }
        //don't show durability if item is not electric and it's damage is zero
        return stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null) || getItemDamage(stack) != 0;
    }

    @Override
    public double getDurabilityForDisplay(@Nonnull ItemStack stack) {
        T item = getItem(stack);
        if (item != null && item.getDurabilityManager() != null) {
            return item.getDurabilityManager().getDurabilityForDisplay(stack);
        }
        //if itemstack has electric charge ability, show electric charge percentage
        if (stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            //noinspection ConstantConditions
            return 1.0 - (electricItem.getCharge() / (electricItem.getMaxCharge() * 1.0));
        }
        //otherwise, show actual durability percentage
        return getItemDamage(stack) / (getMaxItemDamage(stack) * 1.0);
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        //always color durability bar as item internal damage
        double internalDamage = getItemDamage(stack) / (getMaxItemDamage(stack) * 1.0);
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0 - internalDamage)) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void onCreated(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull EntityPlayer playerIn) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            toolStats.onToolCrafted(stack, playerIn);
        }
    }

    @Override
    public boolean hasContainerItem(@Nonnull ItemStack stack) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack stack) {
        stack = stack.copy();
        stack.setCount(1);
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IItemContainerItemProvider containerItemProvider = metaToolValueItem.getContainerItemProvider();
            if (containerItemProvider != null) {
                return containerItemProvider.getContainerItem(stack);
            }

            if (metaToolValueItem.toolStats != null) {
                IToolStats toolStats = metaToolValueItem.toolStats;
                int toolDamagePerCraft = toolStats.getToolDamagePerContainerCraft(stack);
                toolStats.onCraftingUse(stack, ForgeHooks.getCraftingPlayer());
                boolean canApplyDamage = damageItem(stack, ForgeHooks.getCraftingPlayer(), toolDamagePerCraft, false);
                if (!canApplyDamage) return stack;
            }
        }
        return stack;
    }

    @Override
    public List<BlockPos> getAOEBlocks(ItemStack itemStack, EntityPlayer player, RayTraceResult rayTraceResult) {
        T metaToolValueItem = getItem(itemStack);
        if (rayTraceResult.getBlockPos() != null && metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return toolStats.getAOEBlocks(itemStack, player, rayTraceResult);
        }
        return Collections.emptyList();
    }

    @Override
    public float getSaplingModifier(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return toolStats.getSaplingModifier(stack, world, player, pos);

        }
        return 0.0f;
    }

    @Override
    public boolean onBlockStartBreak(@Nonnull ItemStack itemStack, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return toolStats.onBlockPreBreak(itemStack, pos, player);
        }
        return false;
    }

    @Override
    public boolean onBlockDestroyed(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EntityLivingBase entity) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            toolStats.onBlockDestroyed(stack, world, state, pos, entity);
            toolStats.onBreakingUse(stack, world, pos);
            damageItem(stack, entity, toolStats.getToolDamagePerBlockBreak(stack), false);
        }
        return true;
    }

    public void onBlockDropsHarvested(ItemStack itemStack, World world, BlockPos pos, IBlockState blockState, EntityPlayer player, List<ItemStack> dropList) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            toolStats.convertBlockDrops(world, pos, blockState, player, dropList, itemStack);
        }
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
        Preconditions.checkNotNull(state, "null blockState");
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            if (isUsable(stack, toolStats.getToolDamagePerBlockBreak(stack)) && toolStats.canMineBlock(state, stack)) {
                return getToolDigSpeed(stack);
            }
        }
        return 1.0f;
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, @Nonnull ItemStack stack) {
        Preconditions.checkNotNull(state, "null blockState");
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return isUsable(stack, toolStats.getToolDamagePerBlockBreak(stack)) && toolStats.canMineBlock(state, stack);
        }
        return false;
    }

    @Override
    public int getHarvestLevel(@Nonnull ItemStack stack, @Nonnull String toolClass, EntityPlayer player, IBlockState blockState) {
        if (blockState == null) {
            GTLog.logger.warn("ToolMetaItem.getHarvestLevel called for tool '{}' without providing IBlockState. Offending stack trace:\n    {}",
                    toolClass, Arrays.stream(Thread.currentThread().getStackTrace()).skip(1).map(StackTraceElement::toString).collect(Collectors.joining("\n    ")));
            return -1;
        }
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem == null) {
            return -1;
        }
        IToolStats toolStats = metaToolValueItem.getToolStats();
        if (isUsable(stack, toolStats.getToolDamagePerBlockBreak(stack)) && toolStats.canMineBlock(blockState, stack)) {
            return getHarvestLevel(stack);
        }
        return -1;
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, @Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        HashMultimap<String, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.putAll(super.getAttributeModifiers(slot, stack));
        if (metaValueItem != null && slot == EntityEquipmentSlot.MAINHAND) {
            IToolStats toolStats = metaValueItem.getToolStats();
            float attackDamage = getToolAttackDamage(stack);
            float attackSpeed = toolStats.getAttackSpeed(stack);

            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", attackDamage, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", attackSpeed, 0));
        }
        return modifiers;
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull Entity entity) {
        //cancel attack if broken or out of charge
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            int damagePerAttack = metaToolValueItem.getToolStats().getToolDamagePerEntityAttack(stack);
            if (!isUsable(stack, damagePerAttack)) {
                return true;
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public boolean hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase target, @Nonnull EntityLivingBase attacker) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null) {
            IToolStats toolStats = metaValueItem.getToolStats();
            if (!damageItem(stack, attacker, toolStats.getToolDamagePerEntityAttack(stack), false)) {
                return true;
            }
            float additionalDamage = toolStats.getNormalDamageBonus(target, stack, attacker);
            float additionalMagicDamage = toolStats.getMagicDamageBonus(target, stack, attacker);
            if (additionalDamage > 0.0f) {
                target.attackEntityFrom(new EntityDamageSource(attacker instanceof EntityPlayer ? "player" : "mob", attacker), additionalDamage);
            }
            if (additionalMagicDamage > 0.0f) {
                target.attackEntityFrom(new EntityDamageSource("indirectMagic", attacker), additionalMagicDamage);
            }
        }
        return true;
    }

    public boolean isUsable(ItemStack stack, int damage) {
        IElectricItem capability = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        int energyAmount = ConfigHolder.machines.energyUsageMultiplier * damage;
        return capability == null || capability.canUse(energyAmount);
    }

    @Override
    public int damageItem(ItemStack stack, EntityLivingBase entity, int vanillaDamage, boolean allowPartial, boolean simulate) {
        IElectricItem capability = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (capability != null) {
            int energyAmount = ConfigHolder.machines.energyUsageMultiplier * vanillaDamage;
            long discharged = capability.discharge(energyAmount, capability.getTier(), true, false, true);
            // if we can't discharge full amount of energy
            if (discharged < energyAmount) {
                // when asked use the discharged energy and recalculate the equivalent damage
                if (allowPartial && discharged > 0) {
                    energyAmount = (int) discharged;
                    vanillaDamage = energyAmount / ConfigHolder.machines.energyUsageMultiplier;
                    if (energyAmount % ConfigHolder.machines.energyUsageMultiplier != 0)
                        ++vanillaDamage;
                } else {
                    // Can't do the operation
                    return 0;
                }
            }
            capability.discharge(energyAmount, capability.getTier(), true, false, simulate);
        }
        if (capability == null || (capability.getCharge() <= 0 || GTValues.RNG.nextInt(100) <= 4)) {
            T toolMetaItem = getItem(stack);
            if (toolMetaItem == null) {
                return 0;
            }
            IToolStats toolStats = toolMetaItem.getToolStats();
            if (!toolStats.isUsingDurability(stack)) {
                return vanillaDamage;
            }
            int itemDamage = getItemDamage(stack);
            int maxDamage = getMaxItemDamage(stack);
            int damageRemaining = maxDamage - itemDamage;
            int newDamageValue = itemDamage + calculateToolDamage(stack, itemRand, vanillaDamage);
            if (!simulate && !setInternalDamage(stack, newDamageValue)) {
                GTUtility.setItem(stack, toolStats.getBrokenStack(stack));
                if (entity != null)
                    entity.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1, 1);
            }
            return Math.min(vanillaDamage, damageRemaining);
        }
        return 1;
    }

    public int regainItemDurability(ItemStack itemStack, int maxDurabilityRegain) {
        IToolStats toolStats = getItem(itemStack).getToolStats();
        if (!toolStats.isUsingDurability(itemStack)) {
            return 0;
        }
        int toolDamage = getItemDamage(itemStack);
        int durabilityRegained = Math.min(toolDamage, maxDurabilityRegain);
        setInternalDamage(itemStack, toolDamage - durabilityRegained);
        return durabilityRegained;
    }

    private static int calculateToolDamage(ItemStack itemStack, Random random, int amount) {
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, itemStack);
        int damageNegated = 0;
        for (int k = 0; level > 0 && k < amount; ++k) {
            if (EnchantmentDurability.negateDamage(itemStack, level, random)) {
                ++damageNegated;
            }
        }
        return Math.max(0, amount - damageNegated);
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        ICapabilityProvider capabilityProvider = super.initCapabilities(stack, nbt);
        if (capabilityProvider != null && capabilityProvider.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            IElectricItem electricItem = capabilityProvider.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            //noinspection ConstantConditions
            electricItem.addChargeListener((itemStack, newCharge) -> {
                int newDamage = (newCharge == 0 ? 16000 : 0) + itemStack.getItemDamage() % 16000;
                if (newDamage != itemStack.getItemDamage()) {
                    itemStack.setItemDamage(newDamage);
                }
            });
        }
        return capabilityProvider;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.getItemDamage() >= metaItemOffset) {
            T item = getItem(stack);
            if (item == null) {
                return "invalid item";
            }
            Material primaryMaterial = getToolMaterial(stack);
            String materialName = primaryMaterial == null ? "" : String.valueOf(primaryMaterial.getLocalizedName());
            return LocalizationUtils.format("metaitem." + item.unlocalizedName + ".name", materialName);
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World worldIn, @Nonnull List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
        T item = getItem(itemStack);
        if (item == null) {
            return;
        }
        IToolStats toolStats = item.getToolStats();
        Material primaryMaterial = getToolMaterial(itemStack);
        int maxInternalDamage = getMaxItemDamage(itemStack);
        if (toolStats.isUsingDurability(itemStack) && maxInternalDamage > 0) {
            lines.add(I18n.format("metaitem.tool.tooltip.durability", maxInternalDamage - getItemDamage(itemStack), maxInternalDamage));
        }
        lines.add(I18n.format("metaitem.tool.tooltip.primary_material", primaryMaterial.getLocalizedName(), getHarvestLevel(itemStack)));
        lines.add(I18n.format("metaitem.tool.tooltip.attack_damage", toolStats.getBaseDamage(itemStack) + primaryMaterial.getToolHarvestLevel()));
        lines.add(I18n.format("metaitem.tool.tooltip.mining_speed", getToolDigSpeed(itemStack)));
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        toolStats.addInformation(itemStack, lines, tooltipFlag.isAdvanced());
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getItemEnchantability(@Nonnull ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.TOOL).getToolEnchantability();
    }

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null && metaToolValueItem.toolStats != null) {
            return metaToolValueItem.toolStats.canApplyEnchantment(stack, enchantment);
        }
        return false;
    }

    @Override
    public int getMaxItemDamage(ItemStack itemStack) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            NBTTagCompound toolTag = getToolStatsTag(itemStack);
            Material toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            int materialDurability = 0;
            if (toolTag != null && toolTag.hasKey("MaxDurability")) {
                materialDurability = toolTag.getInteger("MaxDurability");
            } else if (toolMaterial != null) {
                ToolProperty prop = toolMaterial.getProperty(PropertyKey.TOOL);
                if (prop != null) materialDurability = prop.getToolDurability();
                else return 0;
            }
            float multiplier = toolStats.getMaxDurabilityMultiplier(itemStack);
            return (int) (materialDurability * multiplier);
        }
        return 0;
    }

    public float getToolDigSpeed(ItemStack itemStack) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            NBTTagCompound toolTag = getToolStatsTag(itemStack);
            Material toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            float toolSpeed = 0;
            if (toolTag != null && toolTag.hasKey("DigSpeed")) {
                toolSpeed = toolTag.getFloat("DigSpeed");
            } else if (toolMaterial != null) {
                ToolProperty prop = toolMaterial.getProperty(PropertyKey.TOOL);
                if (prop != null) toolSpeed = prop.getToolSpeed();
                else return 0;
            }
            float multiplier = toolStats.getDigSpeedMultiplier(itemStack);
            return toolSpeed * multiplier;
        }
        return 0;
    }

    public int getHarvestLevel(ItemStack itemStack) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            NBTTagCompound toolTag = getToolStatsTag(itemStack);
            Material toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            int harvestLevel = 0;
            if (toolTag != null && toolTag.hasKey("HarvestLevel")) {
                harvestLevel = toolTag.getInteger("HarvestLevel");
            } else if (toolMaterial != null) {
                DustProperty prop = toolMaterial.getProperty(PropertyKey.DUST);
                if (prop != null) harvestLevel = prop.getHarvestLevel();
                else return 0;
            }
            int baseHarvestLevel = toolStats.getBaseQuality(itemStack);
            return baseHarvestLevel + harvestLevel;
        }
        return 0;
    }

    public float getToolAttackDamage(ItemStack itemStack) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            NBTTagCompound toolTag = getToolStatsTag(itemStack);
            Material toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            float attackDamage = 0;
            if (toolTag != null && toolTag.hasKey("AttackDamage")) {
                attackDamage = toolTag.getFloat("AttackDamage");
            } else if (toolTag != null && toolTag.hasKey("HarvestLevel")) {
                attackDamage = toolTag.getInteger("HarvestLevel");
            } else if (toolMaterial != null) {
                ToolProperty prop = toolMaterial.getProperty(PropertyKey.TOOL);
                if (prop != null) attackDamage = prop.getToolAttackDamage();
                else return 0;
            }
            float baseAttackDamage = toolStats.getBaseDamage(itemStack);
            return baseAttackDamage + attackDamage;
        }
        return 0;
    }

    @Override
    public int getItemDamage(ItemStack itemStack) {
        NBTTagCompound statsTag = getToolStatsTag(itemStack);
        if (statsTag == null) {
            return 0;
        }
        if (statsTag.hasKey("Damage")) {
            boolean isElectricItem = itemStack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            int oldToolDamage = statsTag.getInteger("Damage");
            return isElectricItem ? oldToolDamage : oldToolDamage / 10;
        }
        return statsTag.getInteger("Dmg");
    }

    private boolean setInternalDamage(ItemStack itemStack, int damage) {
        NBTTagCompound statsTag = getOrCreateToolStatsTag(itemStack);
        statsTag.setInteger("Dmg", damage);
        statsTag.removeTag("Damage");
        return getItemDamage(itemStack) < getMaxItemDamage(itemStack);
    }

    private static NBTTagCompound getToolStatsTag(ItemStack itemStack) {
        return itemStack.getSubCompound("GT.ToolStats");
    }

    private static NBTTagCompound getOrCreateToolStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound("GT.ToolStats");
    }

    public static Material getToolMaterial(ItemStack itemStack) {
        NBTTagCompound statsTag = getToolStatsTag(itemStack);
        if (statsTag == null) {
            return Materials.Neutronium;
        }
        String toolMaterialName;
        if (statsTag.hasKey("Material")) {
            toolMaterialName = statsTag.getString("Material");
        } else if (statsTag.hasKey("PrimaryMaterial")) {
            toolMaterialName = statsTag.getString("PrimaryMaterial");
        } else {
            return Materials.Neutronium;
        }
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObject(toolMaterialName);
        if (material != null) {
            return material;
        }
        return Materials.Neutronium;
    }

    @Override
    @Nonnull
    public Set<String> getToolClasses(@Nonnull ItemStack stack) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return toolStats.getToolClasses(stack);
        }
        return Collections.emptySet();
    }

    // BuildCraft Wrench Compat
    @Override
    public boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        T metaToolValueItem = getItem(player.getHeldItem(hand));
        if (metaToolValueItem != null) {
            return metaToolValueItem.getToolStats() instanceof ToolWrench;
        }
        return false;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        this.damageItem(player.getHeldItem(hand), player, DamageValues.DAMAGE_FOR_WRENCH, false);
    }

    // CoFH Hammer Compat
    @Override
    public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
        T metaToolValueItem = getItem(item);
        if (metaToolValueItem != null) {
            return metaToolValueItem.getToolStats() instanceof ToolWrench;
        }
        return false;
    }

    @Override
    public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
        T metaToolValueItem = getItem(item);
        if (metaToolValueItem != null) {
            return metaToolValueItem.getToolStats() instanceof ToolWrench;
        }
        return false;
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
        this.damageItem(item, user, DamageValues.DAMAGE_FOR_WRENCH, false);
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {
        this.damageItem(item, user, DamageValues.DAMAGE_FOR_WRENCH, false);
    }

    // EIO Wrench Compat
    @Override
    public boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canUse(@Nonnull EnumHand stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
        T metaToolValueItem = getItem(player.getHeldItem(stack));
        if (metaToolValueItem != null) {
            return metaToolValueItem.getToolStats() instanceof ToolWrench;
        }
        return false;
    }

    @Override
    public void used(@Nonnull EnumHand stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
        this.damageItem(player.getHeldItem(stack), player, DamageValues.DAMAGE_FOR_WRENCH, false);
    }

    // Applied Energistics Wrench Compat
    @Override
    public boolean canWrench(ItemStack wrench, EntityPlayer player, BlockPos pos) {
        T metaToolValueItem = getItem(wrench);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            if (toolStats instanceof ToolWrench) {
                damageItem(wrench, player, DamageValues.DAMAGE_FOR_WRENCH, false);
                return true;
            }
        }
        return false;
    }

    public void setCraftingSoundTime(ItemStack stack) {
        NBTTagCompound statsTag = getOrCreateToolStatsTag(stack);
        statsTag.setInteger("lastCraftingUse", (int) System.currentTimeMillis());
    }

    public boolean canPlaySound(ItemStack stack) {
        NBTTagCompound statsTag = getOrCreateToolStatsTag(stack);
        return Math.abs((int) System.currentTimeMillis() - statsTag.getInteger("lastCraftingUse")) > 16;
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    public class MetaToolValueItem extends MetaValueItem {

        protected IToolStats toolStats = new DummyToolStats();
        protected double amountOfMaterialToRepair = 0;
        protected SoundEvent sound;

        protected MetaToolValueItem(int metaValue, String unlocalizedName) {
            super(metaValue, unlocalizedName);
            setMaxStackSize(1);
        }

        @Override
        public MetaToolValueItem addComponents(IItemComponent... stats) {
            super.addComponents(stats);
            return this;
        }

        public MetaToolValueItem setSound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public SoundEvent getSound() {
            return sound;
        }

        public MetaToolValueItem setFullRepairCost(double amountOfMaterialToRepair) {
            this.amountOfMaterialToRepair = amountOfMaterialToRepair;
            return this;
        }

        public MetaToolValueItem setToolStats(IToolStats toolStats) {
            Preconditions.checkNotNull(toolStats, "Cannot set toolStats to null");
            this.toolStats = toolStats;
            toolStats.onStatsAddedToTool(this);
            return this;
        }

        public MetaToolValueItem addOreDict(ToolDictNames... oreDictNames) {
            Validate.notNull(oreDictNames, "Cannot add null ToolDictName.");
            Validate.noNullElements(oreDictNames, "Cannot add null ToolDictName.");

            for (ToolDictNames oreDict : oreDictNames) {
                OreDictionary.registerOre(oreDict.name(), getStackForm());
            }
            return this;
        }

        @Nonnull
        public IToolStats getToolStats() {
            return toolStats;
        }

        public double getAmountOfMaterialToRepair(ItemStack toolStack) {
            return amountOfMaterialToRepair;
        }

        @Override
        public ItemStack getStackForm(int amount) {
            ItemStack rawStack = super.getStackForm(amount);
            setToolMaterial(rawStack, Materials.Neutronium);
            return rawStack;
        }

        public ItemStack getStackForm(Material primaryMaterial) {
            ItemStack rawStack = super.getStackForm(1);
            setToolMaterial(rawStack, primaryMaterial);
            return rawStack;
        }

        public ItemStack getChargedStack(Material primaryMaterial, long chargeAmount) {
            ItemStack rawStack = super.getChargedStack(chargeAmount);
            setToolMaterial(rawStack, primaryMaterial);
            return rawStack;
        }

        public ItemStack getMaxChargeOverrideStack(Material primaryMaterial, long maxCharge) {
            ItemStack rawStack = super.getMaxChargeOverrideStack(maxCharge);
            setToolMaterial(rawStack, primaryMaterial);
            return rawStack;
        }

        public final ItemStack getStackForm(Material primaryMaterial, int amount) {
            ItemStack stack = new ItemStack(ToolMetaItem.this, amount, metaItemOffset + metaValue);
            setToolMaterial(stack, primaryMaterial);
            return stack;
        }

        public void setToolMaterial(ItemStack stack, Material toolMaterial) {
            NBTTagCompound toolNBT = new NBTTagCompound();
            ArrayList<Material> materials = new ArrayList<>();
            toolNBT.setString("Material", toolMaterial.toString());
            materials.add(toolMaterial);

            NBTTagCompound nbtTag = stack.getTagCompound();
            if (nbtTag == null) {
                nbtTag = new NBTTagCompound();
            }
            nbtTag.setTag("GT.ToolStats", toolNBT);
            stack.setTagCompound(nbtTag);

            Map<Enchantment, Integer> enchantments = bakeEnchantmentsMap(stack, materials);
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }

        public ItemStack setToolData(ItemStack stack, Material toolMaterial, int maxDurability, int harvestLevel, float digSpeed, float attackDamage) {
            NBTTagCompound toolNBT = new NBTTagCompound();
            ArrayList<Material> materials = new ArrayList<>();
            toolNBT.setString("Material", toolMaterial.toString());
            materials.add(toolMaterial);
            if (maxDurability > -1) {
                toolNBT.setInteger("MaxDurability", maxDurability);
            }
            if (harvestLevel > -1) {
                toolNBT.setInteger("HarvestLevel", harvestLevel);
            }
            if (digSpeed > -1.0f) {
                toolNBT.setFloat("DigSpeed", digSpeed);
            }
            if (attackDamage > -1.0f) {
                toolNBT.setFloat("AttackDamage", attackDamage);
            }
            NBTTagCompound nbtTag = stack.getTagCompound();
            if (nbtTag == null) {
                nbtTag = new NBTTagCompound();
            }
            nbtTag.setTag("GT.ToolStats", toolNBT);
            stack.setTagCompound(nbtTag);

            Map<Enchantment, Integer> enchantments = bakeEnchantmentsMap(stack, materials);
            EnchantmentHelper.setEnchantments(enchantments, stack);
            return stack;
        }

        private Map<Enchantment, Integer> bakeEnchantmentsMap(ItemStack itemStack, Collection<Material> materials) {
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            for (Material material : materials) {
                ToolProperty prop = material.getProperty(PropertyKey.TOOL);
                if (prop == null)
                    continue;

                for (EnchantmentData enchantmentData : prop.toolEnchantments) {
                    if (enchantments.containsKey(enchantmentData.enchantment)) {
                        int level = Math.min(enchantments.get(enchantmentData.enchantment) + enchantmentData.level,
                                enchantmentData.enchantment.getMaxLevel());
                        enchantments.put(enchantmentData.enchantment, level);
                    } else {
                        enchantments.put(enchantmentData.enchantment, enchantmentData.level);
                    }
                }
            }
            for (EnchantmentData enchantmentData : toolStats.getEnchantments(itemStack)) {
                if (enchantments.containsKey(enchantmentData.enchantment)) {
                    int level = Math.min(enchantments.get(enchantmentData.enchantment) + enchantmentData.level,
                            enchantmentData.enchantment.getMaxLevel());
                    enchantments.put(enchantmentData.enchantment, level);
                } else {
                    enchantments.put(enchantmentData.enchantment, enchantmentData.level);
                }
            }
            enchantments.keySet().removeIf(enchantment -> !enchantment.canApply(itemStack));
            return enchantments;
        }
    }
}
