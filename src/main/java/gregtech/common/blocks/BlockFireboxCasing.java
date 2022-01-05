package gregtech.common.blocks;

import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockFireboxCasing extends VariantActiveBlock<FireboxCasingType> {

    public BlockFireboxCasing() {
        super(Material.IRON);
        setTranslationKey("boiler_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(FireboxCasingType.BRONZE_FIREBOX));
    }

    @Override
    public int getLightValue(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getPackedLightmapCoords(IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        if (state.getValue(ACTIVE)) {
            return 0b10100000 << 16 | 0b10100000;
        }
        return source.getCombinedLight(pos, state.getLightValue(source, pos));
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
