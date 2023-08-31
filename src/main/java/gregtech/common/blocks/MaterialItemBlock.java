package gregtech.common.blocks;

import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class MaterialItemBlock extends ItemBlock {

    private final BlockMaterialBase block;
    private final OrePrefix prefix;

    public MaterialItemBlock(BlockMaterialBase block, OrePrefix prefix) {
        super(block);
        this.block = block;
        this.prefix = prefix;
        setHasSubtypes(true);
    }

    @Override
    public BlockMaterialBase getBlock() {
        return block;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return this.prefix.getLocalNameForItem(this.block.getGtMaterial(stack));
    }
}
