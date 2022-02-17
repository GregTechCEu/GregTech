package gregtech.api.items.toolitem;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import crazypants.enderio.api.tool.ITool;
import forestry.api.arboriculture.IToolGrafter;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.ThreadContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

import static gregtech.api.items.armor.IArmorLogic.*;

/**
 * Backing of every variation of a GT Tool
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = GTValues.MODID_APPENG, iface = "appeng.api.implementations.items.IAEWrench"),
        @Optional.Interface(modid = GTValues.MODID_BC, iface = "buildcraft.api.tools.IToolWrench"),
        @Optional.Interface(modid = GTValues.MODID_COFH, iface = "cofh.api.item.IToolHammer"),
        @Optional.Interface(modid = GTValues.MODID_EIO, iface = "crazypants.enderio.api.tool.ITool"),
        @Optional.Interface(modid = GTValues.MODID_FR, iface = "forestry.api.arboriculture.IToolGrafter")})
public interface IGTTool extends IAEWrench, IToolWrench, IToolHammer, ITool, IToolGrafter {

    String getDomain();

    String getId();

    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEvent getSound();

    Set<Block> getEffectiveBlocks();

    Set<String> getOreDictNames();

    AoEDefinition getAoEDefinition();

    default Item get() {
        return (Item) this;
    }

    default ItemStack get(Material material) {
        ItemStack stack = new ItemStack(get());
        NBTTagCompound toolTag = getToolTag(stack);
        toolTag.setString("Material", material.toString());
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
        toolTag.setInteger("MaxDurability", toolProperty.getToolDurability());
        toolTag.setInteger("Durability", 0);
        EnchantmentHelper.setEnchantments(toolProperty.getEnchantments(), stack);
        return stack;
    }

    default NBTTagCompound getToolTag(ItemStack stack) {
        return stack.getOrCreateSubCompound("GT.Tools");
    }

    default Material getToolMaterial(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        String string = toolTag.getString("Material");
        Material material = GregTechAPI.MaterialRegistry.get(string);
        if (material == null) {
            GTLog.logger.error("Attempt to get {} as a tool material, but material does not exist. Using Neutronium instead.", string);
            material = Materials.Neutronium;
        }
        return material;
    }

    default ToolProperty getToolProperty(ItemStack stack) {
        Material material = getToolMaterial(stack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        if (property == null) {
            GTLog.logger.error("Tool property for {} does not exist. Using Neutronium's tool property instead.", material.getId());
            property = Materials.Neutronium.getProperty(PropertyKey.TOOL);
        }
        return property;
    }

    default DustProperty getDustProperty(ItemStack stack) {
        Material material = getToolMaterial(stack);
        DustProperty property = material.getProperty(PropertyKey.DUST);
        if (property == null) {
            GTLog.logger.error("Dust property for {} does not exist. Using Neutronium's dust property instead.", material.getId());
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

    default Object2IntMap<Enchantment> getMaterialEnchantments(ItemStack stack) {
        return getToolProperty(stack).getEnchantments();
    }

    default float getTotalToolSpeed(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("ToolSpeed", Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat("ToolSpeed");
        }
        float toolSpeed = getMaterialToolSpeed(stack) + getToolStats().getBaseEfficiency(stack);
        toolTag.setFloat("ToolSpeed", toolSpeed);
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("AttackDamage", Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat("AttackDamage");
        }
        float attackDamage = getMaterialAttackDamage(stack) + getToolStats().getBaseDamage(stack);
        toolTag.setFloat("AttackDamage", attackDamage);
        return attackDamage;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("MaxDurability", Constants.NBT.TAG_INT)) {
            return toolTag.getInteger("MaxDurability");
        }
        int maxDurability = getToolProperty(stack).getToolDurability();
        toolTag.setInteger("MaxDurability", maxDurability);
        return maxDurability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("Enchantability", Constants.NBT.TAG_INT)) {
            return toolTag.getInteger("Enchantability");
        }
        int enchantability = getMaterialEnchantability(stack);
        toolTag.setInteger("Enchantability", enchantability);
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("HarvestLevel", Constants.NBT.TAG_INT)) {
            return toolTag.getInteger("HarvestLevel");
        }
        int harvestLevel = getMaterialHarvestLevel(stack) + getToolStats().getBaseQuality(stack);
        toolTag.setInteger("HarvestLevel", harvestLevel);
        return harvestLevel;
    }

    @SideOnly(Side.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
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
        damageItem(stack, attacker, getToolStats().getToolDamagePerEntityAttack(stack));
        return true;
    }

    default boolean definition$onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote) {
            if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
                if (stack.isEmpty()) {
                    return true;
                }
            }
            if (!entityLiving.isSneaking() && entityLiving instanceof EntityPlayerMP && !ThreadContext.containsKey("GT_AoE_Breaking")) { // Disable AoE when sneaking
                EntityPlayerMP serverPlayer = (EntityPlayerMP) entityLiving;
                ThreadContext.put("GT_AoE_Breaking", "");
                for (BlockPos aoePos : getHarvestableBlocks(worldIn, serverPlayer)) {
                    serverPlayer.interactionManager.tryHarvestBlock(aoePos);
                    if (stack.isEmpty()) {
                        ThreadContext.remove("GT_AoE_Breaking");
                        return true;
                    }
                }
                ThreadContext.remove("GT_AoE_Breaking");
            }
        }
        return true;
    }

    default boolean definition$getIsRepairable(ItemStack toRepair, ItemStack repair) {
        MaterialStack repairMaterialStack = OreDictUnifier.getMaterial(repair);
        return repairMaterialStack.material == getToolMaterial(toRepair);
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
        return get().getToolClasses(stack).contains("axe");
    }

    default boolean definition$doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    default boolean definition$hasContainerItem(ItemStack stack) {
        return true;
    }

    default ItemStack definition$getContainerItem(ItemStack stack) {
        int damage = getToolStats().getToolDamagePerContainerCraft(stack);
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

    /**
     * Damages the tool appropriately
     *
     * @param stack  Tool ItemStack
     * @param entity Entity that has damaged this ItemStack
     * @param damage Damage the ItemStack will be taking
     */
    default void damageItem(ItemStack stack, EntityLivingBase entity, int damage) {
        if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode) {
            if (isElectric()) {
                int electricDamage = damage * ConfigHolder.machines.energyUsageMultiplier;
                IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem != null) {
                    long newCharge = electricItem.getCharge() - electricDamage;
                    electricItem.discharge(electricDamage, getElectricTier(), true, false, false);
                    if (newCharge > 0 && entity.getRNG().nextInt(100) > ConfigHolder.tools.rngDamageElectricTools) {
                        return;
                    }
                } else {
                    throw new IllegalStateException("Electric tool does not have an attached electric item capability.");
                }
            }
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
            int negated = 0;
            for (int k = 0; unbreakingLevel > 0 && k < damage; k++) {
                if (EnchantmentDurability.negateDamage(stack, unbreakingLevel, entity.getRNG())) {
                    negated++;
                }
            }
            damage -= negated;
            if (damage <= 0) {
                return;
            }
            int newDurability = definition$getDamage(stack) + damage;
            if (entity instanceof EntityPlayerMP) {
                CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger((EntityPlayerMP) entity, stack, newDurability);
            }
            definition$setDamage(stack, newDurability);
            if (newDurability > definition$getMaxDamage(stack)) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityplayer = (EntityPlayer) entity;
                    entityplayer.addStat(StatList.getObjectBreakStats(stack.getItem()));
                }
                entity.renderBrokenItemStack(stack);
                stack.shrink(1);
            }
        }
    }

    default boolean definition$isDamaged(ItemStack stack) {
        return definition$getDamage(stack) > 0;
    }

    default int definition$getDamage(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey("Durability", Constants.NBT.TAG_INT)) {
            return toolTag.getInteger("Durability");
        }
        toolTag.setInteger("Durability", 0);
        return 0;
    }

    default int definition$getMaxDamage(ItemStack stack) {
        return getTotalMaxDurability(stack);
    }

    default void definition$setDamage(ItemStack stack, int durability) {
        NBTTagCompound toolTag = getToolTag(stack);
        toolTag.setInteger("Durability", durability);
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

    // AoE
    default Set<BlockPos> getHarvestableBlocks(@Nonnull World world, @Nonnull EntityPlayer player, RayTraceResult rayTraceResult) {
        if (getAoEDefinition() == AoEDefinition.of()) {
            return Collections.emptySet();
        }
        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.sideHit != null) {
            int height = getAoEDefinition().height;
            int width = getAoEDefinition().width;
            int depth = getAoEDefinition().depth;
            EnumFacing playerFacing = player.getHorizontalFacing();
            EnumFacing.Axis playerAxis = playerFacing.getAxis();
            EnumFacing.Axis sideHitAxis = rayTraceResult.sideHit.getAxis();
            EnumFacing.AxisDirection sideHitAxisDir = rayTraceResult.sideHit.getAxisDirection();
            ImmutableSet.Builder<BlockPos> validPositions = ImmutableSet.builder();
            if (sideHitAxis.isVertical()) {
                boolean isX = playerAxis == EnumFacing.Axis.X;
                boolean isDown = sideHitAxisDir == EnumFacing.AxisDirection.NEGATIVE;
                for (int y = 0; y <= depth; y++) {
                    for (int x = isX ? -width : -height; x <= (isX ? width : height); x++) {
                        for (int z = isX ? -height : -width; z <= (isX ? height : width); z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = rayTraceResult.getBlockPos().add(x, isDown ? y : -y, z);
                                IBlockState state = world.getBlockState(pos);
                                if (state.getBlock().canHarvestBlock(world, pos, player)) {
                                    ItemStack stack = player.getHeldItemMainhand();
                                    if (get().getToolClasses(stack).stream().anyMatch(s -> state.getBlock().isToolEffective(s, state))) {
                                        validPositions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                boolean isX = sideHitAxis == EnumFacing.Axis.X;
                boolean isNegative = sideHitAxisDir == EnumFacing.AxisDirection.NEGATIVE;
                for (int x = 0; x <= depth; x++) {
                    for (int y = -width; y <= width; y++) {
                        for (int z = -height; z <= height; z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = rayTraceResult.getBlockPos().add(isX ? (isNegative ? x : -x) : (isNegative ? z : -z), y, isX ? (isNegative ? z : -z) : (isNegative ? x : -x));
                                IBlockState state = world.getBlockState(pos);
                                if (state.getBlock().canHarvestBlock(world, pos, player)) {
                                    ItemStack stack = player.getHeldItemMainhand();
                                    if (get().getToolClasses(stack).stream().anyMatch(s -> state.getBlock().isToolEffective(s, state))) {
                                        validPositions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return validPositions.build();
        }
        return Collections.emptySet();
    }

    default Set<BlockPos> getHarvestableBlocks(@Nonnull World world, @Nonnull EntityPlayer player) {
        if (getAoEDefinition() == AoEDefinition.of()) {
            return Collections.emptySet();
        }
        Vec3d lookPos = player.getPositionEyes(1F);
        Vec3d rotation = player.getLook(1);
        Vec3d realLookPos = lookPos.add(rotation.x * 5, rotation.y * 5, rotation.z * 5);
        RayTraceResult rayTraceResult = world.rayTraceBlocks(lookPos, realLookPos);
        return getHarvestableBlocks(world, player, rayTraceResult);
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

}
