package gregtech.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class VariantItemBlock<R extends Enum<R> & IStringSerializable, T extends VariantBlock<R>> extends ItemBlock {

    private final T genericBlock;

    public VariantItemBlock(T block) {
        super(block);
        this.genericBlock = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @SuppressWarnings("deprecation")
    public IBlockState getBlockState(ItemStack stack) {
        return block.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        return super.getTranslationKey(stack) + '.' + genericBlock.getState(getBlockState(stack)).getName();
    }

}
