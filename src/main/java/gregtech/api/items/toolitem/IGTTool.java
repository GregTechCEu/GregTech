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
import gregtech.api.capability.impl.CombinedCapabilityProvider;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.client.utils.ToolChargeBarRenderer;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentMending;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static gregtech.api.items.armor.IArmorLogic.ATTACK_DAMAGE_MODIFIER;
import static gregtech.api.items.armor.IArmorLogic.ATTACK_SPEED_MODIFIER;
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
        @Optional.Interface(modid = GTValues.MODID_ECORE, iface = "com.enderio.core.common.interfaces.IOverlayRenderAware")})
public interface IGTTool extends ItemUIFactory, IAEWrench, IToolWrench, IToolHammer, ITool, IToolGrafter, IOverlayRenderAware {

    /**
     * @return the modid of the tool
     */
    String getDomain();

    /**
     * @return the name of the tool
     */
    String getId();

    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEvent getSound();

    boolean playSoundOnBlockDestroy();

    @Nullable
    String getOreDictName();

    @Nullable
    Supplier<ItemStack> getMarkerItem();

    default Item get() {
        return (Item) this;
    }

    default ItemStack getRaw() {
        ItemStack stack = new ItemStack(get());
        getToolTag(stack);
        getBehavioursTag(stack);
        return stack;
    }

    default ItemStack get(Material material) {
        ItemStack stack = new ItemStack(get());
        NBTTagCompound toolTag = getToolTag(stack);

        stack.getTagCompound().setBoolean(DISALLOW_CONTAINER_ITEM_KEY, false);

        // don't show the normal vanilla damage and attack speed tooltips,
        // we handle those ourselves
        // stack.getTagCompound().setInteger(HIDE_FLAGS, 2);

        // Set Material
        toolTag.setString(MATERIAL_KEY, material.toString());

        // Set other tool stats (durability)
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
        toolTag.setInteger(MAX_DURABILITY_KEY, toolProperty.getToolDurability());
        toolTag.setInteger(DURABILITY_KEY, 0);
        if (toolProperty.getUnbreakable()) {
            stack.getTagCompound().setBoolean(UNBREAKABLE_KEY, true);
        }

        // Set material enchantments
        toolProperty.getEnchantments().forEach((enchantment, level) -> {
            if (stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
                stack.addEnchantment(enchantment, level);
            }
        });

        // Set behaviours
        NBTTagCompound behaviourTag = getBehavioursTag(stack);
        getToolStats().getBehaviors().forEach(behavior -> behavior.addBehaviorNBT(stack, behaviourTag));

        AoESymmetrical aoeDefinition = getToolStats().getAoEDefinition(stack);

        if (aoeDefinition != AoESymmetrical.none()) {
            behaviourTag.setInteger(MAX_AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.setInteger(MAX_AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.setInteger(MAX_AOE_LAYER_KEY, aoeDefinition.layer);
            behaviourTag.setInteger(AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.setInteger(AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.setInteger(AOE_LAYER_KEY, aoeDefinition.layer);
        }

        if (material.hasFlag(MaterialFlags.IS_MAGNETIC)) {
            behaviourTag.setBoolean(RELOCATE_MINED_BLOCKS_KEY, true);
        }

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

    @Nullable
    default ToolProperty getToolProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.TOOL);
    }

    @Nullable
    default DustProperty getDustProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.DUST);
    }

    default float getMaterialToolSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolSpeed();
    }

    default float getMaterialAttackDamage(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolAttackDamage();
    }

