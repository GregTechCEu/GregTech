package gregtech.api.worldgen.bedrockFluids;

import gregtech.api.GTValues;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.CPacketFluidVeinList;
import gregtech.api.util.GTLog;
import gregtech.api.util.XSTR;
import gregtech.api.worldgen.config.BedrockFluidDepositDefinition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class BedrockFluidVeinHandler {

    public final static LinkedHashMap<BedrockFluidDepositDefinition, Integer> veinList = new LinkedHashMap<>();
    private final static Map<Integer, HashMap<Integer, Integer>> totalWeightMap = new HashMap<>();
    public static HashMap<ChunkPosDimension, FluidVeinWorldEntry> veinCache = new HashMap<>();

    private static final int veinChunkSize = 8; // veins are 8x8 chunk squares

    /**
     * Gets the FluidVeinWorldInfo object associated with the given chunk
     *
     * @param world  The world to retrieve
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The FluidVeinWorldInfo corresponding with the given chunk
     */
    @Nullable
    public static FluidVeinWorldEntry getFluidVeinWorldEntry(@Nonnull World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return null;

        Random random = new XSTR(31 * 31 * chunkX + chunkZ * 31 + Long.hashCode(world.getSeed()));

        ChunkPosDimension coords = new ChunkPosDimension(world.provider.getDimension(), chunkX / veinChunkSize, chunkZ / veinChunkSize);

        FluidVeinWorldEntry worldEntry = veinCache.get(coords);
        if (worldEntry == null) {
            BedrockFluidDepositDefinition definition = null;

            int query = world.getChunk(chunkX / veinChunkSize, chunkZ / veinChunkSize).getRandomWithSeed(90210).nextInt();

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

            int capacity = 0;
            if (definition != null) //todo scale capacity to be not 100% random
                capacity = Math.min(definition.getMaximumProductionRate(),
                        random.nextInt(definition.getMaximumProductionRate()) + definition.getMinimumProductionRate());

            worldEntry = new FluidVeinWorldEntry(definition, capacity, capacity);
            veinCache.put(coords, worldEntry);
        }
        return worldEntry;
    }

    /**
     * Gets the current production rate of fluid in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return rate of fluid in the given vein
     */
    public static int getFluidRateInChunk(@Nonnull World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return 0;
        return info.currentFluidAmount;
    }

    public static boolean isChunkDepleted(@Nonnull World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return true;
        return info.currentFluidAmount == 0;
    }

    /**
     * Gets the Fluid in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return Fluid in given vein
     */
    @Nullable
    public static Fluid getFluid(@Nonnull World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return null;

        BedrockFluidDepositDefinition definition = info.getVein();
        if (definition == null)
            return null;

        return definition.getStoredFluid();
    }

    /**
     * Gets the rate of fluid in the chunk after the vein is completely depleted
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return rate of fluid produced post depletion
     */
    public static int getDepletedFluidRate(@Nonnull World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return 0;
        return info.getVein().getDepletedProductionRate();
    }

    /**
     * Depletes fluid from a given chunk
     *
     * @param coefficient the depletion coefficient of the rig depleting the chunk
     * @param world       World whose chunk to drain
     * @param chunkX      Chunk x
     * @param chunkZ      Chunk z
     */
    public static void depleteVein(float coefficient, @Nonnull World world, int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.currentFluidAmount == 0)
            return;
        BedrockFluidDepositDefinition definition = info.getVein();

        double averageDecrease = definition.getDepletionChance() * definition.getDepletionAmount() * coefficient;
        int decrease = (int) Math.ceil(averageDecrease);
        if (GTValues.RNG.nextFloat() < (decrease - averageDecrease)) {
            info.currentFluidAmount = Math.max(0, info.currentFluidAmount - definition.getDepletionAmount());
            BedrockFluidVeinSaveData.setDirty();
        }
    }

    /**
     * Gets the total weight of all veins for the given dimension ID and biome type
     *
     * @param provider The WorldProvider whose dimension to check
     * @param biome    The biome type to check
     * @return The total weight associated with the dimension/biome pair
     */
    public static int getTotalWeight(@Nonnull WorldProvider provider, Biome biome) {
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
        if (totalWeight == 0)
            GTLog.logger.error("Bedrock Fluid Vein weight was 0 in biome {}", biome.getBiomeName());

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
            for (Map.Entry<ChunkPosDimension, FluidVeinWorldEntry> entry : BedrockFluidVeinHandler.veinCache.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null)
                    packetMap.put(entry.getValue(), entry.getValue().getVein().getWeight());
            }
            NetworkHandler.channel.sendToAll(new CPacketFluidVeinList(packetMap).toFMLPacket());
        }
    }

    public static class FluidVeinWorldEntry {
        private BedrockFluidDepositDefinition vein;
        private int maximumCapacity;
        private int currentFluidAmount;

        public FluidVeinWorldEntry(BedrockFluidDepositDefinition vein, int maximumCapacity, int currentFluidAmount) {
            this.vein = vein;
            this.maximumCapacity = maximumCapacity;
            this.currentFluidAmount = currentFluidAmount;
        }

        public FluidVeinWorldEntry(@Nonnull FluidVeinWorldEntry entry) {
            this.vein = entry.getVein();
            this.maximumCapacity = entry.maximumCapacity;
            this.currentFluidAmount = entry.currentFluidAmount;
        }

        private FluidVeinWorldEntry() {

        }

        public BedrockFluidDepositDefinition getVein() {
            return this.vein;
        }

        public int getCurrentFluidAmount() {
            return this.currentFluidAmount;
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("maximumCapacity", maximumCapacity);
            tag.setInteger("currentFluidAmount", currentFluidAmount);
            if (vein != null) {
                tag.setString("vein", vein.getDepositName());
            }
            return tag;
        }

        @Nonnull
        public static FluidVeinWorldEntry readFromNBT(@Nonnull NBTTagCompound tag) {
            FluidVeinWorldEntry info = new FluidVeinWorldEntry();
            info.maximumCapacity = tag.getInteger("maximumCapacity");
            info.currentFluidAmount = tag.getInteger("currentFluidAmount");

            if (tag.hasKey("vein")) {
                String s = tag.getString("vein");
                for (BedrockFluidDepositDefinition definition : veinList.keySet()) {
                    if (s.equalsIgnoreCase(definition.getDepositName()))
                        info.vein = definition;
                }
            }

            return info;
        }
    }
}
