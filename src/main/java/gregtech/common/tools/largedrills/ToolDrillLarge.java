package gregtech.common.tools.largedrills;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.ModeSwitchBehavior;
import gregtech.common.tools.ToolBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ToolDrillLarge<E extends Enum<E> & IDrillMode> extends ToolBase {

    abstract ModeSwitchBehavior<E> getModeSwitchBehavior();

    abstract int getTier();

    abstract MetaItem.MetaValueItem getPowerUnit();

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return getTier();
    }

    @Override
    public int getToolDamagePerDropConversion(ItemStack stack) {
        return getTier() * 2;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return (int) Math.pow(2, getTier() * 2 - 1);
    }

    @Override
    public int getToolDamagePerEntityAttack(ItemStack stack) {
        return getTier() + 2;
    }

    @Override
    public int getBaseQuality(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return getTier();
    }

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment.type.canEnchantItem(Items.IRON_PICKAXE);
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        E drillMode = this.getModeSwitchBehavior().getModeFromItemStack(stack);
        return drillMode.getDigSpeedMultiplier();
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 29.0F;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && (tool.equals("pickaxe") || tool.equals("shovel"))) ||
                block.getMaterial() == Material.ROCK ||
                block.getMaterial() == Material.IRON ||
                block.getMaterial() == Material.ANVIL ||
                block.getMaterial() == Material.SAND ||
                block.getMaterial() == Material.GRASS ||
                block.getMaterial() == Material.GROUND ||
                block.getMaterial() == Material.SNOW ||
                block.getMaterial() == Material.CLAY ||
                block.getMaterial() == Material.GLASS;
    }

    public boolean canAOEMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && (tool.equals("hammer") || tool.equals("pickaxe"))) ||
                block.getMaterial() == Material.ROCK ||
                block.getMaterial() == Material.GLASS ||
                block.getMaterial() == Material.ICE ||
                block.getMaterial() == Material.PACKED_ICE;
    }

    @Override
    public void onStatsAddedToTool(MetaItem.MetaValueItem metaValueItem) {
        metaValueItem.addComponents(this.getModeSwitchBehavior());
    }

    @Override
    public List<BlockPos> getAOEBlocks(ItemStack itemStack, EntityPlayer player, RayTraceResult rayTraceResult) {
        if (player.isCreative()) {
            return Collections.emptyList();
        }
        ArrayList<BlockPos> result = new ArrayList<>();
        BlockPos pos = rayTraceResult.getBlockPos();
        IDrillMode drillMode;
        if (player.isSneaking() || !canAOEMineBlock(player.world.getBlockState(pos), itemStack)) {
            drillMode = IDrillMode.getSingleBlock();
        } else {
            drillMode = this.getModeSwitchBehavior().getModeFromItemStack(itemStack);
        }
        int sizeExtension = (drillMode.getCubeSize() - 1) / 2;
        for (int x = -sizeExtension; x <= sizeExtension; x++) {
            for (int y = -sizeExtension; y <= sizeExtension; y++) {
                for (int z = -sizeExtension; z <= sizeExtension; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; //that's handled separately
                    BlockPos offsetPos = pos.add(x, y, z);
                    IBlockState blockState = player.world.getBlockState(offsetPos);
                    if (itemStack.canHarvestBlock(blockState)) {
                        result.add(offsetPos);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer)) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            int damagePerBlockBreak = getToolDamagePerBlockBreak(stack);
            IDrillMode drillMode;
            if (entityPlayer.isSneaking()  || !canAOEMineBlock(entityPlayer.world.getBlockState(pos), stack)) {
                drillMode = IDrillMode.getSingleBlock();
            } else {
                drillMode = this.getModeSwitchBehavior().getModeFromItemStack(stack);
            }
            int sizeExtension = (drillMode.getCubeSize() - 1) / 2;
            for (int x = -sizeExtension; x <= sizeExtension; x++) {
                for (int y = -sizeExtension; y <= sizeExtension; y++) {
                    for (int z = -sizeExtension; z <= sizeExtension; z++) {
                        if (x == 0 && y == 0 && z == 0) continue; //that's handled separately
                        BlockPos offsetPos = pos.add(x, y, z);
                        IBlockState blockState = world.getBlockState(offsetPos);
                        if (world.isBlockModifiable(entityPlayer, offsetPos) &&
                                blockState.getBlock().canHarvestBlock(world, offsetPos, entityPlayer) &&
                                blockState.getPlayerRelativeBlockHardness(entityPlayer, world, offsetPos) > 0.0f &&
                                stack.canHarvestBlock(blockState)) {
                            GTUtility.harvestBlock(world, offsetPos, entityPlayer);
                            ((ToolMetaItem) stack.getItem()).damageItem(stack, damagePerBlockBreak, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getBrokenStack(ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return getPowerUnit().getChargedStackWithOverride(electricItem);
    }

}
