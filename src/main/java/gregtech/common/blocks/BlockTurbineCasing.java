package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockTurbineCasing extends VariantBlock<BlockTurbineCasing.TurbineCasingType> {

    public BlockTurbineCasing() {
        super(Material.IRON);
        setTranslationKey("turbine_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(TurbineCasingType.BRONZE_GEARBOX));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
        return false;
    }

    public enum TurbineCasingType implements IStringSerializable, IStateHarvestLevel {

        BRONZE_GEARBOX("bronze_gearbox", 1),
        STEEL_GEARBOX("steel_gearbox", 2),
        STAINLESS_STEEL_GEARBOX("stainless_steel_gearbox", 2),
        TITANIUM_GEARBOX("titanium_gearbox", 2),
        TUNGSTENSTEEL_GEARBOX("tungstensteel_gearbox", 3),

        STEEL_TURBINE_CASING("steel_turbine_casing", 2),
        TITANIUM_TURBINE_CASING("titanium_turbine_casing", 2),
        STAINLESS_TURBINE_CASING("stainless_turbine_casing", 2),
        TUNGSTENSTEEL_TURBINE_CASING("tungstensteel_turbine_casing", 3);

        private final String name;
        private final int harvestLevel;

        TurbineCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        @Nonnull
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return ToolClasses.WRENCH;
        }
    }

}
