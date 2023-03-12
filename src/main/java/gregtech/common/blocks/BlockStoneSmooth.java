package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockStoneSmooth extends VariantBlock<BlockStoneSmooth.BlockType> {

    public BlockStoneSmooth() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("stone_smooth");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.PICKAXE, 1);
        setDefaultState(getState(BlockType.BLACK_GRANITE));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onEntityWalk(@Nonnull World worldIn, @Nonnull BlockPos pos, Entity entityIn) {

        IBlockState below = entityIn.getEntityWorld().getBlockState(new BlockPos(entityIn.posX, entityIn.posY - (1 / 16D), entityIn.posZ));
        if (below == getState(BlockStoneSmooth.BlockType.CONCRETE_DARK) || below == getState(BlockStoneSmooth.BlockType.CONCRETE_LIGHT)) {
            if (!entityIn.isInWater()) {
                entityIn.motionX *= 1.6;
                entityIn.motionZ *= 1.6;
            }
        }
    }

    public BlockStoneSmooth.BlockType getVariant(IBlockState blockState) {
        return blockState.getValue(VARIANT);
    }

    public enum BlockType implements IStringSerializable {

        BLACK_GRANITE("black_granite"),
        RED_GRANITE("red_granite"),
        MARBLE("marble"),
        BASALT("basalt"),
        CONCRETE_LIGHT("concrete_light"),
        CONCRETE_DARK("concrete_dark");

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
