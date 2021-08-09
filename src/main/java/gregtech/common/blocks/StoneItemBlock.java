package gregtech.common.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class StoneItemBlock<R extends Enum<R> & IStringSerializable, T extends StoneBlock<R>> extends ItemBlock {

    private final T genericBlock;

    public StoneItemBlock(T block) {
        super(block);
        this.genericBlock = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @SuppressWarnings("deprecation")
    protected IBlockState getBlockState(ItemStack stack) {
        return block.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        IBlockState blockState = getBlockState(stack);
        return super.getTranslationKey(stack) + '.' +
                genericBlock.getVariant(blockState).getName() + "." +
                genericBlock.getChiselingVariant(blockState).getName();
    }

}

