package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
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
        setDefaultState(getState(BlockType.ASPHALT));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onEntityWalk(@Nonnull World worldIn, @Nonnull BlockPos pos, Entity entityIn) {
        if ((entityIn.motionX != 0 || entityIn.motionZ != 0) && !entityIn.isInWater() && !entityIn.isSneaking()) {
            entityIn.motionX *= 1.3;
            entityIn.motionZ *= 1.3;
        }
    }

    public enum BlockType implements IStringSerializable, IStateHarvestLevel {

        ASPHALT("asphalt", 1);

        private final String name;
        private final int harvestLevel;

        BlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }
    }
}
