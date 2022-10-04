package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockLamp extends VariantBlock<BlockLamp.LampType> {

    public BlockLamp() {
        super(Material.GLASS);
        setTranslationKey("lamp");
        setHardness(2.0f);
        setResistance(8.0f);
        setLightLevel(1.0f);
        setSoundType(SoundType.GLASS);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(LampType.LAMP_WHITE));
    }

    public void toggleState(World worldIn, BlockPos pos, int meta) {
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, getComplementaryState(meta));
        }
    }

    public IBlockState getComplementaryState(int meta) {
        return MetaBlocks.OFF_LAMP.getStateFromMeta(meta);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote && worldIn.isBlockPowered(pos)) {
            worldIn.scheduleUpdate(pos, this, 4);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && worldIn.isBlockPowered(pos)) {
            toggleState(worldIn, pos, getMetaFromState(state));
        }

    }

    public enum LampType implements IStringSerializable {

        LAMP_WHITE("lamp_white"),
        LAMP_ORANGE("lamp_orange"),
        LAMP_MAGENTA("lamp_magenta"),
        LAMP_LIGHTBLUE("lamp_lightblue"),
        LAMP_YELLOW("lamp_yellow"),
        LAMP_LIME("lamp_lime"),
        LAMP_PINK("lamp_pink"),
        LAMP_GRAY("lamp_gray"),
        LAMP_LIGHTGRAY("lamp_lightgray"),
        LAMP_CYAN("lamp_cyan"),
        LAMP_PURPLE("lamp_purple"),
        LAMP_BLUE("lamp_blue"),
        LAMP_BROWN("lamp_brown"),
        LAMP_GREEN("lamp_green"),
        LAMP_RED("lamp_red"),
        LAMP_BLACK("lamp_black");

        private final String name;

        LampType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getName() {
            return this.name;
        }
    }
}
