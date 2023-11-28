package gregtech.api.worldgen.bedrockFluids;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTLog;
import gregtech.api.util.XSTR;
import gregtech.api.worldgen.config.BedrockFluidDepositDefinition;
import gregtech.core.network.packets.PacketFluidVeinList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class BedrockFluidVeinHandler {

    public final static LinkedHashMap<BedrockFluidDepositDefinition, Integer> veinList = new LinkedHashMap<>();
    private final static Map<Integer, HashMap<Integer, Integer>> totalWeightMap = new HashMap<>();
    public static HashMap<ChunkPosDimension, FluidVeinWorldEntry> veinCache = new HashMap<>();

    /**
     * 1: Original version
     * <br>
     * 2: Fixed interpretation of coordinates around axes
     */
    public static int saveDataVersion;

    public static final int MAX_FLUID_SAVE_DATA_VERSION = 2;

    public static final int VEIN_CHUNK_SIZE = 8; // veins are 8x8 chunk squares

    public static final int MAXIMUM_VEIN_OPERATIONS = 100_000;

    /**
     * Gets the FluidVeinWorldInfo object associated with the given chunk
     *
     * @param world  The world to retrieve
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The FluidVeinWorldInfo corresponding with the given chunk
     */
    @Nullable
    public static FluidVeinWorldEntry getFluidVeinWorldEntry(@NotNull World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return null;

        ChunkPosDimension coords = new ChunkPosDimension(world.provider.getDimension(), getVeinCoord(chunkX),
                getVeinCoord(chunkZ));

        FluidVeinWorldEntry worldEntry = veinCache.get(coords);
        if (worldEntry == null) {
            BedrockFluidDepositDefinition definition = null;

            int query = world.getChunk(getVeinCoord(chunkX), getVeinCoord(chunkZ)).getRandomWithSeed(90210).nextInt();

            Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX << 4, 64, chunkZ << 4));
            int totalWeight = getTotalWeight(world.provider, biome);
            if (totalWeight > 0) {
                int weight = Math.abs(query % totalWeight);
                for (Map.Entry<BedrockFluidDepositDefinition, Integer> entry : veinList.entrySet()) {
                    int veinWeight = entry.getValue() + entry.getKey().getBiomeWeightModifier().apply(biome);
                    if (veinWeight > 0 && entry.getKey().getDimensionFilter().test(world.provider)) {
                        weight -= veinWeight;
                        if (weight < 0) {
                            definition = entry.getKey();
                            break;
                        }
                    }
                }
            }

            Random random = new XSTR(31L * 31 * chunkX + chunkZ * 31L + Long.hashCode(world.getSeed()));

            int maximumYield = 0;
            if (definition != null) {
                if (definition.getMaximumYield() - definition.getMinimumYield() <= 0) {
                    maximumYield = definition.getMinimumYield();
                } else {
                    maximumYield = random.nextInt(definition.getMaximumYield() - definition.getMinimumYield()) +
                            definition.getMinimumYield();
                }
                maximumYield = Math.min(maximumYield, definition.getMaximumYield());
            }

            worldEntry = new FluidVeinWorldEntry(definition, maximumYield, MAXIMUM_VEIN_OPERATIONS);
            veinCache.put(coords, worldEntry);
        }
        return worldEntry;
    }

    /**
     * Gets the total weight of all veins for the given dimension ID and biome type
     *
     * @param provider The WorldProvider whose dimension to check
     * @param biome    The biome type to check
     * @return The total weight associated with the dimension/biome pair
     */
    public static int getTotalWeight(@NotNull WorldProvider provider, Biome biome) {
        int dim = provider.getDimension();
        if (!totalWeightMap.containsKey(dim)) {
            totalWeightMap.put(dim, new HashMap<>());
        }

        Map<Integer, Integer> dimMap = totalWeightMap.get(dim);
        int biomeID = Biome.getIdForBiome(biome);

        if (dimMap.containsKey(biomeID)) {
            return dimMap.get(biomeID);
        }

        int totalWeight = 0;
        for (Map.Entry<BedrockFluidDepositDefinition, Integer> entry : veinList.entrySet()) {
            if (entry.getKey().getDimensionFilter().test(provider)) {
                totalWeight += entry.getKey().getBiomeWeightModifier().apply(biome);
                totalWeight += entry.getKey().getWeight();
            }
        }

        // make sure the vein can generate if no biome weighting is added
        if (totalWeight == 0 && !veinList.isEmpty())
            GTLog.logger.error("Bedrock Fluid Vein weight was 0 in biome {}", biome.biomeName);

        dimMap.put(biomeID, totalWeight);
        return totalWeight;
    }

    /**
     * Adds a vein to the pool of veins
     *
     * @param definition the vein to add
     */
    public static void addFluidDeposit(BedrockFluidDepositDefinition definition) {
        veinList.put(definition, definition.getWeight());
    }

    public static void recalculateChances(boolean mutePackets) {
        totalWeightMap.clear();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !mutePackets) {
            HashMap<FluidVeinWorldEntry, Integer> packetMap = new HashMap<>();
            for (Map.Entry<ChunkPosDimension, FluidVeinWorldEntry> entry : BedrockFluidVeinHandler.veinCache
                    .entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null)
                    packetMap.put(entry.getValue(), entry.getValue().getDefinition().getWeight());
            }
            GregTechAPI.networkHandler.sendToAll(new PacketFluidVeinList(packetMap));
        }
    }

    /**
     * gets the fluid yield in a specific chunk
     *
     * @param world  the world to retrieve it from
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return yield in the vein
     */
    public static int getFluidYield(World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null) return 0;
        return info.getFluidYield();
    }

    /**
     * Gets the yield of fluid in the chunk after the vein is completely depleted
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return yield of fluid post depletion
     */
    public static int getDepletedFluidYield(World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.getDefinition() == null) return 0;
        return info.getDefinition().getDepletedYield();
    }

    /**
     * Gets the current operations remaining in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return amount of operations in the given chunk
     */
    public static int getOperationsRemaining(World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null) return 0;
        return info.getOperationsRemaining();
    }

    /**
     * Gets the Fluid in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return Fluid in given chunk
     */
    @Nullable
    public static Fluid getFluidInChunk(World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.getDefinition() == null) return null;
        return info.getDefinition().getStoredFluid();
    }

    /**
     * Depletes fluid from a given chunk
     *
     * @param world           World whose chunk to drain
     * @param chunkX          Chunk x
     * @param chunkZ          Chunk z
     * @param amount          the amount of fluid to deplete the vein by
     * @param ignoreVeinStats whether to ignore the vein's depletion data, if false ignores amount
     */
    public static void depleteVein(World world, int chunkX, int chunkZ, int amount, boolean ignoreVeinStats) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null) return;

        if (ignoreVeinStats) {
            info.decreaseOperations(amount);
            return;
        }

        BedrockFluidDepositDefinition definition = info.getDefinition();

        // prevent division by zero, veins that never deplete don't need updating
        if (definition == null || definition.getDepletionChance() == 0)
            return;

        if (definition.getDepletionChance() == 100 || GTValues.RNG.nextInt(100) <= definition.getDepletionChance()) {
            info.decreaseOperations(definition.getDepletionAmount());
            BedrockFluidVeinSaveData.setDirty();
        }
    }

    public static int getVeinCoord(int chunkCoord) {
        if (saveDataVersion >= 2) {
            return Math.floorDiv(chunkCoord, VEIN_CHUNK_SIZE);
        }
        return chunkCoord / VEIN_CHUNK_SIZE;
    }

    public static class FluidVeinWorldEntry {

        private BedrockFluidDepositDefinition vein;
        private int fluidYield;
        private int operationsRemaining;

        public FluidVeinWorldEntry(BedrockFluidDepositDefinition vein, int fluidYield, int operationsRemaining) {
            this.vein = vein;
            this.fluidYield = fluidYield;
            this.operationsRemaining = operationsRemaining;
        }

        private FluidVeinWorldEntry() {}

        public BedrockFluidDepositDefinition getDefinition() {
            return this.vein;
        }

        public int getFluidYield() {
            return this.fluidYield;
        }

        public int getOperationsRemaining() {
            return this.operationsRemaining;
        }

        @SuppressWarnings("unused")
        public void setOperationsRemaining(int amount) {
            this.operationsRemaining = amount;
        }

        public void decreaseOperations(int amount) {
            operationsRemaining = Math.max(0, operationsRemaining - amount);
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("fluidYield", fluidYield);
            tag.setInteger("operationsRemaining", operationsRemaining);
            if (vein != null) {
                tag.setString("vein", vein.getDepositName());
            }
            return tag;
        }

        @NotNull
        public static FluidVeinWorldEntry readFromNBT(@NotNull NBTTagCompound tag) {
            FluidVeinWorldEntry info = new FluidVeinWorldEntry();
            info.fluidYield = tag.getInteger("fluidYield");
            info.operationsRemaining = tag.getInteger("operationsRemaining");

            if (tag.hasKey("vein")) {
                String s = tag.getString("vein");
                for (BedrockFluidDepositDefinition definition : veinList.keySet()) {
                    // old save data can have deposit names with native separators, get rid of those
                    if (FileUtility.nativeSepToSlash(s).equalsIgnoreCase(definition.getDepositName()))
                        info.vein = definition;
                }
            }

            return info;
        }
    }
}
