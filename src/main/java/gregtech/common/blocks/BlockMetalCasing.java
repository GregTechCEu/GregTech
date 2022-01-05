package gregtech.common.blocks;

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
        setHarvestLevel("pickaxe", 2);
        setDefaultState(getState(MetalCasingType.BRONZE_BRICKS));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    public enum MetalCasingType implements IStringSerializable {

        BRONZE_BRICKS("bronze_bricks"),
        PRIMITIVE_BRICKS("primitive_bricks"),
        INVAR_HEATPROOF("invar_heatproof"),
        ALUMINIUM_FROSTPROOF("aluminium_frostproof"),
        STEEL_SOLID("steel_solid"),
        STAINLESS_CLEAN("stainless_clean"),
        TITANIUM_STABLE("titanium_stable"),
        TUNGSTENSTEEL_ROBUST("tungstensteel_robust"),
        COKE_BRICKS("coke_bricks"),
        PTFE_INERT_CASING("ptfe_inert"),
        HSSE_STURDY("hsse_sturdy");

        private final String name;

        MetalCasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

    }

}
