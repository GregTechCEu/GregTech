package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockLampOff extends VariantBlock<BlockLampOff.OffLampType> {

    public BlockLampOff() {
        super(Material.GLASS);
        setTranslationKey("off_lamp");
        setHardness(2.0f);
        setResistance(8.0f);
        setSoundType(SoundType.GLASS);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(OffLampType.OFF_LAMP_WHITE));
    }

    public void toggleState(World worldIn, BlockPos pos, int meta) {
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, getComplementaryState(meta));
        }
    }

    public IBlockState getComplementaryState(int meta) {
        return MetaBlocks.LAMP.getStateFromMeta(meta);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return MetaBlocks.LAMP.getItemDropped(state, rand, fortune);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote && !worldIn.isBlockPowered(pos)) {
            toggleState(worldIn, pos, getMetaFromState(state));
        }
    }

    public enum OffLampType implements IStringSerializable {

        OFF_LAMP_WHITE("lamp_white"),
        OFF_LAMP_ORANGE("lamp_orange"),
        OFF_LAMP_MAGENTA("lamp_magenta"),
        OFF_LAMP_LIGHTBLUE("lamp_lightblue"),
        OFF_LAMP_YELLOW("lamp_yellow"),
        OFF_LAMP_LIME("lamp_lime"),
        OFF_LAMP_PINK("lamp_pink"),
        OFF_LAMP_GRAY("lamp_gray"),
        OFF_LAMP_LIGHTGRAY("lamp_lightgray"),
        OFF_LAMP_CYAN("lamp_cyan"),
        OFF_LAMP_PURPLE("lamp_purple"),
        OFF_LAMP_BLUE("lamp_blue"),
        OFF_LAMP_BROWN("lamp_brown"),
        OFF_LAMP_GREEN("lamp_green"),
        OFF_LAMP_RED("lamp_red"),
        OFF_LAMP_BLACK("lamp_black");

        private final String name;

        OffLampType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getName() {
            return this.name;
        }
    }
}
