package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockStoneBricksMossy extends VariantBlock<BlockStoneBricksMossy.BlockType> {

    public BlockStoneBricksMossy() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("stone_bricks_mossy");
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
