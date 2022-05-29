package gregtech.common.blocks;

import gregtech.api.GTValues;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockBrittleCharcoal extends Block {

    public BlockBrittleCharcoal() {
        super(Material.ROCK, MapColor.BLACK);
        setHardness(0.5F);
        setResistance(8.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("shovel", 0);
        setTranslationKey("brittle_charcoal");
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        drops.add(new ItemStack(Items.COAL, 1 + GTValues.RNG.nextInt(2), 1));
    }
}
