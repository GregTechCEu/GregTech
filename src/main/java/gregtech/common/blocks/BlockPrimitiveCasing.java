package gregtech.common.blocks;

import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockPrimitiveCasing extends VariantBlock<BlockPrimitiveCasing.MultiblockCasingType> {

    public BlockPrimitiveCasing() {
        super(Material.IRON);
        setTranslationKey("primitive_casing");
        setHardness(2.5f);
        setResistance(5f);
        setSoundType(SoundType.WOOD);
        setHarvestLevel(ToolClasses.WRENCH, 1);
        setDefaultState(getState(MultiblockCasingType.COAGULATION_TANK_WALL));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    public enum MultiblockCasingType implements IStringSerializable {

        COAGULATION_TANK_WALL("coagulation_tank_wall");

        private final String name;

        MultiblockCasingType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
