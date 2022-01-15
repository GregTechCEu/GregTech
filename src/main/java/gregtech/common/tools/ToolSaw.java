package gregtech.common.tools;

import com.google.common.collect.ImmutableSet;
import gregtech.api.items.toolitem.ToolMetaItem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

public class ToolSaw extends ToolBase {

    private static final Set<String> SAW_TOOL_CLASSES = ImmutableSet.of("axe", "saw");

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        if(enchantment.type == null) {
            return false;
        }

        return enchantment.type.canEnchantItem(Items.IRON_AXE);
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 2;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 3.0F;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && SAW_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.LEAVES ||
                block.getMaterial() == Material.VINE ||
                block.getMaterial() == Material.WOOD ||
                block.getMaterial() == Material.CACTUS ||
                block.getMaterial() == Material.ICE ||
                block.getMaterial() == Material.PACKED_ICE;
    }

    @Override
    public boolean onBlockPreBreak(ItemStack stack, BlockPos blockPos, EntityPlayer player) {
        if (player.world.isRemote || player.capabilities.isCreativeMode) {
            return false;
        }
        boolean result = ToolUtility.applyShearBehavior(stack, blockPos, player);
        if (result) {
            ToolMetaItem<?> toolMetaItem = (ToolMetaItem<?>) stack.getItem();
            int damagePerBlockBreak = getToolDamagePerBlockBreak(stack);
            toolMetaItem.damageItem(stack, player, damagePerBlockBreak, false);
        }
        return result;
    }

    @Override
    public void convertBlockDrops(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, List<ItemStack> dropList, ItemStack toolStack) {
        if (blockState.getMaterial() == Material.PACKED_ICE || blockState.getMaterial() == Material.ICE) {
            if (blockState.getMaterial() == Material.ICE) {
                world.addEventListener(new IceEventListener(blockPos, world.getTotalWorldTime()));
            }
            Item item = Item.getItemFromBlock(blockState.getBlock());
            ItemStack dropStack = new ItemStack(item, 1);
            dropList.add(dropStack);
        }
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return SAW_TOOL_CLASSES;
    }

    /**
     * Added to the world event listeners to hopefully catch and revert the ice-to-water conversion.
     * <p>
     * The conversion may never happen, for example when there is air under the ice block,
     * so this invalidates itself on the first block update after the tick it was added.
     */
    @ParametersAreNonnullByDefault
    private static class IceEventListener implements IWorldEventListener {

        private final BlockPos blockPos;
        private final long time;

        public IceEventListener(BlockPos blockPos, long totalWorldTime) {
            this.blockPos = blockPos;
            this.time = totalWorldTime;
        }

        @Override
        public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
            /*
            It should be reasonably safe to remove the event listener at this time:
            - The loop this is called from is index-based, rather than iterator-based,
              so it shouldn't throw a ConcurrentModificationException.
            - Hopefully this is the last entry in the list, so removal here
              doesn't skip any of the other listeners.
             */
            if (time != worldIn.getTotalWorldTime()) {
                worldIn.removeEventListener(this);
                return;
            }
            if (pos.equals(blockPos) && newState.getBlock() == Blocks.FLOWING_WATER) {
                // it's probably alright to assume this will only be attempted once
                worldIn.removeEventListener(this);
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), flags);
            }
        }

        @Override
        public void notifyLightSet(BlockPos pos) {
        }

        @Override
        public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        }

        @Override
        public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        }

        @Override
        public void playRecord(SoundEvent soundIn, BlockPos pos) {
        }

        @Override
        public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        }

        @Override
        public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        }

        @Override
        public void onEntityAdded(Entity entityIn) {
        }

        @Override
        public void onEntityRemoved(Entity entityIn) {
        }

        @Override
        public void broadcastSound(int soundID, BlockPos pos, int data) {
        }

        @Override
        public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
        }

        @Override
        public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        }
    }
}
