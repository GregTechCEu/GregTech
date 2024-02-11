package gregtech.api.worldgen.bedrockOres;

import crafttweaker.api.item.IItemStack;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTLog;
import gregtech.api.util.XSTR;
import gregtech.api.worldgen.bedrockFluids.ChunkPosDimension;
import gregtech.api.worldgen.config.BedrockOreDepositDefinition;

import gregtech.core.network.packets.PacketOreVeinList;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class BedrockOreVeinHandler {

    public final static LinkedHashMap<BedrockOreDepositDefinition, Integer> veinList = new LinkedHashMap<>();
    private final static Map<Integer, HashMap<Integer, Integer>> totalWeightMap = new HashMap<>();
    public static HashMap<ChunkPosDimension, OreVeinWorldEntry> veinCache = new HashMap<>();

    /**
     * 1: Original version
     * <br>
     * 2: Fixed interpretation of coordinates around axes
     */
    public static int saveDataVersion;

    public static final int VEIN_CHUNK_SIZE = 8; // veins are 8x8 chunk squares

    public static final int MAXIMUM_VEIN_OPERATIONS = 100_000;

    @Nullable
    public static OreVeinWorldEntry getOreVeinWorldEntry(@NotNull World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return null;

        ChunkPosDimension coords = new ChunkPosDimension(world.provider.getDimension(), getVeinCoord(chunkX),
                getVeinCoord(chunkZ));

        OreVeinWorldEntry worldEntry = veinCache.get(coords);
        if (worldEntry == null) {
            BedrockOreDepositDefinition definition = null;

            int query = world.getChunk(getVeinCoord(chunkX), getVeinCoord(chunkZ)).getRandomWithSeed(100210).nextInt();

            Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX << 4, 64, chunkZ << 4));
            int totalWeight = getTotalWeight(world.provider, biome);
            if (totalWeight > 0) {
                int weight = Math.abs(query % totalWeight);
                for (Map.Entry<BedrockOreDepositDefinition, Integer> entry : veinList.entrySet()) {
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

            int maximumDensity = 0;
            if (definition != null) {
                if (definition.getMaximumDensity() - definition.getMinimumDensity() <= 0) {
                    maximumDensity = definition.getMinimumDensity();
                } else {
                    maximumDensity = random.nextInt(definition.getMaximumDensity() - definition.getMinimumDensity()) +
                            definition.getMinimumDensity();
                }
                maximumDensity = Math.min(maximumDensity, definition.getMaximumDensity());
            }

            worldEntry = new OreVeinWorldEntry(definition, maximumDensity, MAXIMUM_VEIN_OPERATIONS);
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
        for (Map.Entry<BedrockOreDepositDefinition, Integer> entry : veinList.entrySet()) {
            if (entry.getKey().getDimensionFilter().test(provider)) {
                totalWeight += entry.getKey().getBiomeWeightModifier().apply(biome);
                totalWeight += entry.getKey().getWeight();
            }
        }

        // make sure the vein can generate if no biome weighting is added
        if (totalWeight == 0 && !veinList.isEmpty())
            GTLog.logger.error("Bedrock Ore Vein weight was 0 in biome {}", biome.biomeName);

        dimMap.put(biomeID, totalWeight);
        return totalWeight;
    }

    public static void addOreDeposit(BedrockOreDepositDefinition definition) {
        veinList.put(definition, definition.getWeight());
    }

    public static void recalculateChances(boolean mutePackets) {
        totalWeightMap.clear();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !mutePackets) {
            HashMap<OreVeinWorldEntry, Integer> packetMap = new HashMap<>();
            for (Map.Entry<ChunkPosDimension, OreVeinWorldEntry> entry : BedrockOreVeinHandler.veinCache
                    .entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null)
                    packetMap.put(entry.getValue(), entry.getValue().getDefinition().getWeight());
            }
            GregTechAPI.networkHandler.sendToAll(new PacketOreVeinList(packetMap));
        }
    }

    public static int getOreDensity(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null) return 0;
        return info.getOreDensity();
    }

    public static int getOperationsRemaining(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null) return 0;
        return info.getOperationsRemaining();
    }

    @Nullable
    public static Map<IBlockState, Integer> getOresInChunk(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.getDefinition() == null) {
            return null;
        }
        return info.getDefinition().getStoredOres();
    }

    public static int getTotalWeightInChunk(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.getDefinition() == null) return 0;
        return info.getDefinition().getTotalOreWeight();
    }

    public static ItemStack getStoneInChunk(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.getDefinition() == null) return null;
        return info.getDefinition().getDefaultDrop();
    }

    public static void depleteVein(World world, int chunkX, int chunkZ, int amount, boolean ignoreVeinStats) {
        BedrockOreVeinHandler.OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null) return;

        if (ignoreVeinStats) {
            info.decreaseOperations(amount);
            return;
        }

        BedrockOreDepositDefinition definition = info.getDefinition();

        // prevent division by zero, veins that never deplete don't need updating
        if (definition == null || definition.getDepletionChance() == 0)
            return;

        if (definition.getDepletionChance() == 100 || GTValues.RNG.nextInt(100) <= definition.getDepletionChance()) {
            info.decreaseOperations(definition.getDepletionAmount());
            BedrockOreVeinSaveData.setDirty();
        }
    }

    public static int getVeinCoord(int chunkCoord) {
        if (saveDataVersion >= 2) {
            return Math.floorDiv(chunkCoord, VEIN_CHUNK_SIZE);
        }
        return chunkCoord / VEIN_CHUNK_SIZE;
    }

    public static class OreVeinWorldEntry {

        private BedrockOreDepositDefinition vein;
        private int oreDensity;
        private int operationsRemaining;

        public OreVeinWorldEntry(BedrockOreDepositDefinition vein, int oreDensity, int operationsRemaining) {
            this.vein = vein;
            this.oreDensity = oreDensity;
            this.operationsRemaining = operationsRemaining;
        }

        private OreVeinWorldEntry() {}

        public BedrockOreDepositDefinition getDefinition() {
            return this.vein;
        }

        public int getOreDensity() {
            return this.oreDensity;
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
            tag.setInteger("oreDensity", oreDensity);
            tag.setInteger("operationsRemaining", operationsRemaining);
            if (vein != null) {
                tag.setString("vein", vein.getDepositName());
            }
            return tag;
        }

        @NotNull
        public static OreVeinWorldEntry readFromNBT(@NotNull NBTTagCompound tag) {
            OreVeinWorldEntry info = new OreVeinWorldEntry();
            info.oreDensity = tag.getInteger("oreDensity");
            info.operationsRemaining = tag.getInteger("operationsRemaining");

            if (tag.hasKey("vein")) {
                String s = tag.getString("vein");
                for (BedrockOreDepositDefinition definition : veinList.keySet()) {
                    // old save data can have deposit names with native separators, get rid of those
                    if (FileUtility.nativeSepToSlash(s).equalsIgnoreCase(definition.getDepositName()))
                        info.vein = definition;
                }
            }

            return info;
        }
    }
}
