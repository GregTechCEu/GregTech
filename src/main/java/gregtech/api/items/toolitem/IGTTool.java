package gregtech.api.items.toolitem;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import com.enderio.core.common.interfaces.IOverlayRenderAware;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import crazypants.enderio.api.tool.ITool;
import forestry.api.arboriculture.IToolGrafter;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.ConfigHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static gregtech.api.items.armor.IArmorLogic.*;
import static gregtech.api.items.toolitem.ToolHelper.*;

/**
 * Backing of every variation of a GT Tool
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = GTValues.MODID_APPENG, iface = "appeng.api.implementations.items.IAEWrench"),
        @Optional.Interface(modid = GTValues.MODID_BC, iface = "buildcraft.api.tools.IToolWrench"),
        @Optional.Interface(modid = GTValues.MODID_COFH, iface = "cofh.api.item.IToolHammer"),
        @Optional.Interface(modid = GTValues.MODID_EIO, iface = "crazypants.enderio.api.tool.ITool"),
        @Optional.Interface(modid = GTValues.MODID_FR, iface = "forestry.api.arboriculture.IToolGrafter"),
        @Optional.Interface(modid = GTValues.MODID_EIO, iface = "com.enderio.core.common.interfaces.IOverlayRenderAware")})
public interface IGTTool extends ItemUIFactory, IAEWrench, IToolWrench, IToolHammer, ITool, IToolGrafter, IOverlayRenderAware {

    String getDomain();

    String getId();

    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEvent getSound();

    Set<Block> getEffectiveBlocks();

    Set<String> getOreDictNames();

    default Item get() {
        return (Item) this;
    }

    default ItemStack get(Material material) {
        ItemStack stack = new ItemStack(get());
        NBTTagCompound toolTag = getToolTag(stack);

        stack.getTagCompound().setBoolean(DISALLOW_CONTAINER_ITEM_KEY, false);

        // Set Material
        toolTag.setString(MATERIAL_KEY, material.toString());

        // Set other tool stats (durability)
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
        toolTag.setInteger(MAX_DURABILITY_KEY, toolProperty.getToolDurability());
        toolTag.setInteger(DURABILITY_KEY, 0);

        // Set material enchantments
        toolProperty.getEnchantments().forEach((enchantment, level) -> {
            if (stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
                stack.addEnchantment(enchantment, level);
            }
        });

        // Set behaviours
        NBTTagCompound behaviourTag = getBehavioursTag(stack);
        AoEDefinition aoeDefinition = getToolStats().getAoEDefinition(stack);

        behaviourTag.setInteger(MAX_AOE_COLUMN_KEY, aoeDefinition.column);
        behaviourTag.setInteger(MAX_AOE_ROW_KEY, aoeDefinition.row);
        behaviourTag.setInteger(MAX_AOE_LAYER_KEY, aoeDefinition.layer);
        behaviourTag.setInteger(AOE_COLUMN_KEY, aoeDefinition.column);
        behaviourTag.setInteger(AOE_ROW_KEY, aoeDefinition.row);
        behaviourTag.setInteger(AOE_LAYER_KEY, aoeDefinition.layer);

        Set<String> toolClasses = stack.getItem().getToolClasses(stack);

        behaviourTag.setBoolean(HARVEST_ICE_KEY, toolClasses.contains("saw"));
        behaviourTag.setBoolean(TORCH_PLACING_KEY, toolClasses.contains("pickaxe"));
        behaviourTag.setBoolean(TREE_FELLING_KEY, toolClasses.contains("axe"));
        behaviourTag.setBoolean(DISABLE_SHIELDS_KEY, toolClasses.contains("axe"));
        behaviourTag.setBoolean(RELOCATE_MINED_BLOCKS_KEY, false);

        return stack;
    }

    default ItemStack get(Material material, long defaultCharge, long defaultMaxCharge) {
        ItemStack stack = get(material);
        if (isElectric()) {
            ElectricItem electricItem = (ElectricItem) stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem != null) {
                electricItem.setMaxChargeOverride(defaultMaxCharge);
                electricItem.setCharge(defaultCharge);
            }
        }
        return stack;
    }

    default ItemStack get(Material material, long defaultMaxCharge) {
        return get(material, defaultMaxCharge, defaultMaxCharge);
    }

    default Material getToolMaterial(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        String string = toolTag.getString(MATERIAL_KEY);
        Material material = GregTechAPI.MaterialRegistry.get(string);
        if (material == null) {
            toolTag.setString(MATERIAL_KEY, (material = Materials.Neutronium).toString());
        }
        return material;
    }

    default ToolProperty getToolProperty(ItemStack stack) {
        Material material = getToolMaterial(stack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        if (property == null) {
            property = Materials.Neutronium.getProperty(PropertyKey.TOOL);
        }
        return property;
    }

    default DustProperty getDustProperty(ItemStack stack) {
        Material material = getToolMaterial(stack);
        DustProperty property = material.getProperty(PropertyKey.DUST);
        if (property == null) {
            property = Materials.Neutronium.getProperty(PropertyKey.DUST);
        }
        return property;
    }

    default float getMaterialToolSpeed(ItemStack stack) {
        return getToolProperty(stack).getToolSpeed();
    }

    default float getMaterialAttackDamage(ItemStack stack) {
        return getToolProperty(stack).getToolAttackDamage();
    }

    default int getMaterialDurability(ItemStack stack) {
        return getToolProperty(stack).getToolDurability();
    }

    default int getMaterialEnchantability(ItemStack stack) {
        return getToolProperty(stack).getToolEnchantability();
    }

    default int getMaterialHarvestLevel(ItemStack stack) {
        return getDustProperty(stack).getHarvestLevel();
    }

    default long getMaxCharge(ItemStack stack) {
        if (isElectric()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(MAX_CHARGE_KEY, Constants.NBT.TAG_LONG)) {
                return stack.getTagCompound().getLong(MAX_CHARGE_KEY);
            }
        }
        return -1L;
    }

    default long getCharge(ItemStack stack) {
        if (isElectric()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(CHARGE_KEY, Constants.NBT.TAG_LONG)) {
                return stack.getTagCompound().getLong(CHARGE_KEY);
            }
        }
        return -1L;
    }

    default float getTotalToolSpeed(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(TOOL_SPEED_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(TOOL_SPEED_KEY);
        }
        float toolSpeed = getMaterialToolSpeed(stack) + getToolStats().getBaseEfficiency(stack);
        toolTag.setFloat(TOOL_SPEED_KEY, toolSpeed);
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(ATTACK_DAMAGE_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_DAMAGE_KEY);
        }
        float attackDamage = getMaterialAttackDamage(stack) + getToolStats().getBaseDamage(stack);
        toolTag.setFloat(ATTACK_DAMAGE_KEY, attackDamage);
        return attackDamage;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(MAX_DURABILITY_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(MAX_DURABILITY_KEY);
        }
        int maxDurability = getMaterialDurability(stack) + getToolStats().getBaseDurability(stack);
        toolTag.setInteger(MAX_DURABILITY_KEY, maxDurability);
        return maxDurability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(ENCHANTABILITY_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(ENCHANTABILITY_KEY);
        }
        int enchantability = getMaterialEnchantability(stack);
        toolTag.setInteger(ENCHANTABILITY_KEY, enchantability);
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(HARVEST_LEVEL_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(HARVEST_LEVEL_KEY);
        }
        int harvestLevel = getMaterialHarvestLevel(stack) + getToolStats().getBaseQuality(stack);
        toolTag.setInteger(HARVEST_LEVEL_KEY, harvestLevel);
        return harvestLevel;
    }

    default AoEDefinition getMaxAoEDefinition(ItemStack stack) {
        return AoEDefinition.readMax(getToolTag(stack));
    }

    default AoEDefinition getAoEDefinition(ItemStack stack) {
        return AoEDefinition.read(getToolTag(stack), getMaxAoEDefinition(stack));
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, IBlockState state) {
        for (String type : get().getToolClasses(stack)) {
            if (type.equals("sword")) {
                Block block = state.getBlock();
                if (block instanceof BlockWeb) {
                    return 15F;
                } else if (getEffectiveBlocks().contains(block)) {
                    return getTotalToolSpeed(stack);
                } else {
                    net.minecraft.block.material.Material material = state.getMaterial();
                    return material != net.minecraft.block.material.Material.PLANTS && material != net.minecraft.block.material.Material.VINE && material != net.minecraft.block.material.Material.CORAL && material != net.minecraft.block.material.Material.LEAVES && material != net.minecraft.block.material.Material.GOURD ? 1.0F : 1.5F;
                }
            } else if (state.getBlock().isToolEffective(type, state)) {
                return getTotalToolSpeed(stack);
            }
        }
        return getEffectiveBlocks().contains(state.getBlock()) ? getTotalToolSpeed(stack) : 1.0F;
    }

    default boolean definition$hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        if (!player.world.isRemote && !player.isSneaking()) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            int result = ToolHelper.shearBlockRoutine(playerMP, stack, pos);
            if (result != 0) {
                if (!areaOfEffectBlockBreakRoutine(stack, playerMP)) {
                    if (result == -1) {
                        treeFellingRoutine(playerMP, stack, pos);
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    default boolean definition$onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote) {
            if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
            }
        }
        return true;
    }

    default boolean definition$getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (repair.getItem() instanceof IGTTool) {
            return getToolMaterial(toRepair) == ((IGTTool) repair.getItem()).getToolMaterial(repair);
        }
        MaterialStack repairMaterialStack = OreDictUnifier.getMaterial(repair);
        return repairMaterialStack != null && repairMaterialStack.material == getToolMaterial(toRepair);
    }

    default Multimap<String, AttributeModifier> definition$getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getTotalAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", getToolStats().getAttackSpeed(stack), 0));
        }
        return multimap;
    }

    default int definition$getHarvestLevel(ItemStack stack, String toolClass, @javax.annotation.Nullable net.minecraft.entity.player.EntityPlayer player, @javax.annotation.Nullable IBlockState blockState) {
        return get().getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity, EntityLivingBase attacker) {
        return getBehavioursTag(stack).getBoolean(DISABLE_SHIELDS_KEY);
    }

    default boolean definition$doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem() || oldStack.getItemDamage() < newStack.getItemDamage();
    }

    default boolean definition$hasContainerItem(ItemStack stack) {
        return stack.getTagCompound() == null || !stack.getTagCompound().getBoolean(DISALLOW_CONTAINER_ITEM_KEY);
    }

    default ItemStack definition$getContainerItem(ItemStack stack) {
        // Sanity-check, callers should really validate with hasContainerItem themselves...
        if (!definition$hasContainerItem(stack)) {
            return ItemStack.EMPTY;
        }
        int damage = getToolStats().getToolDamagePerCraft(stack);
        if (damage > 0) {
            EntityPlayer player = ForgeHooks.getCraftingPlayer();
            damageItem(stack, player, damage);
            playCraftingSound(player);
            // We cannot simply return the copied stack here because Forge's bug
            // Introduced here: https://github.com/MinecraftForge/MinecraftForge/pull/3388
            // Causing PlayerDestroyItemEvent to never be fired under correct circumstances.
            // While preliminarily fixing ItemStack being null in ForgeHooks#getContainerItem in the PR
            // The semantics was misunderstood, any stack that are "broken" (damaged beyond maxDamage)
            // Will be "empty" ItemStacks (while not == ItemStack.EMPTY, but isEmpty() == true)
            // PlayerDestroyItemEvent will not be fired correctly because of this oversight.
            if (stack.isEmpty()) { // Equal to listening to PlayerDestroyItemEvent
                return getToolStats().getBrokenStack();
            }
        }
        return stack.copy();
    }

    default boolean definition$isDamaged(ItemStack stack) {
        return definition$getDamage(stack) > 0;
    }

    default int definition$getDamage(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(DURABILITY_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(DURABILITY_KEY);
        }
        toolTag.setInteger(DURABILITY_KEY, 0);
        return 0;
    }

    default int definition$getMaxDamage(ItemStack stack) {
        return getTotalMaxDurability(stack);
    }

    default void definition$setDamage(ItemStack stack, int durability) {
        NBTTagCompound toolTag = getToolTag(stack);
        toolTag.setInteger(DURABILITY_KEY, durability);
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return isElectric() ? ElectricStats.createElectricItem(0L, getElectricTier()).createProvider(stack) : null;
    }

    default EnumActionResult definition$onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (placeTorchRoutine(player, world, pos, hand, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    default ActionResult<ItemStack> definition$onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            // TODO: relocate to keybind action when keybind PR happens
            if (getMaxAoEDefinition(stack) != AoEDefinition.none()) {
                PlayerInventoryHolder.openHandItemUI(player, hand);
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    // Client-side methods
    @SideOnly(Side.CLIENT)
    default void definition$addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.WHITE + "Behaves like: " + stack.getItem().getToolClasses(stack).stream().map(StringUtils::capitalize).collect(Collectors.joining(", ")));
        NBTTagCompound behavioursTag = getBehavioursTag(stack);
        List<String> behaviours = new ArrayList<>();
        if (behavioursTag.getBoolean(HARVEST_ICE_KEY)) {
            behaviours.add(" " + TextFormatting.AQUA + "Silk Harvest Ice");
        }
        if (behavioursTag.getBoolean(TORCH_PLACING_KEY)) {
            behaviours.add(" " + TextFormatting.YELLOW + "Torch Placing");
        }
        if (behavioursTag.getBoolean(TREE_FELLING_KEY)) {
            behaviours.add(" " + TextFormatting.DARK_RED + "Tree Felling");
        }
        if (behavioursTag.getBoolean(DISABLE_SHIELDS_KEY)) {
            behaviours.add(" " + TextFormatting.GRAY + "Disable Shields");
        }
        if (behavioursTag.getBoolean(RELOCATE_MINED_BLOCKS_KEY)) {
            behaviours.add(" " + TextFormatting.DARK_GREEN + "Relocate Mined Blocks");
        }
        AoEDefinition aoeDefinition = ToolHelper.getAoEDefinition(stack);
        if (aoeDefinition != AoEDefinition.none()) {
            behaviours.add(" " + TextFormatting.DARK_PURPLE + (aoeDefinition.column + 1) + "x" + (aoeDefinition.row + 1) + "x" + (aoeDefinition.layer + 1) + " AoE Mining");
        }
        if (!behaviours.isEmpty()) {
            tooltip.add(TextFormatting.YELLOW + "Behaviours:");
            tooltip.addAll(behaviours);
        }
    }

    @SideOnly(Side.CLIENT)
    default void renderElectricBar(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        if (isElectric()) {
            long maxCharge = getMaxCharge(stack);
            if (maxCharge != -1L) {
                long charge = getCharge(stack);
                if (charge < maxCharge) {
                    double level = (double) charge / (double) maxCharge;
                    boolean showDurability = stack.getItem().showDurabilityBar(stack);
                    ToolChargeBarRenderer.render(level, xPosition, yPosition, showDurability ? 2 : 0, true);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

    @SideOnly(Side.CLIENT)
    default String getModelPath() {
        return getDomain() + ":" + "tools/" + getId();
    }

    @SideOnly(Side.CLIENT)
    default ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(getModelPath(), "inventory");
    }

    // Sound Playing
    default void playCraftingSound(EntityPlayer player) {
        if (ConfigHolder.client.toolCraftingSounds && getSound() != null) {
            if (!player.getCooldownTracker().hasCooldown(get())) {
                player.getCooldownTracker().setCooldown(get(), 10);
                player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
            }
        }
    }

    default void playSound(EntityPlayer player) {
        if (ConfigHolder.client.toolUseSounds && getSound() != null) {
            player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    default ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        NBTTagCompound tag = getToolTag(holder.getCurrentItem());
        AoEDefinition defaultDefinition = getMaxAoEDefinition(holder.getCurrentItem());
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 120, 80)
                .label(10, 10, "Column")
                .label(52, 10, "Row")
                .label(82, 10, "Layer")
                .widget(new ClickButtonWidget(15, 24, 20, 20, "+", data -> {
                    AoEDefinition.increaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(15, 44, 20, 20, "-", data -> {
                    AoEDefinition.decreaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 24, 20, 20, "+", data -> {
                    AoEDefinition.increaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 44, 20, 20, "-", data -> {
                    AoEDefinition.decreaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 24, 20, 20, "+", data -> {
                    AoEDefinition.increaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 44, 20, 20, "-", data -> {
                    AoEDefinition.decreaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new DynamicLabelWidget(23, 65, () -> Integer.toString(AoEDefinition.getColumn(getToolTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(58, 65, () -> Integer.toString(AoEDefinition.getRow(getToolTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(93, 65, () -> Integer.toString(AoEDefinition.getLayer(getToolTag(holder.getCurrentItem()), defaultDefinition))))
                .build(holder, entityPlayer);
    }

    // Extended Interfaces

    // IAEWrench
    /**
     * Check if the wrench can be used.
     *
     * @param player wrenching player
     * @param pos    of block.
     * @return true if wrench can be used
     */
    @Override
    default boolean canWrench(ItemStack wrench, EntityPlayer player, BlockPos pos) {
        return get().getToolClasses(wrench).contains("wrench");
    }

    // IToolWrench
    /*** Called to ensure that the wrench can be used.
     *
     * @param player - The player doing the wrenching
     * @param hand - Which hand was holding the wrench
     * @param wrench - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched
     *
     * @return true if wrenching is allowed, false if not */
    @Override
    default boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        return get().getToolClasses(wrench).contains("wrench");
    }

    /*** Callback after the wrench has been used. This can be used to decrease durability or for other purposes.
     *
     * @param player - The player doing the wrenching
     * @param hand - Which hand was holding the wrench
     * @param wrench - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched */
    @Override
    default void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) { }

    // IToolHammer
    @Override
    default boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
        return get().getToolClasses(item).contains("wrench");
    }

    @Override
    default boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
        return get().getToolClasses(item).contains("wrench");
    }

    @Override
    default void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) { }

    @Override
    default void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) { }

    // ITool
    @Override
    default boolean canUse(@Nonnull EnumHand hand, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
        return get().getToolClasses(player.getHeldItem(hand)).contains("wrench");
    }

    @Override
    default void used(@Nonnull EnumHand hand, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) { }

    // IHideFacades
    @Override
    default boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
        return get().getToolClasses(stack).contains("wrench");
    }

    // IToolGrafter
    /**
     * Called by leaves to determine the increase in sapling droprate.
     *
     * @param stack ItemStack containing the grafter.
     * @param world Minecraft world the player and the target block inhabit.
     * @param pos   Coordinate of the broken leaf block.
     * @return Float representing the factor the usual drop chance is to be multiplied by.
     */
    @Override
    default float getSaplingModifier(ItemStack stack, World world, EntityPlayer player, BlockPos pos) {
        return get().getToolClasses(stack).contains("grafter") ? 100F : 1.0F;
    }

    // IOverlayRenderAware
    @Override
    default void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        renderElectricBar(stack, xPosition, yPosition);
    }
}
