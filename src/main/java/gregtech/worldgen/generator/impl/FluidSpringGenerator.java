package gregtech.worldgen.generator.impl;

import gregtech.api.util.GTLog;
import gregtech.worldgen.generator.SporadicWorldGenerator;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FluidSpringGenerator extends GeneratorBase<FluidSpringSettings> implements SporadicWorldGenerator {

    protected FluidSpringGenerator(@NotNull FluidSpringSettings settings) {
        super(settings);
    }

    @Override
    public void generate(@NotNull World world, @NotNull Random random, @NotNull String biome, int dimension, int chunkX,
                         int chunkZ) {
        if (!canGenerateInDimension(dimension)) return;
        if (!canGenerateInBiome(biome)) return;

        if (random.nextInt(settings.weight()) >= 1) return;

//        int amount = (settings.size() / 2) + (random.nextInt(settings.size()) / 2);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int x = chunkX + 8 + random.nextInt(16);
        int z = chunkZ + 8 + random.nextInt(16);
//            int y = settings.minY() + random.nextInt(settings.maxY() - settings.minY());
        int y = world.getHeight(x, z) + 1;
        pos.setPos(x, y, z);
        GTLog.logger.fatal("Generating at {} {} {}", x, y, z);
        if (mc(world, random, pos)) {
            GTLog.logger.fatal("Generating Success!");
        } else {
            GTLog.logger.fatal("Generating Failure!");
        }

//        placeable.place(world, pos, world.getBlockState(pos));
    }

    private boolean mc(@NotNull World world, @NotNull Random random, @NotNull BlockPos.MutableBlockPos pos) {
        boolean[] aboolean = new boolean[2048];
        int i = random.nextInt(4) + 4;

        for (int j = 0; j < i; ++j)
        {
            double d0 = random.nextDouble() * 6.0D + 3.0D;
            double d1 = random.nextDouble() * 4.0D + 2.0D;
            double d2 = random.nextDouble() * 6.0D + 3.0D;
            double d3 = random.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
            double d4 = random.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;
            double d5 = random.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;

            for (int l = 1; l < 15; ++l)
            {
                for (int i1 = 1; i1 < 15; ++i1)
                {
                    for (int j1 = 1; j1 < 7; ++j1)
                    {
                        double d6 = (l - d3) / (d0 / 2.0D);
                        double d7 = (j1 - d4) / (d1 / 2.0D);
                        double d8 = (i1 - d5) / (d2 / 2.0D);
                        double d9 = d6 * d6 + d7 * d7 + d8 * d8;

                        if (d9 < 1.0D)
                        {
                            aboolean[(l * 16 + i1) * 8 + j1] = true;
                        }
                    }
                }
            }
        }

        BlockPos.MutableBlockPos copy = new BlockPos.MutableBlockPos(pos);
        for (int k1 = 0; k1 < 16; ++k1) {
            for (int l2 = 0; l2 < 16; ++l2) {
                for (int k = 0; k < 8; ++k) {
                    boolean flag = !aboolean[(k1 * 16 + l2) * 8 + k] && (k1 < 15 && aboolean[((k1 + 1) * 16 + l2) * 8 + k] || k1 > 0 && aboolean[((k1 - 1) * 16 + l2) * 8 + k] || l2 < 15 && aboolean[(k1 * 16 + l2 + 1) * 8 + k] || l2 > 0 && aboolean[(k1 * 16 + (l2 - 1)) * 8 + k] || k < 7 && aboolean[(k1 * 16 + l2) * 8 + k + 1] || k > 0 && aboolean[(k1 * 16 + l2) * 8 + (k - 1)]);

                    if (flag) {
                        pos.setPos(copy.getX(), copy.getY(), copy.getZ());
                        pos.setPos(k1, k, l2);
                        Material material = world.getBlockState(pos).getMaterial();

                        if (k >= 4 && material.isLiquid()) {
                            return false;
                        }
                    }
                }
            }
        }

        for (int l1 = 0; l1 < 16; ++l1) {
            for (int i3 = 0; i3 < 16; ++i3) {
                for (int i4 = 0; i4 < 8; ++i4) {
                    if (aboolean[(l1 * 16 + i3) * 8 + i4]) {
                        pos.setPos(copy.getX() + l1, copy.getY() + i4, copy.getZ() + i3);
                        if (i4 > 4) {
                            world.setBlockToAir(pos);
                        } else {
                            settings.placeable().place(world, pos, world.getBlockState(pos));
                        }
                    }
                }
            }
        }
        return true;
    }
}
