package gregtech.api.worldgen.populator;

import gregtech.api.util.GTLog;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.PredicateConfigUtils;
import gregtech.api.worldgen.generator.GridEntryInfo;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

import com.google.gson.JsonObject;

import java.util.Random;

public class SurfaceBlockPopulator implements VeinChunkPopulator {

    private IBlockState blockState;
    private int minIndicatorAmount;
    private int maxIndicatorAmount;
    private int failedGenerationCounter = 0;

    public SurfaceBlockPopulator() {}

    public SurfaceBlockPopulator(IBlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.blockState = PredicateConfigUtils.parseBlockStateDefinition(object.getAsJsonObject("block"));
        this.minIndicatorAmount = JsonUtils.getInt(object, "min_amount", 1);
        this.maxIndicatorAmount = JsonUtils.getInt(object, "max_amount", 3);
    }

    @Override
    public void initializeForVein(OreDepositDefinition definition) {}

    /**
     * Generates the Surface Block for an underground vein. Spawns the Surface Block on top of the applicable topmost
     * block in
     * the chunk, at a random position in the chunk. Does not run on a Flat world type
     *
     * @param world         - The Minecraft world. Used for finding the top most block and its state
     * @param chunkX        - The X chunk coordinate
     * @param chunkZ        - The Z chunk coordinate
     * @param random        - A Random parameter. Used for determining the number of spawned Surface Blocks and their
     *                      position
     * @param definition    - The Ore Vein definition
     * @param gridEntryInfo - Information about the ore generation grid for the current generation section
     */
    @Override
    public void populateChunk(World world, int chunkX, int chunkZ, Random random, OreDepositDefinition definition,
                              GridEntryInfo gridEntryInfo) {
        int stonesCount = minIndicatorAmount + (minIndicatorAmount >= maxIndicatorAmount ? 0 :
                random.nextInt(maxIndicatorAmount - minIndicatorAmount));
        if (stonesCount > 0 && world.getWorldType() != WorldType.FLAT) {
            for (int i = 0; i < stonesCount; i++) {
                int randomX = chunkX * 16 + random.nextInt(8);
                int randomZ = chunkZ * 16 + random.nextInt(8);

                boolean successful = generateSurfaceBlock(world, new BlockPos(randomX, 0, randomZ));

                if (!successful) {
                    failedGenerationCounter++;
                }
            }

            // The Guaranteed generation
            generateSurfaceBlock(world, new BlockPos(gridEntryInfo.getCenterPos(definition)));

        }

        // Log if all Surface Block generation attempts were failed
        if (failedGenerationCounter == stonesCount && maxIndicatorAmount > 0 &&
                world.getWorldType() != WorldType.FLAT) {
            GTLog.logger.debug(
                    "Failed all Surface Block generation attempts for vein {} at chunk with position: x: {}, z: {}",
                    definition.getDepositName(), chunkX, chunkZ);
        }
    }

    private boolean generateSurfaceBlock(World world, BlockPos pos) {
        BlockPos topBlockPos = SurfaceRockPopulator.findSpawnHeight(world, pos);
        IBlockState blockState = world.getBlockState(topBlockPos.down());
        Block blockAtPos = blockState.getBlock();

        if (topBlockPos.getY() >= world.provider.getActualHeight()) {
            return false;
        }

        // Check to see if the selected block has special rendering parameters (like glass) or a special model
        if (!blockState.isOpaqueCube() || !blockState.isFullBlock()) {
            return false;
        }

        // Checks if the block is a replaceable feature like grass or snow layers. Liquids are replaceable, so
        // exclude one deep liquid blocks, for looks
        if (!blockAtPos.isReplaceable(world, topBlockPos) || blockState.getMaterial().isLiquid()) {
            return false;
        }

        return world.setBlockState(topBlockPos, this.blockState, 16);
    }

    public IBlockState getBlockState() {
        return blockState;
    }
}
