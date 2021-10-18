package gregtech.common.blocks;

import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockFireboxCasing extends VariantBlock<FireboxCasingType> {

    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockFireboxCasing() {
        super(Material.IRON);
        setTranslationKey("boiler_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(FireboxCasingType.BRONZE_FIREBOX).withProperty(ACTIVE, false));
    }

    @Override
    public int getLightValue(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        super.createBlockState();
        return new BlockStateContainer(this, VARIANT, ACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta % 8).withProperty(ACTIVE, meta / 8 >= 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) + (state.getValue(ACTIVE) ? 8 : 0);
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return super.getMetaFromState(state);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    public enum FireboxCasingType implements IStringSerializable {

        BRONZE_FIREBOX("bronze_firebox"),
        STEEL_FIREBOX("steel_firebox"),
        TITANIUM_FIREBOX("titanium_firebox"),
        TUNGSTENSTEEL_FIREBOX("tungstensteel_firebox");

        private final String name;

        FireboxCasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

    }

}
