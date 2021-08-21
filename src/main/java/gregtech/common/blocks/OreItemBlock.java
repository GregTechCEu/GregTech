package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.ore.StoneType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class OreItemBlock extends ItemBlock {

    private final BlockOre oreBlock;

    public OreItemBlock(BlockOre oreBlock) {
        super(oreBlock);
        this.oreBlock = oreBlock;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Nonnull
    @Override
    public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{CreativeTabs.SEARCH, GregTechAPI.TAB_GREGTECH_ORES};
    }

    protected IBlockState getBlockState(ItemStack stack) {
        return oreBlock.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        IBlockState blockState = getBlockState(stack);
        StoneType stoneType = blockState.getValue(oreBlock.STONE_TYPE);
        return stoneType.processingPrefix.getLocalNameForItem(oreBlock.material);
    }
}
