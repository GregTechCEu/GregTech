package gregtech.common.blocks;

import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class MaterialItemBlock extends ItemBlock {

    private final BlockMaterialBase block;
    private final OrePrefix prefix;

    public MaterialItemBlock(BlockMaterialBase block, OrePrefix prefix) {
        super(block);
        this.block = block;
        this.prefix = prefix;
        setHasSubtypes(true);
    }

    @NotNull
    @Override
    public BlockMaterialBase getBlock() {
        return block;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        return this.prefix.getLocalNameForItem(this.block.getGtMaterial(stack));
    }
}
