package gregtech.common.blocks;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FrameItemBlock extends ItemBlock {

    private BlockFrame frameBlock;

    public FrameItemBlock(BlockFrame block) {
        super(block);
        this.frameBlock = block;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @SuppressWarnings("deprecation")
    public IBlockState getBlockState(ItemStack stack) {
        return frameBlock.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        Material material = frameBlock.frameMaterial;
        return OrePrefix.frameGt.getLocalNameForItem(material);
    }

}
