package gregtech.api.worldgen.generator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import gregtech.api.util.GTUtility;
import gregtech.api.util.XSTR;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.api.worldgen.populator.IBlockModifierAccess;
import gregtech.api.worldgen.populator.IVeinPopulator;
import gregtech.api.worldgen.populator.VeinBufferPopulator;
import gregtech.api.worldgen.populator.VeinChunkPopulator;
import gregtech.api.worldgen.shape.IBlockGeneratorAccess;
import gregtech.common.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class CachedGridEntry implements GridEntryInfo, IBlockGeneratorAccess, IBlockModifierAccess {

    private static final Map<World, Cache<Long, CachedGridEntry>> gridEntryCache = new WeakHashMap<>();

    public static CachedGridEntry getOrCreateEntry(World world, int gridX, int gridZ, int primerChunkX, int primerChunkZ) {
        Cache<Long, CachedGridEntry> currentValue = gridEntryCache.get(world);
        if (currentValue == null) {
            currentValue = createGridCache();
            gridEntryCache.put(world, currentValue);
        }
        Long gridEntryKey = (long) gridX << 32 | gridZ & 0xFFFFFFFFL;
        CachedGridEntry gridEntry = currentValue.getIfPresent(gridEntryKey);
        if (gridEntry == null) {
            gridEntry = new CachedGridEntry(world, gridX, gridZ, primerChunkX, primerChunkZ);
            currentValue.put(gridEntryKey, gridEntry);
        }
        return gridEntry;
    }

    private static Cache<Long, CachedGridEntry> createGridCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(300)
                .expireAfterAccess(5L, TimeUnit.MINUTES)
                .build();
    }

    private final TLongObjectMap<ChunkDataEntry> dataByChunkPos = new TLongObjectHashMap<>();
    private static final Comparator<OreDepositDefinition> COMPARATOR = Comparator.comparing(OreDepositDefinition::getPriority).reversed();
    private static final BlockPos[] CHUNK_CORNER_SPOTS = new BlockPos[]{
            new BlockPos(0, 0, 0),
            new BlockPos(15, 0, 0),
            new BlockPos(0, 0, 15),
            new BlockPos(15, 0, 15)
    };

    private final Random gridRandom;
    private final int gridX;
    private final int gridZ;
    private final List<Entry<Integer, OreDepositDefinition>> cachedDepositMap;
    private GTWorldGenCapability masterEntry;
    private final int worldSeaLevel;
    private Map<OreDepositDefinition, BlockPos> veinGeneratedMap;

    private int veinCenterX, veinCenterY, veinCenterZ;
    private OreDepositDefinition currentOreVein;

    public CachedGridEntry(World world, int gridX, int gridZ, int primerChunkX, int primerChunkZ) {
        this.gridX = gridX;
        this.gridZ = gridZ;
        long worldSeed = world.getSeed();
        this.gridRandom = new XSTR(31 * 31 * gridX + gridZ * 31 + Long.hashCode(worldSeed));

        int gridSizeX = WorldGeneratorImpl.GRID_SIZE_X * 16;
        int gridSizeZ = WorldGeneratorImpl.GRID_SIZE_Z * 16;
        BlockPos blockPos = new BlockPos(gridX * gridSizeX + gridSizeX / 2, world.getActualHeight(), gridZ * gridSizeZ + gridSizeZ / 2);
        Biome currentBiome = world.getBiomeProvider().getBiome(blockPos);
        this.cachedDepositMap = new ArrayList<>(WorldGenRegistry.INSTANCE.getCachedBiomeVeins(world.provider, currentBiome));

        this.worldSeaLevel = world.getSeaLevel();
        this.masterEntry = searchMasterOrNull(world);
        if (masterEntry == null) {
            Chunk primerChunk = world.getChunk(primerChunkX, primerChunkZ);
            BlockPos heightSpot = findOptimalSpot(gridX, gridZ, primerChunkX, primerChunkZ);
            heightSpot = heightSpot.add(primerChunkX * 16, 0, primerChunkZ * 16);
            int masterHeight = world.getHeight(heightSpot).getY();
            int masterBottomHeight = world.getTopSolidOrLiquidBlock(heightSpot).getY();
            this.masterEntry = primerChunk.getCapability(GTWorldGenCapability.CAPABILITY, null);
            if(this.masterEntry == null) {
                this.masterEntry = new GTWorldGenCapability();
            }
            this.masterEntry.setMaxHeight(masterHeight, masterBottomHeight);
        }

        triggerVeinsGeneration();
    }

    private BlockPos findOptimalSpot(int gridX, int gridZ, int chunkX, int chunkZ) {
        int gridCenterX = (gridX * WorldGeneratorImpl.GRID_SIZE_X + WorldGeneratorImpl.GRID_SIZE_X / 2) * 16 + 7;
        int gridCenterZ = (gridZ * WorldGeneratorImpl.GRID_SIZE_Z + WorldGeneratorImpl.GRID_SIZE_Z / 2) * 16 + 7;
        int chunkBaseX = chunkX * 16;
        int chunkBaseZ = chunkZ * 16;
        BlockPos mostClosePos = null;
        double mostCloseDistance = Double.MAX_VALUE;
        for (BlockPos pos : CHUNK_CORNER_SPOTS) {
            double diffX = (chunkBaseX + pos.getX()) - gridCenterX;
            double diffZ = (chunkBaseZ + pos.getZ()) - gridCenterZ;
            double distance = diffX * diffX + diffZ * diffZ;
            if (mostCloseDistance > distance) {
                mostCloseDistance = distance;
                mostClosePos = pos;
            }
        }
        return mostClosePos;
    }

    private GTWorldGenCapability searchMasterOrNull(World world) {
        int gridSizeX = WorldGeneratorImpl.GRID_SIZE_X;
        int gridSizeZ = WorldGeneratorImpl.GRID_SIZE_Z;
        int startChunkX = gridX * gridSizeX;
        int startChunkZ = gridZ * gridSizeZ;
        for (int x = 0; x < gridSizeX; x++) {
            for (int z = 0; z < gridSizeZ; z++) {
                int chunkX = startChunkX + x;
                int chunkZ = startChunkZ + z;
                if (world.isChunkGeneratedAt(chunkX, chunkZ)) {
                    return retrieveCapability(world, chunkX, chunkZ);
                }
            }
        }
        return null;
    }

    @Override
    public int getTerrainHeight() {
        return masterEntry.getMaxHeight();
    }

    @Override
    public int getBottomHeight() {
        return masterEntry.getMaxBottomHeight();
    }

    @Override
    public int getSeaLevel() {
        return worldSeaLevel;
    }

    @Override
    public Set<OreDepositDefinition> getGeneratedVeins() {
        return veinGeneratedMap.keySet();
    }

    @Override
    public BlockPos getCenterPos(OreDepositDefinition definition) {
        return veinGeneratedMap.get(definition);
    }

    public boolean populateChunk(World world, int chunkX, int chunkZ, Random random) {
        long chunkId = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        ChunkDataEntry chunkDataEntry = dataByChunkPos.get(chunkId);
        GTWorldGenCapability capability = retrieveCapability(world, chunkX, chunkZ);
        capability.setFrom(masterEntry);
        if (chunkDataEntry != null && chunkDataEntry.populateChunk(world)) {
            for (OreDepositDefinition definition : chunkDataEntry.generatedOres) {
                IVeinPopulator veinPopulator = definition.getVeinPopulator();
                if (veinPopulator instanceof VeinChunkPopulator) {
                    ((VeinChunkPopulator) veinPopulator).populateChunk(world, chunkX, chunkZ, random, definition, this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Collection<IBlockState> getGeneratedBlocks(OreDepositDefinition definition, int chunkX, int chunkZ) {
        long chunkId = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        ChunkDataEntry chunkDataEntry = dataByChunkPos.get(chunkId);
        if (chunkDataEntry != null) {
            TLongSet longSet = chunkDataEntry.generatedBlocksSet.get(definition);
            ArrayList<IBlockState> blockStates = new ArrayList<>();
            TLongIterator iterator = longSet.iterator();
            while (iterator.hasNext())
                blockStates.add(Block.getStateById((int) iterator.next()));
            return blockStates;
        }
        return Collections.emptyList();
    }

    private GTWorldGenCapability retrieveCapability(World world, int chunkX, int chunkZ) {
        return world.getChunk(chunkX, chunkZ).getCapability(GTWorldGenCapability.CAPABILITY, null);
    }

    public void triggerVeinsGeneration() {
        this.veinGeneratedMap = new HashMap<>();
        if (!cachedDepositMap.isEmpty()) {
            int currentCycle = 0;
            int maxCycles = ConfigHolder.worldgen.minVeinsInSection + (ConfigHolder.worldgen.additionalVeinsInSection == 0 ? 0 : gridRandom.nextInt(ConfigHolder.worldgen.additionalVeinsInSection + 1));
            ArrayList<OreDepositDefinition> veins = new ArrayList<>();
            while (currentCycle < cachedDepositMap.size() && currentCycle < maxCycles) {
                //instead of removing already generated veins, we swap last element with one we selected
                int randomEntryIndex = GTUtility.getRandomItem(gridRandom, cachedDepositMap, cachedDepositMap.size() - currentCycle);
                OreDepositDefinition randomEntry = cachedDepositMap.get(randomEntryIndex).getValue();
                Collections.swap(cachedDepositMap, randomEntryIndex, cachedDepositMap.size() - 1 - currentCycle);
                //need to put into list first to apply priority properly, so
                //red granite vein will be properly filled with ores from other veins
                veins.add(randomEntry);
                if (!randomEntry.isVein())
                    maxCycles++;
                currentCycle++;
            }
            veins.sort(COMPARATOR);
            for (OreDepositDefinition depositDefinition : veins) {
                doGenerateVein(depositDefinition);
            }
        }
    }

    private void doGenerateVein(OreDepositDefinition definition) {
        this.currentOreVein = definition;

        int topHeightOffset = currentOreVein.getShapeGenerator().getMaxSize().getY() / 2 + 4;
        int maximumHeight = Math.min(masterEntry.getMaxBottomHeight(), currentOreVein.getHeightLimit()[1] - topHeightOffset);
        int minimumHeight = Math.max(3, currentOreVein.getHeightLimit()[0]);
        if (minimumHeight >= maximumHeight) {
            return;
        }
        this.veinCenterX = calculateVeinCenterX();
        this.veinCenterY = minimumHeight + gridRandom.nextInt(maximumHeight - minimumHeight);
        this.veinCenterZ = calculateVeinCenterZ();
        this.currentOreVein.getShapeGenerator().generate(gridRandom, this);
        this.veinGeneratedMap.put(definition, new BlockPos(veinCenterX, veinCenterY, veinCenterZ));
        IVeinPopulator veinPopulator = currentOreVein.getVeinPopulator();
        if (veinPopulator instanceof VeinBufferPopulator) {
            ((VeinBufferPopulator) veinPopulator).populateBlockBuffer(gridRandom, this, this, currentOreVein);
        }
        this.currentOreVein = null;
    }

    private int calculateVeinCenterX() {
        int gridSizeX = WorldGeneratorImpl.GRID_SIZE_X * 16;
        int offset = (ConfigHolder.worldgen.generateVeinsInCenterOfChunk && currentOreVein.isVein()) ? gridSizeX / 2 : gridRandom.nextInt(gridSizeX);
        return gridX * gridSizeX + offset;
    }

    private int calculateVeinCenterZ() {
        int gridSizeZ = WorldGeneratorImpl.GRID_SIZE_Z * 16;
        int offset = (ConfigHolder.worldgen.generateVeinsInCenterOfChunk && currentOreVein.isVein()) ? gridSizeZ / 2 : gridRandom.nextInt(gridSizeZ);
        return gridZ * gridSizeZ + offset;
    }

    @Override
    public boolean generateBlock(int x, int y, int z, boolean withRandom) {
        if (currentOreVein == null)
            throw new IllegalStateException("Attempted to call generateBlock without current ore vein!");
        int globalBlockX = veinCenterX + x;
        int globalBlockY = veinCenterY + y;
        int globalBlockZ = veinCenterZ + z;
        //we should do all random-related things here, otherwise it gets corrupted by current chunk information
        float randomDensityValue = gridRandom.nextFloat();

        if (withRandom && currentOreVein.getDensity() < randomDensityValue)
            return false; //only place blocks in positions matching density
        setBlock(globalBlockX, globalBlockY, globalBlockZ, currentOreVein, 0);
        return true;
    }

    @Override
    public boolean setBlock(int x, int y, int z, int index) {
        if (currentOreVein == null)
            throw new IllegalStateException("Attempted to call generateBlock without current ore vein!");
        int globalBlockX = veinCenterX + x;
        int globalBlockY = veinCenterY + y;
        int globalBlockZ = veinCenterZ + z;
        setBlock(globalBlockX, globalBlockY, globalBlockZ, currentOreVein, index + 1);
        return true;
    }

    private void setBlock(int worldX, int worldY, int worldZ, OreDepositDefinition definition, int index) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;
        int localX = worldX - chunkX * 16;
        int localZ = worldZ - chunkZ * 16;
        if (worldY > 0) {
            long chunkKey = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
            ChunkDataEntry dataEntry = dataByChunkPos.get(chunkKey);
            if (dataEntry == null) {
                dataEntry = new ChunkDataEntry(chunkX, chunkZ, gridRandom);
                dataByChunkPos.put(chunkKey, dataEntry);
            }
            dataEntry.setBlock(localX, worldY, localZ, definition, index);
        }
    }


    public static class ChunkDataEntry {

        private final Map<OreDepositDefinition, MutablePair<TLongList, Integer>> oreBlocks = new HashMap<>();
        private final Map<OreDepositDefinition, TLongSet> generatedBlocksSet = new HashMap<>();
        private final List<OreDepositDefinition> generatedOres = new ArrayList<>();
        private final int chunkX;
        private final int chunkZ;
        private final Random gridRandom;

        public ChunkDataEntry(int chunkX, int chunkZ, Random gridRandom) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.gridRandom = gridRandom;
        }

        public void setBlock(int x, int y, int z, OreDepositDefinition definition, int index) {
            int xzValue = (x & 0xFF) | ((z & 0xFF) << 8) | ((y & 0xFF) << 16);
            long blockIndex = (long) xzValue << 32 | index & 0xFFFFFFFFL;
            MutablePair<TLongList, Integer> pair = oreBlocks.get(definition);
            TLongList longList;
            if (pair == null) {
                longList = new TLongArrayList();
                oreBlocks.put(definition, MutablePair.of(longList, y));
            } else {
                longList = pair.getLeft();
                if (y < pair.getRight()) {
                    pair.setRight(y);
                }
            }
            longList.add(blockIndex);
        }

        public boolean populateChunk(World world) {
            MutableBlockPos blockPos = new MutableBlockPos();
            boolean generatedAnything = false;
            for (Map.Entry<OreDepositDefinition, MutablePair<TLongList, Integer>> entry : oreBlocks.entrySet()) {
                OreDepositDefinition definition = entry.getKey();
                TLongList blockIndexList = entry.getValue().getLeft();
                int lowestY = entry.getValue().getRight();
                TLongSet generatedBlocks = new TLongHashSet();
                boolean generatedOreVein = false;
                for (int i = 0; i < blockIndexList.size(); i++) {
                    long blockIndex = blockIndexList.get(i);
                    int xyzValue = (int) (blockIndex >> 32);
                    int blockX = (byte) xyzValue;
                    int blockZ = (byte) (xyzValue >> 8);
                    int blockY = (short) (xyzValue >> 16);
                    int index = (int) blockIndex;
                    blockPos.setPos(chunkX * 16 + blockX, blockY, chunkZ * 16 + blockZ);
                    IBlockState currentState = world.getBlockState(blockPos);
                    IBlockState newState;
                    if (index == 0) {
                        //it's primary ore block
                        if (!definition.getGenerationPredicate().test(currentState, world, blockPos))
                            continue; //do not generate if predicate didn't match
                        newState = definition.getBlockFiller().apply(currentState, world, blockPos, blockX, blockY, blockZ, definition.getDensity(), gridRandom, blockY - lowestY);
                    } else {
                        //it's populator-generated block with index
                        VeinBufferPopulator populator = (VeinBufferPopulator) definition.getVeinPopulator();
                        newState = populator.getBlockByIndex(world, blockPos, index - 1);
                    }
                    //set flags as 16 to avoid observer updates loading neighbour chunks
                    world.setBlockState(blockPos, newState, 16);
                    generatedBlocks.add(Block.getStateId(newState));
                    generatedOreVein = true;
                    generatedAnything = true;
                }
                if (generatedOreVein) {
                    this.generatedBlocksSet.put(definition, generatedBlocks);
                    this.generatedOres.add(definition);
                }
            }
            return generatedAnything;
        }
    }
}
