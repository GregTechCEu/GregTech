package gregtech.api.worldgen.populator;

import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.generator.GridEntryInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface VeinBufferPopulator extends IVeinPopulator {

    void populateBlockBuffer(Random random, GridEntryInfo gridEntryInfo, IBlockModifierAccess modifier,
                             OreDepositDefinition depositDefinition);

    IBlockState getBlockByIndex(World world, BlockPos pos, int index);
}
