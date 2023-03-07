package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockHermeticCasing extends VariantBlock<BlockHermeticCasing.HermeticCasingsType> {

    public BlockHermeticCasing() {
        super(Material.IRON);
        setTranslationKey("hermetic_casing");
        setHardness(2.0f);
        setResistance(8.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 1);
        setDefaultState(getState(HermeticCasingsType.HERMETIC_LV));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        // cutout is needed for the top (outer) layer/overlay to render properly in world
        return BlockRenderLayer.CUTOUT;
    }

    public enum HermeticCasingsType implements IStringSerializable {

        HERMETIC_LV("hermetic_casing_lv"),
        HERMETIC_MV("hermetic_casing_mv"),
        HERMETIC_HV("hermetic_casing_hv"),
        HERMETIC_EV("hermetic_casing_ev"),
        HERMETIC_IV("hermetic_casing_iv"),
        HERMETIC_LUV("hermetic_casing_luv"),
        HERMETIC_ZPM("hermetic_casing_zpm"),
        HERMETIC_UV("hermetic_casing_uv"),
        HERMETIC_UHV("hermetic_casing_uhv");

        private final String name;

        HermeticCasingsType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getName() {
            return this.name;
        }
    }
}
