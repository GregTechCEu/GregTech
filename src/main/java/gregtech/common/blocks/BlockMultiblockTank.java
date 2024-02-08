package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public class BlockMultiblockTank extends VariantActiveBlock<BlockMultiblockTank.MultiblockTankType> {
    public BlockMultiblockTank() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("multiblock_tank");
        setHardness(3.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(MultiblockTankType.CLARIFIER));
    }

    @Override
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    public enum MultiblockTankType implements IStringSerializable, IStateHarvestLevel {
        CLARIFIER("clarifier", 1),
        FLOTATION("flotation", 1);

        private final String name;
        private final int harvestLevel;

        private MultiblockTankType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        public @NotNull String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
