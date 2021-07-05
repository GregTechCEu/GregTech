package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import gregtech.common.items.MetaItems;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockGregLog extends BlockLog {

    public static final PropertyEnum<LogVariant> VARIANT = PropertyEnum.create("variant", LogVariant.class);
    public static final PropertyBool NATURAL = PropertyBool.create("natural");

    public BlockGregLog() {
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(VARIANT, LogVariant.RUBBER_WOOD)
            .withProperty(LOG_AXIS, BlockLog.EnumAxis.Y)
            .withProperty(NATURAL, false));
        setTranslationKey("gt.log");
        this.setCreativeTab(GregTechAPI.TAB_GREGTECH);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (LogVariant logVariant : LogVariant.values()) {
            items.add(getItem(logVariant));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, LOG_AXIS, NATURAL);
    }

    public ItemStack getItem(LogVariant variant) {
        return new ItemStack(this, 1, variant.ordinal() * 2);
    }

    public ItemStack getItem(LogVariant variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
            .withProperty(LOG_AXIS, EnumAxis.values()[meta / 4 % 4])
            .withProperty(VARIANT, LogVariant.values()[meta % 4 / 2 % LogVariant.values().length])
            .withProperty(NATURAL, meta % 4 % 2 == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LOG_AXIS).ordinal() * 4 +
            state.getValue(VARIANT).ordinal() * 2 +
            (state.getValue(NATURAL) ? 1 : 0);
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this), 1,
            state.getValue(VARIANT).ordinal());
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1,
            state.getValue(VARIANT).ordinal());
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        if (state.getValue(NATURAL)) {
            drops.add(MetaItems.RUBBER_DROP.getStackForm(1 + rand.nextInt(2)));
        }
        drops.add(new ItemStack(this, 1, state.getValue(VARIANT).ordinal()));
    }

    public enum LogVariant implements IStringSerializable {

        RUBBER_WOOD("rubber_wood");

        private final String name;

        LogVariant(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
