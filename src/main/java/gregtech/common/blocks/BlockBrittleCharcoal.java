package gregtech.common.blocks;

import gregtech.api.GTValues;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                         @NotNull IBlockState state, int fortune) {
        drops.add(new ItemStack(Items.COAL, 1 + GTValues.RNG.nextInt(2), 1));
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("tile.brittle_charcoal.tooltip.1"));
        tooltip.add(I18n.format("tile.brittle_charcoal.tooltip.2"));
    }
}
