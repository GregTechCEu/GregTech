package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockAsphalt extends VariantBlock<BlockAsphalt.BlockType> {

    public BlockAsphalt() {
        super(Material.ROCK);
        setTranslationKey("asphalt");
        setHardness(1.5f);
        setResistance(10.0f);
        setSoundType(SoundType.STONE);
        setDefaultState(getState(BlockType.ASPHALT));
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public double getWalkingSpeedBonus() {
        return 1.6D;
    }

    @Override
    public boolean checkApplicableBlocks(IBlockState state) {
        return state == getState(BlockType.ASPHALT);
    }

    public enum BlockType implements IStringSerializable, IStateHarvestLevel {

        ASPHALT("asphalt", 0);

        private final String name;
        private final int harvestLevel;

        BlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }
    }
}
