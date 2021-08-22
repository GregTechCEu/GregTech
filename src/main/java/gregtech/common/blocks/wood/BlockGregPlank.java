package gregtech.common.blocks.wood;

import gregtech.api.GregTechAPI;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockGregPlank extends Block {

    public static final PropertyEnum<PlankVariant> VARIANT = PropertyEnum.create("variant", PlankVariant.class);

    public BlockGregPlank() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(VARIANT, PlankVariant.RUBBER_PLANK));
        setTranslationKey("gt.plank");
        this.setCreativeTab(GregTechAPI.TAB_GREGTECH);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        for(PlankVariant plankVariant : PlankVariant.values()) {
            items.add(getItem(plankVariant));
        }
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, PlankVariant.values()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1,
                state.getValue(VARIANT).ordinal());
    }

    public ItemStack getItem(PlankVariant variant) {
        return new ItemStack(this, 1, variant.ordinal());
    }

    public ItemStack getItem(PlankVariant variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    public enum PlankVariant implements IStringSerializable {

        RUBBER_PLANK("rubber_plank");

        private final String name;

        PlankVariant(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return name;
        }
    }
}
