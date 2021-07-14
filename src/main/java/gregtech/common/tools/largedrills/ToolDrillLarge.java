package gregtech.common.tools.largedrills;

import com.google.common.collect.Lists;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.util.DirectionHelper;
import gregtech.api.util.GTUtility;
import gregtech.common.items.behaviors.ModeSwitchBehavior;
import gregtech.common.tools.ToolBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        BlockPos pos = rayTraceResult.getBlockPos();
        if (player.isSneaking() || !canAOEMineBlock(player.world.getBlockState(pos), itemStack))
            return Collections.emptyList();

        IDrillMode drillMode = this.getModeSwitchBehavior().getModeFromItemStack(itemStack);
        return getAOEBlocks(drillMode.getCubeSize(), player, pos);
    }

    private List<BlockPos> getAOEBlocks(int max, EntityPlayer player, BlockPos hitPos) {
        Vec3d lookVec = player.getLookVec();
        EnumFacing facing = EnumFacing.getFacingFromVector((float) lookVec.x, (float) lookVec.y, (float) lookVec.z);
        BlockPos corner = findCorner(max, hitPos, player, facing);
        BlockPos oppositeCorner = findOppositeCorner(max, corner, player);

        List<BlockPos> posList = Lists.newArrayList(BlockPos.getAllInBox(corner, oppositeCorner));
        return posList.stream()
                // Remove air blocks from Render
                .filter(pos -> {
                    IBlockState state = player.world.getBlockState(pos);
                    return !state.getBlock().isAir(state, player.world, pos);
                })
                // Remove the BlockPos the player is looking at
                .filter(pos -> !pos.equals(hitPos))
                .collect(Collectors.toList());
    }

    /**
     * Returns the relative bottom left closest corner of the possible mining area for the drill.
     */
    private static BlockPos findCorner(int max, BlockPos startPos, EntityPlayer player, EnumFacing facing) {
        Vec3i leftVec = DirectionHelper.getRelativeLeft(player).getDirectionVec();
        Vec3i downVec = DirectionHelper.getRelativeDown(player).getDirectionVec();
        switch (facing) {
            case UP:
            case DOWN:
                // treat up and down as standard cube. just acquire leftmost corner, ignoring floor level
                return startPos
                        .add(DirectionHelper.multiplyVec(leftVec, max / 2))
                        .add(DirectionHelper.multiplyVec(downVec, max / 2));
            default:
                // try to find lowest pos
                Vec3i towardsVec = DirectionHelper.getRelativeForward(player).getDirectionVec();

                // Find the relative downwards offset
                for (int i = 1; i <= max; i++) {
                    BlockPos currentPos = startPos.add(DirectionHelper.multiplyVec(downVec, i));

                    BlockPos forwardPos = currentPos.add(towardsVec);
                    IBlockState state = player.world.getBlockState(forwardPos);
                    if (!state.getBlock().isAir(state, player.world, forwardPos)) {
                        startPos = currentPos;
                        break;
                    }
                }

                // Find the relative leftmost BlockPos
                return startPos.add(DirectionHelper.multiplyVec(leftVec, max / 2));
        }
    }

    private static BlockPos findOppositeCorner(int max, BlockPos corner, EntityPlayer player) {
        Vec3i rightVec = DirectionHelper.getRelativeRight(player).getDirectionVec();
        Vec3i forwardVec = DirectionHelper.getRelativeForward(player).getDirectionVec();
        Vec3i upVec = DirectionHelper.getRelativeUp(player).getDirectionVec();

        return corner
                .add(DirectionHelper.multiplyVec(rightVec, max - 1))
                .add(DirectionHelper.multiplyVec(forwardVec, max - 1))
                .add(DirectionHelper.multiplyVec(upVec, max - 1));
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
            List<BlockPos> blocksToBreak = getAOEBlocks(drillMode.getCubeSize(), entityPlayer, pos);
            blocksToBreak.forEach(currentPos -> {
                IBlockState blockState = world.getBlockState(currentPos);
                if (world.isBlockModifiable(entityPlayer, currentPos) &&
                        blockState.getBlock().canHarvestBlock(world, currentPos, entityPlayer) &&
                        blockState.getPlayerRelativeBlockHardness(entityPlayer, world, currentPos) > 0.0f &&
                        stack.canHarvestBlock(blockState)) {
                    GTUtility.harvestBlock(world, currentPos, entityPlayer);
                    ((ToolMetaItem<?>) stack.getItem()).damageItem(stack, damagePerBlockBreak, false);
                }
            });
            /*
            int sizeExtension = (drillMode.getCubeSize() - 1) / 2;
            for (int x = -sizeExtension; x <= sizeExtension; x++) {
                for (int y = -sizeExtension; y <= sizeExtension; y++) {
                    for (int z = -sizeExtension; z <= sizeExtension; z++) {
                        if (x == 0 && y == 0 && z == 0) continue; //that's handled separately

                    }
                }
            }*/
        }
    }

    @Override
    public ItemStack getBrokenStack(ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return getPowerUnit().getChargedStackWithOverride(electricItem);
    }
}
