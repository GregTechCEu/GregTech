package gregtech.api.items.toolitem;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import forestry.api.arboriculture.IToolGrafter;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.enchants.EnchantmentData;
import gregtech.api.items.IToolItem;
import gregtech.api.items.ToolDictNames;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.items.metaitem.stats.IItemContainerItemProvider;
import gregtech.api.items.metaitem.stats.IMetaItemStats;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.material.type.SolidMaterial;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
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
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional.Interface;
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
@Interface(modid = GTValues.MODID_FR, iface = "forestry.api.arboriculture.IToolGrafter")
public class ToolMetaItem<T extends ToolMetaItem<?>.MetaToolValueItem> extends MetaItem<T> implements IToolItem, IAOEItem, IToolGrafter {

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
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true; //required for machine wrenching
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        T item = getItem(stack);
        if (item != null && item.getDurabilityManager() != null) {
            return item.getDurabilityManager().showsDurabilityBar(stack);
        }
        //don't show durability if item is not electric and it's damage is zero
        return stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null) || getItemDamage(stack) != 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
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
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        //always color durability bar as item internal damage
        double internalDamage = getItemDamage(stack) / (getMaxItemDamage(stack) * 1.0);
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0 - internalDamage)) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            toolStats.onToolCrafted(stack, playerIn);
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
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
                boolean canApplyDamage = damageItem(stack, toolDamagePerCraft, false);
                if (!canApplyDamage) return stack;
            }
        }
        return stack;
    }

    @Override
    public List<BlockPos> getAOEBlocks(ItemStack itemStack, EntityPlayer player, RayTraceResult rayTraceResult) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return toolStats.getAOEBlocks(itemStack, player, rayTraceResult);
        }
        return Collections.emptyList();
    }

    @Override
    public float getSaplingModifier(ItemStack stack, World world, EntityPlayer player, BlockPos pos) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return toolStats.getSaplingModifier(stack, world, player, pos);

        }
        return 0.0f;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemStack, BlockPos pos, EntityPlayer player) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return toolStats.onBlockPreBreak(itemStack, pos, player);
        }
        return false;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase entity) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            toolStats.onBlockDestroyed(stack, world, state, pos, entity);
            damageItem(stack, toolStats.getToolDamagePerBlockBreak(stack), false);
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
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
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
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        Preconditions.checkNotNull(state, "null blockState");
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null) {
            IToolStats toolStats = metaToolValueItem.getToolStats();
            return isUsable(stack, toolStats.getToolDamagePerBlockBreak(stack)) && toolStats.canMineBlock(state, stack);
        }
        return false;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, EntityPlayer player, IBlockState blockState) {
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

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        T metaValueItem = getItem(stack);
        HashMultimap<String, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.putAll(super.getAttributeModifiers(slot, stack));
        if (metaValueItem != null && slot == EntityEquipmentSlot.MAINHAND) {
            IToolStats toolStats = metaValueItem.getToolStats();
            if (toolStats == null) {
                return HashMultimap.create();
            }
            float attackDamage = getToolAttackDamage(stack);
            float attackSpeed = toolStats.getAttackSpeed(stack);

            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", attackDamage, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", attackSpeed, 0));
        }
        return modifiers;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
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
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null) {
            IToolStats toolStats = metaValueItem.getToolStats();
            if (!damageItem(stack, toolStats.getToolDamagePerEntityAttack(stack), false)) {
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
        int energyAmount = ConfigHolder.energyUsageMultiplier * damage;
        return capability == null || capability.canUse(energyAmount);
    }

    @Override
    public int damageItem(ItemStack stack, int vanillaDamage, boolean allowPartial, boolean simulate) {
        IElectricItem capability = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (capability != null) {
            int energyAmount = ConfigHolder.energyUsageMultiplier * vanillaDamage;
            long discharged = capability.discharge(energyAmount, capability.getTier(), true, false, true);
            // if we can't discharge full amount of energy
            if (discharged < energyAmount) {
                // when asked use the discharged energy and recalculate the equivalent damage
                if (allowPartial && discharged > 0) {
                    energyAmount = (int) discharged;
                    vanillaDamage = energyAmount / ConfigHolder.energyUsageMultiplier;
                    if (energyAmount % ConfigHolder.energyUsageMultiplier != 0)
                        ++vanillaDamage;
                } else {
                    // Can't do the operation
                    return 0;
                }
            }
            capability.discharge(energyAmount, capability.getTier(), true, false, simulate);
        }
        if ( capability == null || (capability.getCharge() <= 0 || GTUtility.getRandomIntXSTR(100) <= 4)) {
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
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
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

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.getItemDamage() >= metaItemOffset) {
            T item = getItem(stack);
            if (item == null) {
                return "invalid item";
            }
            SolidMaterial primaryMaterial = getToolMaterial(stack);
            String materialName = primaryMaterial == null ? "" : String.valueOf(primaryMaterial.getLocalizedName());
            return I18n.format("metaitem." + item.unlocalizedName + ".name", materialName);
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> lines, ITooltipFlag tooltipFlag) {
        T item = getItem(itemStack);
        if (item == null) {
            return;
        }
        IToolStats toolStats = item.getToolStats();
        SolidMaterial primaryMaterial = getToolMaterial(itemStack);
        int maxInternalDamage = getMaxItemDamage(itemStack);
        if (toolStats.isUsingDurability(itemStack) && maxInternalDamage > 0) {
            lines.add(I18n.format("metaitem.tool.tooltip.durability", maxInternalDamage - getItemDamage(itemStack), maxInternalDamage));
        }
        lines.add(I18n.format("metaitem.tool.tooltip.primary_material", primaryMaterial.getLocalizedName(), getHarvestLevel(itemStack)));
        lines.add(I18n.format("metaitem.tool.tooltip.attack_damage", toolStats.getBaseDamage(itemStack) + primaryMaterial.harvestLevel));
        lines.add(I18n.format("metaitem.tool.tooltip.mining_speed", getToolDigSpeed(itemStack)));
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        toolStats.addInformation(itemStack, lines, tooltipFlag.isAdvanced());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        SolidMaterial primaryMaterial = getToolMaterial(stack);
        return getMaterialEnchantability(primaryMaterial);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        T metaToolValueItem = getItem(stack);
        if (metaToolValueItem != null && metaToolValueItem.toolStats != null) {
            return metaToolValueItem.toolStats.canApplyEnchantment(stack, enchantment);
        }
        return false;
    }

    public static int getMaterialEnchantability(SolidMaterial material) {
        if (material.materialIconSet == MaterialIconSet.SHINY ||
            material.materialIconSet == MaterialIconSet.RUBY) {
            return 33; //all shiny metals have gold enchantability
        } else if (material.materialIconSet == MaterialIconSet.DULL ||
            material.materialIconSet == MaterialIconSet.METALLIC) {
            return 21; //dull metals have iron enchantability
        } else if (material.materialIconSet == MaterialIconSet.GEM_VERTICAL ||
            material.materialIconSet == MaterialIconSet.GEM_HORIZONTAL ||
            material.materialIconSet == MaterialIconSet.DIAMOND ||
            material.materialIconSet == MaterialIconSet.OPAL ||
            material.materialIconSet == MaterialIconSet.NETHERSTAR) {
            return 15; //standard gems have diamond enchantability
        } else if (material.materialIconSet == MaterialIconSet.WOOD ||
            material.materialIconSet == MaterialIconSet.ROUGH ||
            material.materialIconSet == MaterialIconSet.FINE) {
            return 11; //wood and stone has their default enchantability
        }
        return 10; //otherwise return lowest enchantability
    }

    @Override
    public int getMaxItemDamage(ItemStack itemStack) {
        T metaToolValueItem = getItem(itemStack);
        if (metaToolValueItem != null) {
            NBTTagCompound toolTag = getToolStatsTag(itemStack);
            SolidMaterial toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            int materialDurability = 0;
            if (toolTag != null && toolTag.hasKey("MaxDurability")) {
                materialDurability = toolTag.getInteger("MaxDurability");
            } else if (toolMaterial != null) {
                materialDurability = toolMaterial.toolDurability;
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
            SolidMaterial toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            float toolSpeed = 0;
            if (toolTag != null && toolTag.hasKey("DigSpeed")) {
                toolSpeed = toolTag.getFloat("DigSpeed");
            } else if (toolMaterial != null) {
                toolSpeed = toolMaterial.toolSpeed;
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
            SolidMaterial toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            int harvestLevel = 0;
            if (toolTag != null && toolTag.hasKey("HarvestLevel")) {
                harvestLevel = toolTag.getInteger("HarvestLevel");
            } else if (toolMaterial != null) {
                harvestLevel = toolMaterial.harvestLevel;
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
            SolidMaterial toolMaterial = getToolMaterial(itemStack);
            IToolStats toolStats = metaToolValueItem.getToolStats();
            float attackDamage = 0;
            if (toolTag != null && toolTag.hasKey("AttackDamage")) {
                attackDamage = toolTag.getFloat("AttackDamage");
            } else if (toolTag != null && toolTag.hasKey("HarvestLevel")) {
                attackDamage = toolTag.getInteger("HarvestLevel");
            } else if (toolMaterial != null) {
                attackDamage = toolMaterial.toolAttackDamage;
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

    public static SolidMaterial getToolMaterial(ItemStack itemStack) {
        NBTTagCompound statsTag = getToolStatsTag(itemStack);
        if (statsTag == null) {
            return Materials.Aluminium;
        }
        String toolMaterialName;
        if (statsTag.hasKey("Material")) {
            toolMaterialName = statsTag.getString("Material");
        } else if (statsTag.hasKey("PrimaryMaterial")) {
            toolMaterialName = statsTag.getString("PrimaryMaterial");
        } else {
            return Materials.Aluminium;
        }
        Material material = Material.MATERIAL_REGISTRY.getObject(toolMaterialName);
        if (material instanceof SolidMaterial) {
            return (SolidMaterial) material;
        }
        return Materials.Aluminium;
    }

    public class MetaToolValueItem extends MetaValueItem {

        protected IToolStats toolStats = new DummyToolStats();
        protected double amountOfMaterialToRepair = 0;

        protected MetaToolValueItem(int metaValue, String unlocalizedName) {
            super(metaValue, unlocalizedName);
            setMaxStackSize(1);
        }

        @Override
        @Deprecated
        public MetaToolValueItem addStats(IMetaItemStats... stats) {
            super.addStats(stats);
            return this;
        }

        @Override
        public MetaToolValueItem addComponents(IItemComponent... stats) {
            super.addComponents(stats);
            return this;
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
            setToolMaterial(rawStack, Materials.Darmstadtium);
            return rawStack;
        }

        public ItemStack getStackForm(SolidMaterial primaryMaterial) {
            ItemStack rawStack = super.getStackForm(1);
            setToolMaterial(rawStack, primaryMaterial);
            return rawStack;
        }

        public ItemStack getChargedStack(SolidMaterial primaryMaterial, long chargeAmount) {
            ItemStack rawStack = super.getChargedStack(chargeAmount);
            setToolMaterial(rawStack, primaryMaterial);
            return rawStack;
        }

        public ItemStack getMaxChargeOverrideStack(SolidMaterial primaryMaterial, long maxCharge) {
            ItemStack rawStack = super.getMaxChargeOverrideStack(maxCharge);
            setToolMaterial(rawStack, primaryMaterial);
            return rawStack;
        }

        public final ItemStack getStackForm(SolidMaterial primaryMaterial, int amount) {
            ItemStack stack = new ItemStack(ToolMetaItem.this, amount, metaItemOffset + metaValue);
            setToolMaterial(stack, primaryMaterial);
            return stack;
        }

        public void setToolMaterial(ItemStack stack, SolidMaterial toolMaterial) {
            NBTTagCompound toolNBT = new NBTTagCompound();
            ArrayList<SolidMaterial> materials = new ArrayList<>();
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

        public ItemStack setToolData(ItemStack stack, SolidMaterial toolMaterial, int maxDurability, int harvestLevel, float digSpeed, float attackDamage) {
            NBTTagCompound toolNBT = new NBTTagCompound();
            ArrayList<SolidMaterial> materials = new ArrayList<>();
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

        private Map<Enchantment, Integer> bakeEnchantmentsMap(ItemStack itemStack, Collection<SolidMaterial> materials) {
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            for (SolidMaterial material : materials) {
                for (EnchantmentData enchantmentData : material.toolEnchantments) {
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