    default float getMaterialAttackSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolAttackSpeed();
    }

    default int getMaterialDurability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolDurability();
    }

    default int getMaterialEnchantability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolEnchantability();
    }

    default int getMaterialHarvestLevel(ItemStack stack) {
        DustProperty dustProperty = getDustProperty(stack);
        return dustProperty == null ? 0 : dustProperty.getHarvestLevel();
    }

    default long getMaxCharge(ItemStack stack) {
        if (isElectric()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(MAX_CHARGE_KEY, Constants.NBT.TAG_LONG)) {
                return tag.getLong(MAX_CHARGE_KEY);
            }
        }
        return -1L;
    }

    default long getCharge(ItemStack stack) {
        if (isElectric()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(CHARGE_KEY, Constants.NBT.TAG_LONG)) {
                return tag.getLong(CHARGE_KEY);
            }
        }
        return -1L;
    }

    default float getTotalToolSpeed(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(TOOL_SPEED_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(TOOL_SPEED_KEY);
        }
        float toolSpeed = getToolStats().getEfficiencyMultiplier(stack) * getMaterialToolSpeed(stack) + getToolStats().getBaseEfficiency(stack);
        toolTag.setFloat(TOOL_SPEED_KEY, toolSpeed);
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(ATTACK_DAMAGE_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_DAMAGE_KEY);
        }
        float baseDamage = getToolStats().getBaseDamage(stack);
        float attackDamage = 0;
        // represents a tool that should always have an attack damage value of 0
        if (baseDamage != Float.MIN_VALUE) {
            attackDamage = getMaterialAttackDamage(stack) + baseDamage;
        }
        toolTag.setFloat(ATTACK_DAMAGE_KEY, attackDamage);
        return attackDamage;
    }

    default float getTotalAttackSpeed(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(ATTACK_SPEED_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_SPEED_KEY);
        }
        float attackSpeed = getMaterialAttackSpeed(stack) + getToolStats().getAttackSpeed(stack);
        toolTag.setFloat(ATTACK_SPEED_KEY, attackSpeed);
        return attackSpeed;
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

    default AoESymmetrical getMaxAoEDefinition(ItemStack stack) {
        return AoESymmetrical.readMax(getBehavioursTag(stack));
    }

    default AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.read(getToolTag(stack), getMaxAoEDefinition(stack));
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, IBlockState state) {
        // special case check (mostly for the sword)
        float specialValue = ToolHelper.getDestroySpeed(state, getToolClasses(stack));
        if (specialValue != -1) return specialValue;

        if (ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack))) {
            return getTotalToolSpeed(stack);
        }

        return getToolStats().isToolEffective(state) ? getTotalToolSpeed(stack) : 1.0F;
    }

    default boolean definition$hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        getToolStats().getBehaviors().forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        if (player.world.isRemote) return false;
        getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isSneaking()) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            int result = -1;
            if (isTool(stack, ToolClasses.SHEARS)) {
                result = ToolHelper.shearBlockRoutine(playerMP, stack, pos);
            }
            if (result != 0) {
                // prevent exploits with instantly breakable blocks
                IBlockState state = player.world.getBlockState(pos);
                boolean effective = false;
                for (String type : getToolClasses(stack)) {
                    if (state.getBlock().isToolEffective(type, state)) {
                        effective = true;
                        break;
                    }
                }

                effective |= ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));

                if (effective) {
                    if (areaOfEffectBlockBreakRoutine(stack, playerMP)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            treeFellingRoutine(playerMP, stack, pos);
                            if (playSoundOnBlockDestroy()) playSound(player);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    default boolean definition$onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote) {
            getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockDestroyed(stack, worldIn, state, pos, entityLiving));

            if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
            }
            if (entityLiving instanceof EntityPlayer && playSoundOnBlockDestroy()) {
                // sneaking disables AOE, which means it is okay to play the sound
                // not checking this means the sound will play for every AOE broken block, which is very loud
                if (entityLiving.isSneaking()) {
                    playSound((EntityPlayer) entityLiving);
                }
            }
        }
        return true;
    }

    default boolean definition$getIsRepairable(ItemStack toRepair, ItemStack repair) {
        // full durability tools in the left slot are not repairable
        // this is needed so enchantment merging works when both tools are full durability
        if (toRepair.getItemDamage() == 0) return false;
        if (repair.getItem() instanceof IGTTool) {
            return getToolMaterial(toRepair) == ((IGTTool) repair.getItem()).getToolMaterial(repair);
        }
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(repair);
        if (entry == null || entry.material == null) return false;
        if (entry.material == getToolMaterial(toRepair)) {
            // special case wood to allow Wood Planks
            if (ModHandler.isMaterialWood(entry.material)) {
                return entry.orePrefix == OrePrefix.plank;
            }
            // Gems can use gem and plate, Ingots can use ingot and plate
            if (entry.orePrefix == OrePrefix.plate) {
                return true;
            }
            if (entry.material.hasProperty(PropertyKey.INGOT)) {
                return entry.orePrefix == OrePrefix.ingot;
            }
            if (entry.material.hasProperty(PropertyKey.GEM)) {
                return entry.orePrefix == OrePrefix.gem;
            }
        }
        return false;
    }

    default Multimap<String, AttributeModifier> definition$getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getTotalAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", Math.max(-3.9D, getTotalAttackSpeed(stack)), 0));
        }
        return multimap;
    }

    default int definition$getHarvestLevel(ItemStack stack, String toolClass, @javax.annotation.Nullable net.minecraft.entity.player.EntityPlayer player, @javax.annotation.Nullable IBlockState blockState) {
        return get().getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity, EntityLivingBase attacker) {
        return getToolStats().getBehaviors().stream().anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
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
        stack = stack.copy();
        EntityPlayer player = ForgeHooks.getCraftingPlayer();
        damageItemWhenCrafting(stack, player);
        playCraftingSound(player, stack);
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
        return stack;
    }

    default boolean definition$shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (getCharge(oldStack) != getCharge(newStack)) {
            return slotChanged;
        }
        return !oldStack.equals(newStack);
    }

    default boolean definition$onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        getToolStats().getBehaviors().forEach(behavior -> behavior.onEntitySwing(entityLiving, stack));
        return false;
    }

    default boolean definition$canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return true;
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

    default double definition$getDurabilityForDisplay(ItemStack stack) {
        int damage = stack.getItem().getDamage(stack);
        int maxDamage = stack.getItem().getMaxDamage(stack);
        if (damage == 0) return 1.0;
        return (double) (maxDamage - damage) / (double) maxDamage;
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        if (isElectric()) {
            providers.add(ElectricStats.createElectricItem(0L, getElectricTier()).createProvider(stack));
        }
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            ICapabilityProvider behaviorProvider = behavior.createProvider(stack, nbt);
            if (behaviorProvider != null) {
                providers.add(behaviorProvider);
            }
        }
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
    }

    default EnumActionResult definition$onItemUseFirst(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUseFirst(player, world, pos, facing, hitX, hitY, hitZ, hand) == EnumActionResult.SUCCESS)  {
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    default EnumActionResult definition$onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS)  {
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    default ActionResult<ItemStack> definition$onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            // TODO: relocate to keybind action when keybind PR happens
            if (player.isSneaking() && getMaxAoEDefinition(stack) != AoESymmetrical.none()) {
                PlayerInventoryHolder.openHandItemUI(player, hand);
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }

        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemRightClick(world, player, hand).getType() == EnumActionResult.SUCCESS) {
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    default void definition$getSubItems(@Nonnull NonNullList<ItemStack> items) {
        if (getMarkerItem() != null) {
            items.add(getMarkerItem().get());
        } else if (isElectric()) {
            items.add(get(Materials.Iron, Integer.MAX_VALUE));
        } else {
            items.add(get(Materials.Iron));
        }
    }

    // Client-side methods
    @SideOnly(Side.CLIENT)
    default void definition$addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, ITooltipFlag flag) {
        if (this.isElectric()) {
            tooltip.add(I18n.format("metaitem.generic.electric_item.tooltip",
                    getCharge(stack),
                    getMaxCharge(stack),
                    GTValues.VNF[getElectricTier()]));
            tooltip.add("");
            tooltip.add(I18n.format("item.gt.tool.replace_tool_head"));
        }

        tooltip.add(I18n.format("item.gt.tool.behaves_like", stack.getItem().getToolClasses(stack).stream()
                .map(s -> s.replaceAll("_", " "))
                .map(WordUtils::capitalize)
                .collect(Collectors.joining(", "))));

        NBTTagCompound behavioursTag = getBehavioursTag(stack);
        List<String> behaviors = new ArrayList<>();
        if (behavioursTag.getBoolean(RELOCATE_MINED_BLOCKS_KEY)) {
            behaviors.add(" " + I18n.format("item.gt.tool.behavior.relocate_mining"));
        }
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);
        if (aoeDefinition != AoESymmetrical.none()) {
            behaviors.add(" " + I18n.format("item.gt.tool.behavior.aoe_mining", aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        getToolStats().getBehaviors().forEach(behavior -> behavior.addInformation(stack, world, behaviors, flag));

        if (!behaviors.isEmpty()) {
            tooltip.add(I18n.format("item.gt.tool.behavior.behaviors"));
            tooltip.addAll(behaviors);
        }
    }

    default boolean definition$canApplyAtEnchantingTable(@Nonnull ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) return false;

        // special case enchants from other mods
        switch (enchantment.getName()) {
            case "enchantment.cofhcore.smashing":
                // block cofhcore smashing enchant from all tools
                return false;
            case "enchantment.autosmelt": // endercore
            case "enchantment.cofhcore.smelting": // cofhcore
            case "enchantment.as.smelting": // astral sorcery
                // block autosmelt enchants from AoE and Tree-Felling tools
                return getToolStats().getAoEDefinition(stack) == AoESymmetrical.none() && !getBehavioursTag(stack).hasKey(TREE_FELLING_KEY);
        }

        // Block Mending and Unbreaking on Electric tools
        if (isElectric() && (enchantment instanceof EnchantmentMending || enchantment instanceof EnchantmentDurability)) {
            return false;
        }

        if (enchantment.type == null) return true;
        // bypass EnumEnchantmentType#canEnchantItem and define custom stack-aware logic.
        // the Minecraft method takes an Item, and does not respect NBT nor meta.
        switch (enchantment.type) {
            case DIGGER: {
                return getToolStats().isSuitableForBlockBreak(stack);
            }
            case WEAPON: {
                return getToolStats().isSuitableForAttacking(stack);
            }
            case BREAKABLE:
                return stack.getTagCompound() != null && !stack.getTagCompound().getBoolean(UNBREAKABLE_KEY);
            case ALL: {
                return true;
            }
        }

        ToolProperty property = getToolProperty(stack);
        if (property == null) return false;

        // Check for any special enchantments specified by the material of this Tool
        if (!property.getEnchantments().isEmpty() && property.getEnchantments().containsKey(enchantment)) {
            return true;
        }

        // Check for any additional Enchantment Types added in the builder
        return getToolStats().isEnchantable(stack) && getToolStats().canApplyEnchantment(stack, enchantment);
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
    default void playCraftingSound(EntityPlayer player, ItemStack stack) {
        if (ConfigHolder.client.toolCraftingSounds && getSound() != null) {
            if (canPlaySound(stack)) {
                setLastCraftingSoundTime(stack);
                player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
            }
        }
    }

    default void setLastCraftingSoundTime(ItemStack stack) {
        getToolTag(stack).setInteger(LAST_CRAFTING_USE_KEY, (int) System.currentTimeMillis());
    }

    default boolean canPlaySound(ItemStack stack) {
        return Math.abs((int) System.currentTimeMillis() - getToolTag(stack).getInteger(LAST_CRAFTING_USE_KEY)) > 1000;
    }

    default void playSound(EntityPlayer player) {
        if (ConfigHolder.client.toolUseSounds && getSound() != null) {
            player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    default ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        NBTTagCompound tag = getBehavioursTag(holder.getCurrentItem());
        AoESymmetrical defaultDefinition = getMaxAoEDefinition(holder.getCurrentItem());
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 120, 80)
                .label(6, 10, "item.gt.tool.aoe.columns")
                .label(49, 10, "item.gt.tool.aoe.rows")
                .label(79, 10, "item.gt.tool.aoe.layers")
                .widget(new ClickButtonWidget(15, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(15, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new DynamicLabelWidget(23, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getColumn(getBehavioursTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(58, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getRow(getBehavioursTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(93, 65, () ->
                        Integer.toString(1 + AoESymmetrical.getLayer(getBehavioursTag(holder.getCurrentItem()), defaultDefinition))))
                .build(holder, entityPlayer);
    }

    Set<String> getToolClasses(ItemStack stack);

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
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
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
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
    }

    /*** Callback after the wrench has been used. This can be used to decrease durability or for other purposes.
     *
     * @param player - The player doing the wrenching
     * @param hand - Which hand was holding the wrench
     * @param wrench - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched */
    @Override
    default void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IToolHammer
    @Override
    default boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
        damageItem(item, user);
        if (user instanceof EntityPlayer) {
            playSound((EntityPlayer) user);
        }
    }

    @Override
    default void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {
        damageItem(item, user);
    }

    // ITool
    @Override
    default boolean canUse(@Nonnull EnumHand hand, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
        return get().getToolClasses(player.getHeldItem(hand)).contains(ToolClasses.WRENCH);
    }

    @Override
    default void used(@Nonnull EnumHand hand, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IHideFacades
    @Override
    default boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
        return get().getToolClasses(stack).contains(ToolClasses.WRENCH);
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
        return getToolClasses(stack).contains(ToolClasses.GRAFTER) ? 100F : 1.0F;
    }

    // IOverlayRenderAware
    @Override
    default void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        ToolChargeBarRenderer.renderBarsTool(this, stack, xPosition, yPosition);
    }
}
