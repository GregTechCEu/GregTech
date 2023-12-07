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

public class BlockColored extends VariantBlock<EnumDyeColor> {

    public BlockColored() {
        this(net.minecraft.block.material.Material.IRON, "block_colored", 2.0f, 5.0f, SoundType.METAL,
                EnumDyeColor.WHITE);
    }

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
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public double getWalkingSpeedBonus() {
        return 1.25;
    }

    @Override
    public boolean checkApplicableBlocks(IBlockState state) {
        return this == MetaBlocks.STUDS;
    }

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color) {
        if (world.getBlockState(pos) != getState(color)) {
            world.setBlockState(pos, getState(color));
            return true;
        }

        return false;
    }
}
