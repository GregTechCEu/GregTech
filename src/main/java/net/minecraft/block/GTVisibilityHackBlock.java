package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class GTVisibilityHackBlock extends Block {

    public static ItemStack getSilkTouchDrop(Block block, IBlockState state) {
        return block.getSilkTouchDrop(state);
    }

    private GTVisibilityHackBlock() {
        super(Material.AIR);
    }

}
