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

public class BlockMetalCasing extends VariantBlock<BlockMetalCasing.MetalCasingType> {

    public BlockMetalCasing() {
        super(Material.IRON);
        setTranslationKey("metal_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(MetalCasingType.BRONZE_BRICKS));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    public enum MetalCasingType implements IStringSerializable, IStateHarvestLevel {

        BRONZE_BRICKS("bronze_bricks", 1),
        PRIMITIVE_BRICKS("primitive_bricks", 1),
        INVAR_HEATPROOF("invar_heatproof", 1),
        ALUMINIUM_FROSTPROOF("aluminium_frostproof", 1),
        STEEL_SOLID("steel_solid", 2),
        STAINLESS_CLEAN("stainless_clean", 2),
        TITANIUM_STABLE("titanium_stable", 2),
        TUNGSTENSTEEL_ROBUST("tungstensteel_robust", 3),
        COKE_BRICKS("coke_bricks", 1),
        PTFE_INERT_CASING("ptfe_inert", 0),
        HSSE_STURDY("hsse_sturdy", 3);

        private final String name;
        private final int harvestLevel;

        MetalCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
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
