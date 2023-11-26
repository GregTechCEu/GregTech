package gregtech.worldgen.generator;

import gregtech.worldgen.PlacementResult;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class MixedVeinGenerator extends CuboidVeinGenerator<MixedVeinSettings, MixedVeinGenerator> {

    public MixedVeinGenerator(@NotNull MixedVeinSettings settings) {
        super(settings);
    }

    @Override
    protected @Nullable PlacementResult placeOres(@NotNull World world, @NotNull Random random,
                                                  @NotNull BlockPos.MutableBlockPos pos, double volume, int startY,
                                                  int xLeft, int xRight, int zLeft, int zRight, int westBound,
                                                  int eastBound, int southBound, int northBound) {
        int[] densities = settings.densities();
        WorldgenPlaceable[] placeables = settings.placeables();

        int placed = 0;

        int height = settings.height();
        for (int yOffset = 0; yOffset < height; yOffset++) {
            int y = startY + yOffset;
            for (int x = xLeft; x < xRight; x++) {
                for (int z = zLeft; z < zRight; z++) {
                    for (int i = 0; i < densities.length; i++) {
                        if (!placeables[i].hasRegular()) continue;

                        pos.setPos(x, y, z);
                        IBlockState existing = world.getBlockState(pos);

                        int localDensity = (int) Math.max(1, densities[i] / volume);
                        int weightX = Math.max(1, Math.max(MathHelper.abs(westBound - x), MathHelper.abs(eastBound - x)) / localDensity);
                        int weightZ = Math.max(1, Math.max(MathHelper.abs(southBound - z), MathHelper.abs(northBound - z)) / localDensity);

                        if (shouldPlaceOre(random, weightX, weightZ)) {
                            placeOre(world, pos, placeables[i], existing);
                            placed++;
                            break;
                        }
                    }
                }
            }

            // ensure there's at least one ore in bottom layer before attempting to place the rest of the vein
            if (yOffset == 0 && placed == 0) {
                if (WorldgenModule.DEBUG) {
                    WorldgenModule.logger.info("No ore in bottom layer");
                }
                return PlacementResult.CANNOT_GEN_IN_BOTTOM;
            }
        }

        return null;
    }

    @Override
    protected void placeSmallOres(@NotNull World world, @NotNull Random random, BlockPos.@NotNull MutableBlockPos pos,
                                  int xLeft, int xRight, int zLeft, int zRight, int chunkX, int chunkZ) {

    }
}
