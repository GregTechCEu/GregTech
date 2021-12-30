package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockAsphalt extends VariantBlock<BlockAsphalt.BlockType> {

    public BlockAsphalt() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("asphalt");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 2);
        setDefaultState(getState(BlockType.ASPHALT));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if ((entityIn.motionX != 0 || entityIn.motionZ != 0) && !entityIn.isInWater() && !entityIn.isSneaking()) {
            entityIn.motionX *= 1.3;
            entityIn.motionZ *= 1.3;
        }
    }

    public enum BlockType implements IStringSerializable {

        ASPHALT("asphalt");

        private final String name;

        BlockType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
