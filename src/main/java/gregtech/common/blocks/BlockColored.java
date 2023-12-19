package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class BlockColored extends VariantBlock<EnumDyeColor> {

    public BlockColored(Material material, String translationKey, float hardness, float resistance, SoundType soundType,
                        EnumDyeColor defaultColor) {
        super(material);
        setTranslationKey(translationKey);
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(soundType);
        setDefaultState(getState(defaultColor));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean recolorBlock(World world, @NotNull BlockPos pos, @NotNull EnumFacing side,
                                @NotNull EnumDyeColor color) {
        if (world.getBlockState(pos) != getState(color)) {
            world.setBlockState(pos, getState(color));
            return true;
        }

        return false;
    }
}
