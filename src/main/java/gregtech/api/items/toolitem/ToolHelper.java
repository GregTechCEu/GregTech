package gregtech.api.items.toolitem;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.common.ConfigHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Collection of tool related helper methods
 */
public class ToolHelper {

    /**
     * Damages tools in a context where the tool had been used to craft something.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     */
    public static void damageItemWhenCrafting(ItemStack stack, EntityLivingBase entity) {
        int damage = 2;
        if (stack.getItem() instanceof IGTTool) {
            damage = ((IGTTool) stack.getItem()).getToolStats().getToolDamagePerCraft(stack);
        } else {
            if (OreDictUnifier.getOreDictionaryNames(stack).stream().anyMatch(s -> s.startsWith("craftingTool"))) {
                damage = 1;
            }
        }
        damageItem(stack, entity, damage);
    }

    /**
     * Damages tools appropriately.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     * @param damage how much damage the stack will take
     */
    public static void damageItem(ItemStack stack, EntityLivingBase entity, int damage) {
        if (!(stack.getItem() instanceof IGTTool)) {
            stack.damageItem(damage, entity);
        } else {
            if (stack.getTagCompound() != null && stack.getTagCompound().getBoolean("Unbreakable")) {
                return;
            }
            IGTTool tool = (IGTTool) stack.getItem();
            if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode) {
                if (tool.isElectric()) {
                    int electricDamage = damage * ConfigHolder.machines.energyUsageMultiplier;
                    IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                    if (electricItem != null) {
                        electricItem.discharge(electricDamage, tool.getElectricTier(), true, false, false);
                        if (electricItem.getCharge() > 0 && entity.getRNG().nextInt(100) > ConfigHolder.tools.rngDamageElectricTools) {
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
                int newDurability = stack.getItemDamage() + damage;
                if (entity instanceof EntityPlayerMP) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger((EntityPlayerMP) entity, stack, newDurability);
                }
                stack.setItemDamage(newDurability);
                if (newDurability > stack.getMaxDamage()) {
                    if (entity instanceof EntityPlayer) {
                        EntityPlayer entityplayer = (EntityPlayer) entity;
                        entityplayer.addStat(StatList.getObjectBreakStats(stack.getItem()));
                    }
                    entity.renderBrokenItemStack(stack);
                    stack.shrink(1);
                }
            }
        }
    }

    /**
     * Called from {@link net.minecraft.item.Item#onItemUse(EntityPlayer, World, BlockPos, EnumHand, EnumFacing, float, float, float)}
     *
     * Have to be called both sides.
     */
    public static EnumActionResult placeTorchRoutine(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound behaviourTag = IGTTool.getBehaviourTag(stack);
        if (behaviourTag.getBoolean("TorchPlacing")) {
            int cachedTorchSlot;
            ItemStack slotStack;
            if (behaviourTag.getBoolean("TorchPlacing$Slot")) {
                cachedTorchSlot = behaviourTag.getInteger("TorchPlacing$Slot");
                if (cachedTorchSlot < 0) {
                    slotStack = player.inventory.offHandInventory.get(Math.abs(cachedTorchSlot) + 1);
                } else {
                    slotStack = player.inventory.mainInventory.get(cachedTorchSlot);
                }
                if (checkAndPlaceTorch(slotStack, player, world, pos, hand, facing, hitX, hitY, hitZ)) {
                    return EnumActionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
                slotStack = player.inventory.offHandInventory.get(i);
                if (checkAndPlaceTorch(slotStack, player, world, pos, hand, facing, hitX, hitY, hitZ)) {
                    behaviourTag.setInteger("TorchPlacing$Slot", -(i + 1));
                    return EnumActionResult.SUCCESS;
                }
            }
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                slotStack = player.inventory.mainInventory.get(i);
                if (checkAndPlaceTorch(slotStack, player, world, pos, hand, facing, hitX, hitY, hitZ)) {
                    behaviourTag.setInteger("TorchPlacing$Slot", i);
                    return EnumActionResult.SUCCESS;
                }
            }
        }
        return EnumActionResult.PASS;
    }

    private static boolean checkAndPlaceTorch(ItemStack slotStack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!slotStack.isEmpty()) {
            Item slotItem = slotStack.getItem();
            if (slotItem instanceof ItemBlock) {
                ItemBlock slotItemBlock = (ItemBlock) slotItem;
                Block slotBlock = slotItemBlock.getBlock();
                if (slotBlock == Blocks.TORCH || OreDictUnifier.getOreDictionaryNames(slotStack).stream()
                        .anyMatch(s -> s.equals("torch") || s.equals("blockTorch"))) {
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (!block.isReplaceable(world, pos)) {
                        pos = pos.offset(facing);
                    }
                    if (player.canPlayerEdit(pos, facing, slotStack) && world.mayPlace(slotBlock, pos, false, facing, player)) {
                        int i = slotItemBlock.getMetadata(slotStack.getMetadata());
                        IBlockState slotState = slotBlock.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, i, player, hand);
                        if (slotItemBlock.placeBlockAt(slotStack, player, world, pos, facing, hitX, hitY, hitZ, slotState)) {
                            slotState = world.getBlockState(pos);
                            SoundType soundtype = slotState.getBlock().getSoundType(slotState, world, pos, player);
                            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            slotStack.shrink(1);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ToolHelper() { }

}
