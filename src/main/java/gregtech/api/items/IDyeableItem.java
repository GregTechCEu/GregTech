package gregtech.api.items;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

public interface IDyeableItem {

    String COLOR_KEY = "gt_color";

    default boolean hasColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        return nbttagcompound != null && nbttagcompound.hasKey(COLOR_KEY, Constants.NBT.TAG_INT);
    }

    default int getColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        if (nbttagcompound != null && nbttagcompound.hasKey(COLOR_KEY, Constants.NBT.TAG_INT)) {
            return nbttagcompound.getInteger(COLOR_KEY);
        }
        return getDefaultColor(stack);
    }

    default int getDefaultColor(ItemStack stack) {
        return 0xFFFFFF;
    }

    default void removeColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        if (nbttagcompound != null && nbttagcompound.hasKey(COLOR_KEY)) {
            nbttagcompound.removeTag(COLOR_KEY);
        }
    }

    default void setColor(ItemStack stack, int color) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        if (nbttagcompound == null) {
            nbttagcompound = new NBTTagCompound();
            stack.setTagCompound(nbttagcompound);
        }
        nbttagcompound.setInteger(COLOR_KEY, color);
    }

    default @NotNull EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world,
                                                     @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX,
                                                     float hitY, float hitZ, @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.hasColor(stack)) {
            IBlockState iblockstate = world.getBlockState(pos);
            if (iblockstate.getBlock() instanceof BlockCauldron cauldron) {
                int water = iblockstate.getValue(BlockCauldron.LEVEL);
                if (water > 0) {
                    this.removeColor(stack);
                    cauldron.setWaterLevel(world, pos, iblockstate, water - 1);
                    return EnumActionResult.SUCCESS;
                }
            }
        }
        return EnumActionResult.PASS;
    }

    /**
     * Controls whether the dyeing recipe simply removes the dyeable item from the crafting grid,
     * or calls {@link net.minecraftforge.common.ForgeHooks#getContainerItem(ItemStack)} on it.
     */
    default boolean shouldGetContainerItem() {
        return true;
    }
}
