package gregtech.api.worldgen.populator;

import com.google.gson.JsonObject;
import gregtech.api.util.GTLog;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.PredicateConfigUtils;
import gregtech.api.worldgen.generator.GridEntryInfo;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

import java.util.Random;

public class SurfaceBlockPopulator implements VeinChunkPopulator {

    private IBlockState blockState;
    private int minIndicatorAmount;
    private int maxIndicatorAmount;
    private int failedGenerationCounter = 0;

    public SurfaceBlockPopulator() {
    }

    public SurfaceBlockPopulator(IBlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.blockState = PredicateConfigUtils.parseBlockStateDefinition(object.getAsJsonObject("block"));
        this.minIndicatorAmount = JsonUtils.getInt(object, "min_amount", 0);
        this.maxIndicatorAmount = JsonUtils.getInt(object, "max_amount", 2);
    }

    @Override
    public void initializeForVein(OreDepositDefinition definition) {
    }

    /**
     * Generates the Surface Block for an underground vein. Spawns the Surface Block on top of the applicable topmost block in
     * the chunk, at a random position in the chunk. Does not run on a Flat world type
     * @param world - The Minecraft world. Used for finding the top most block and its state
     * @param chunkX - The X chunk coordinate
     * @param chunkZ - The Z chunk coordinate
     * @param random - A Random parameter. Used for determining the number of spawned Surface Blocks and their position
     * @param definition - The Ore Vein definition
     * @param gridEntryInfo - Information about the ore generation grid for the current generation section
     */
    @Override
    public void populateChunk(World world, int chunkX, int chunkZ, Random random, OreDepositDefinition definition, GridEntryInfo gridEntryInfo) {
        int stonesCount = minIndicatorAmount + (minIndicatorAmount >= maxIndicatorAmount ? 0 : random.nextInt(maxIndicatorAmount - minIndicatorAmount));
        if (stonesCount > 0 && world.getWorldType() != WorldType.FLAT) {
            for (int i = 0; i < stonesCount; i++) {
                int randomX = chunkX * 16 + random.nextInt(16);
                int randomZ = chunkZ * 16 + random.nextInt(16);
                BlockPos topBlockPos = new BlockPos(randomX, 0, randomZ);
                topBlockPos = world.getTopSolidOrLiquidBlock(topBlockPos).down();
                IBlockState blockState = world.getBlockState(topBlockPos);
                Block blockAtPos = blockState.getBlock();

                //Check to see if the block below the planned generation position has a Solid top side. This is similar to
                //an isSideSolid check
                if (blockState.getBlockFaceShape(world, topBlockPos, EnumFacing.UP) != BlockFaceShape.SOLID) {
                    continue;
                }

                //Check to see if the selected block has special rendering parameters (like glass) or a special model
                if(!blockState.isOpaqueCube() || !blockState.isFullBlock()) {
                    continue;
                }

                //Checks if the block is a replaceable feature like grass or snow layers. Liquids are replaceable, so
                // exclude one deep liquid blocks, for looks
                if(!blockAtPos.isReplaceable(world, topBlockPos.up()) || blockState.getMaterial().isLiquid()) {
                    continue;
                }

                BlockPos surfaceRockPos = topBlockPos.up();
                boolean successful = world.setBlockState(surfaceRockPos, this.blockState, 16);

                if(!successful) {
                    failedGenerationCounter++;
                }
            }
        }

        //Log if all Surface Block generation attempts were failed
        if(failedGenerationCounter == stonesCount && maxIndicatorAmount > 0 && world.getWorldType() != WorldType.FLAT) {
            GTLog.logger.debug("Failed all Surface Block generation attempts for vein {} at chunk with position: x: {}, z: {}", definition.getDepositName(), chunkX, chunkZ);
        }
    }

    public IBlockState getBlockState() {
        return blockState;
    }
}
