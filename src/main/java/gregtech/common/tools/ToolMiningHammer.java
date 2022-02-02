package gregtech.common.tools;

import com.google.common.collect.ImmutableSet;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.util.GTUtility;
import gregtech.common.items.behaviors.ModeSwitchBehavior;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ToolMiningHammer extends ToolBase {

    private static final Set<String> HAMMER_TOOL_CLASSES = ImmutableSet.of("pickaxe", "hammer");

    public enum MiningHammerMode implements ModeSwitchBehavior.ILocalizationKey {
        THREE_BY_THREE("metaitem.drill.mode.three_by_three", 3, 3, 0.75f),
        SINGLE_BLOCK("metaitem.drill.mode.single_block", 1, 1, 3.0f);

        private final String localizationKey;
        private final int verticalSize;
        private final int horizontalSize;
        private final float digSpeedMultiplier;

        MiningHammerMode(String localizationKey, int verticalSize, int horizontalSize, float digSpeedMultiplier) {
            this.localizationKey = localizationKey;
            this.verticalSize = verticalSize;
            this.horizontalSize = horizontalSize;
            this.digSpeedMultiplier = digSpeedMultiplier;
        }

        public int getVerticalSize() {
            return verticalSize;
        }

        public int getHorizontalSize() {
            return horizontalSize;
        }

        public float getDigSpeedMultiplier() {
            return digSpeedMultiplier;
        }

        @Override
        public String getUnlocalizedName() {
            return localizationKey;
        }
    }


    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        if(enchantment.type == null) {
            return false;
        }

        return enchantment.type.canEnchantItem(Items.IRON_PICKAXE) ||
                enchantment.type.canEnchantItem(Items.IRON_SHOVEL);
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 3.0F;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 2.5f;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && HAMMER_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.ROCK ||
                block.getMaterial() == Material.GLASS ||
                block.getMaterial() == Material.ICE ||
                block.getMaterial() == Material.PACKED_ICE;
    }

    @Override
    public List<BlockPos> getAOEBlocks(ItemStack itemStack, EntityPlayer player, RayTraceResult rayTraceResult) {
        if (player.isCreative()) {
            return Collections.emptyList();
        }
        ArrayList<BlockPos> result = new ArrayList<>();
        BlockPos pos = rayTraceResult.getBlockPos();
        MiningHammerMode miningHammerMode;
        if (player.isSneaking()) {
            miningHammerMode = MiningHammerMode.SINGLE_BLOCK;
        } else {
            miningHammerMode = MiningHammerMode.THREE_BY_THREE;
        }
        EnumFacing horizontalFacing = player.getHorizontalFacing();
        int xSizeExtend = (miningHammerMode.getHorizontalSize() - 1) / 2;
        int ySizeExtend = (miningHammerMode.getVerticalSize() - 1) / 2;
        for (int x = -xSizeExtend; x <= xSizeExtend; x++) {
            for (int y = -ySizeExtend; y <= ySizeExtend; y++) {
                //do not check center block - it's handled now
                if (x == 0 && y == 0) continue;
                BlockPos offsetPos = rotate(pos, x, y, rayTraceResult.sideHit, horizontalFacing);
                IBlockState blockState = player.world.getBlockState(offsetPos);
                if (itemStack.canHarvestBlock(blockState)) {
                    result.add(offsetPos);
                }
            }
        }
        return result;
    }

    @Override
    public void onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer)) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            EnumFacing sideHit = ToolUtility.getSideHit(world, pos, entityPlayer);
            int damagePerBlockBreak = getToolDamagePerBlockBreak(stack);
            MiningHammerMode miningHammerMode;
            if (entityPlayer.isSneaking()) {
                miningHammerMode = MiningHammerMode.SINGLE_BLOCK;
            } else {
                miningHammerMode = MiningHammerMode.THREE_BY_THREE;
            }
            EnumFacing horizontalFacing = entity.getHorizontalFacing();
            int xSizeExtend = (miningHammerMode.getHorizontalSize() - 1) / 2;
            int ySizeExtend = (miningHammerMode.getVerticalSize() - 1) / 2;
            for (int x = -xSizeExtend; x <= xSizeExtend; x++) {
                for (int y = -ySizeExtend; y <= ySizeExtend; y++) {
                    //do not check center block - it's handled now
                    if (x == 0 && y == 0) continue;
                    BlockPos offsetPos = rotate(pos, x, y, sideHit, horizontalFacing);
                    IBlockState blockState = world.getBlockState(offsetPos);
                    if (world.isBlockModifiable(entityPlayer, offsetPos) &&
                            blockState.getBlock().canHarvestBlock(world, offsetPos, entityPlayer) &&
                            blockState.getPlayerRelativeBlockHardness(entityPlayer, world, offsetPos) > 0.0f &&
                            stack.canHarvestBlock(blockState)) {
                        GTUtility.harvestBlock(world, offsetPos, entityPlayer);
                        ((ToolMetaItem<?>) stack.getItem()).damageItem(stack, entityPlayer, damagePerBlockBreak, false);
                    }
                }
            }
        }
    }

    private static BlockPos rotate(BlockPos origin, int x, int y, EnumFacing sideHit, EnumFacing horizontalFacing) {
        if (sideHit == null) {
            return BlockPos.ORIGIN;
        }
        switch (sideHit.getAxis()) {
            case X:
                return origin.add(0, y, x);
            case Z:
                return origin.add(x, y, 0);
            case Y:
                return rotateVertical(origin, x, y, horizontalFacing);
            default:
                return BlockPos.ORIGIN;
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private static BlockPos rotateVertical(BlockPos origin, int x, int y, EnumFacing horizontalFacing) {
        switch (horizontalFacing.getAxis()) {
            case X:
                return origin.add(y, 0, x);
            case Z:
                return origin.add(x, 0, y);
            default:
                return BlockPos.ORIGIN;
        }
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return HAMMER_TOOL_CLASSES;
    }
}
