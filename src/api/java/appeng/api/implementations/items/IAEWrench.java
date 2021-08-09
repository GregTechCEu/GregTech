package appeng.api.implementations.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Implemented on AE's wrench(s) as a substitute for if BC's API is not
 * available.
 */
public interface IAEWrench {

    /**
     * Check if the wrench can be used.
     *
     * @param player wrenching player
     * @param pos    of block.
     * @return true if wrench can be used
     */
    boolean canWrench(ItemStack wrench, EntityPlayer player, BlockPos pos);
}
