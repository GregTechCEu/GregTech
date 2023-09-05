package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockLargeMetalSheet extends VariantBlock<BlockLargeMetalSheet.SheetType> {
    public BlockLargeMetalSheet() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("large_metal_sheet");
        setHardness(2.0f);
        setResistance(5.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(BlockLargeMetalSheet.SheetType.WHITE));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum SheetType implements IStringSerializable {
        WHITE("white", 1),
        ORANGE("orange", 1),
        MAGENTA("magenta", 1),
        LIGHT_BLUE("light_blue", 1),
        YELLOW("yellow", 1),
        LIME("lime", 1),
        PINK("pink", 1),
        GRAY("gray", 1),
        LIGHT_GRAY("light_gray", 1),
        CYAN("cyan", 1),
        PURPLE("purple", 1),
        BLUE("blue", 1),
        BROWN("brown", 1),
        GREEN("green", 1),
        RED("red", 1),
        BLACK("black", 1);

        private String name;
        private int harvestLevel;
        SheetType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public String getName() {
            return name;
        }

        public int getHarvestLevel() {
            return harvestLevel;
        }
    }

}
