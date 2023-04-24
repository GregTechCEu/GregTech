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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GregTechTerrainGen {

    private static GregTechTerrainGen DEFUALT_INSTANCE;

    private final List<IBlockState> stoneTypes;
    private PerlinNoise noise;

    public static void init() {
        List<IBlockState> stoneTypes = new ArrayList<>();
        for (StoneVariantBlock.StoneType type : StoneVariantBlock.StoneType.values()) {
            stoneTypes.add(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(type));
        }
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE));
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE));
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE));
        stoneTypes.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE));

        DEFUALT_INSTANCE = new GregTechTerrainGen(stoneTypes);
    }

    public GregTechTerrainGen(@Nonnull List<IBlockState> stoneTypes) {
        MinecraftForge.EVENT_BUS.register(this);
        this.stoneTypes = stoneTypes;
    }

    /**
     * Generate terrain for a chunk, offset to prevent cascading
     *
     * @param event the event containing the required data
     */
    @SubscribeEvent
    public void generate(@Nonnull PopulateChunkEvent.Pre event) {
        final World world = event.getWorld();
        if (world.provider.getDimension() != DimensionType.OVERWORLD.getId()) {
            // only apply this generation to the overworld
            return;
        }

        if (this.noise == null) {
            this.noise = new PerlinNoise(world.getSeed());
        }

        final int chunkX = event.getChunkX();
        final int chunkZ = event.getChunkZ();

        final int startX = chunkX * 16 + 8;
        final int startZ = chunkZ * 16 + 8;
        final int maxY = world.getActualHeight();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < maxY; y++) {
            for (int xOffset = 0; xOffset < 16; xOffset++) {
                final int x = startX + xOffset;
                for (int zOffset = 0; zOffset < 16; zOffset++) {
                    final int z = startZ + zOffset;
                    final int surface = world.getHeight(x, z);
                    pos.setPos(x, y, z);

                    IBlockState state = world.getBlockState(pos);
                    if (state.getBlock().isAir(state, world, pos)) continue;
                    if (!(state.getBlock() instanceof BlockStone)) continue;

                    world.setBlockState(pos, getBlockFor(x, y, z, Math.max(1, surface)), 2 | 16);
                }
            }
        }
    }

    @Nonnull
    private IBlockState getBlockFor(int x, int y, int z, int surfaceY) {
        assert this.noise != null;
        // need abs() for x and y, when generating blobs between - and + coords
        double noiseValue = noise.noise(Math.abs(x * 0.01F), y * 1F / surfaceY, Math.abs(z * 0.01F), 4, 0.1);
        return stoneTypes.get((int) (stoneTypes.size() * noiseValue));
    }
}

