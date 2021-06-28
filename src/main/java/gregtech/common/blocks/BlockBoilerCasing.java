package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockBoilerCasing extends VariantBlock<BlockBoilerCasing.BoilerCasingType> {

    public BlockBoilerCasing() {
        super(Material.IRON);
        setTranslationKey("boiler_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BoilerCasingType.BRONZE_PIPE));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
        return false;
    }

    public enum BoilerCasingType implements IStringSerializable {

        BRONZE_PIPE("bronze_pipe"),
        STEEL_PIPE("steel_pipe"),
        TITANIUM_PIPE("titanium_pipe"),
        TUNGSTENSTEEL_PIPE("tungstensteel_pipe"),
        POLYTETRAFLUOROETHYLENE_PIPE("polytetrafluoroethylene_pipe");

        private final String name;

        BoilerCasingType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

    }

}
