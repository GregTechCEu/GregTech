package gregtech.api.worldgen2;

import gregtech.api.util.PerlinNoise;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class GregTechTerrainGen {

    private static List<IBlockState> stoneTypes;
    private static PerlinNoise noise;

    private GregTechTerrainGen() {}

    public static void init() {
        stoneTypes = new ArrayList<>();
        for (StoneVariantBlock.StoneType type : StoneVariantBlock.StoneType.values()) {
            stoneTypes.add(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(type));
        }
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE));
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE));
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE));
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE));

        MinecraftForge.EVENT_BUS.register(GregTechTerrainGen.class);
    }

    /**
     * Generate terrain for a chunk, offset to prevent cascading
     *
     * @param event the event containing the required data
     */
    @SubscribeEvent
    public static void generate(@Nonnull PopulateChunkEvent.Pre event) {
        final World world = event.getWorld();
        if (world.provider.getDimension() != DimensionType.OVERWORLD.getId()) {
            // only apply this generation to the overworld
            return;
        }

        tryInitializeNoise(world);

        final int chunkX = event.getChunkX();
        final int chunkZ = event.getChunkZ();

        final int startX = chunkX << 4;
        final int startZ = chunkZ << 4;
        final int maxY = world.getActualHeight();

        final Chunk chunk = world.getChunk(chunkX, chunkZ);
        final ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < maxY; y++) {
            ExtendedBlockStorage storage = storages[y >> 4];
            // out of storages, so immediately exit
            if (storage == null || storage.isEmpty()) return;

            for (int xOffset = 0; xOffset < 16; xOffset++) {
                final int x = startX + xOffset;
                for (int zOffset = 0; zOffset < 16; zOffset++) {
                    final int z = startZ + zOffset;
                    pos.setPos(x, y, z);

                    final int storageY = y & 0xF;

                    IBlockState state = storage.get(xOffset, storageY, zOffset);
                    if (state.getBlock().isAir(state, world, pos)) continue;
                    if (!(state.getBlock() instanceof BlockStone)) continue;

                    final int surfaceY = getWorldSurfaceFast(world, chunk, xOffset, zOffset);
                    final IBlockState toSet = getStateFor(x, y, z, surfaceY);
                    if (toSet.getBlock().isAir(toSet, world, pos)) continue;

                    storage.set(xOffset, storageY, zOffset, toSet);
                }
            }
        }
    }

    private static void tryInitializeNoise(@Nonnull World world) {
        if (noise == null) {
            noise = new PerlinNoise(world.getSeed());
        }
    }

    /**
     * Faster way to retrieve the surface level of a world. Skips chunk loading checks.
     *
     * @param world the world containing the chunk
     * @param chunk the chunk containing the coordinates
     * @param x the x block coordinate within the chunk [0, 16)
     * @param z the z block coordinate within the chunk [0, 16)
     * @return the world surface height, guaranteed > 0
     */
    private static int getWorldSurfaceFast(@Nonnull World world, @Nonnull Chunk chunk, int x, int z) {
        // code simplified from World#getHeight(int, int)
        if (x >= -30_000_000 && z >= -30_000_000 && x < 30_000_000 && z < 30_000_000) {
            return Math.max(1, chunk.getHeightValue(x, z));
        }
        return Math.max(1, world.getSeaLevel() + 1);
    }

    @Nonnull
    private static IBlockState getStateFor(int x, int y, int z, int surfaceY) {
        assert noise != null;
        // need abs() for x and y, when generating blobs between - and + coords
        float noiseValue = noise.noise(Math.abs(x * 0.01F), y * 1.0F / surfaceY, Math.abs(z * 0.01F), 4, 0.1F);
        return stoneTypes.get((int) (stoneTypes.size() * noiseValue));
    }
}

