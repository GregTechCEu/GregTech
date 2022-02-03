package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import gregtech.common.items.MetaItems;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockRubberLog extends BlockLog {

    public static final PropertyBool NATURAL = PropertyBool.create("natural");

    public BlockRubberLog() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(LOG_AXIS, BlockLog.EnumAxis.Y)
                .withProperty(NATURAL, false));
        setTranslationKey("rubber_log");
        this.setCreativeTab(GregTechAPI.TAB_GREGTECH);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS, NATURAL);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(NATURAL, (meta & 1) == 1)
                .withProperty(LOG_AXIS, EnumAxis.values()[meta >> 1]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(NATURAL) ? 1 : 0) | (state.getValue(LOG_AXIS).ordinal() << 1);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        if (state.getValue(NATURAL)) {
            if(rand.nextDouble() <= .85D) {
                drops.add(MetaItems.STICKY_RESIN.getStackForm());
            }
        }
        drops.add(new ItemStack(this));
    }
}
