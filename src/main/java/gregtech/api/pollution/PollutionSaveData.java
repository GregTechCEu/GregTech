package gregtech.api.pollution;

import gregtech.api.GTValues;
import gregtech.api.recipes.ingredients.nbtmatch.NBTTagType;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * World Save Data for Pollution
 * <p>
 * Do not interface with this class directly, use {@link gregtech.api.GregTechAPI#pollutionManager}
 */
@ApiStatus.Internal
public final class PollutionSaveData extends WorldSavedData {

    public static final String NAME = GTValues.MODID + ".pollution";

    private final Int2ObjectMap<Long2ObjectMap<PollutionData>> data = new Int2ObjectOpenHashMap<>();
    private Version version;

    /**
     * Is called by MC with reflection.
     *
     * @param name should always be {@link PollutionSaveData#NAME}
     */
    public PollutionSaveData(@NotNull String name) {
        super(name);
    }

    /**
     * @return the version of the data being stored
     */
    public @NotNull Version version() {
        if (this.version == null) {
            throw new IllegalStateException("Version was not initialized");
        }
        return this.version;
    }

    public void setVersion(@NotNull Version version) {
        if (this.version == null) {
            this.version = version;
        } else {
            throw new IllegalArgumentException("Version is already set");
        }
    }

    /**
     * @param dimension the dimension containing the pollution
     * @param chunkX    the chunk's x coordinate
     * @param chunkZ    the chunk's z coordinate
     * @return the pollution data for the chunk
     */
    @NotNull PollutionData get(int dimension, int chunkX, int chunkZ) {
        return get(dimension, ChunkPos.asLong(chunkX, chunkZ));
    }

    /**
     * @param dimension the dimension containing the pollution
     * @param pos       the position of the chunk in chunk coordinates encoded with {@link ChunkPos#asLong(int, int)},
     *                  containing the pollution
     * @return the pollution data for the chunk
     */
    @NotNull PollutionData get(int dimension, long pos) {
        var map = data.get(dimension);
        if (map == null) {
            map = new Long2ObjectOpenHashMap<>();
            data.put(dimension, map);
        }

        PollutionData pollutionData = map.get(pos);
        if (pollutionData == null) {
            pollutionData = new PollutionData(dimension, pos);
            map.put(pos, pollutionData);
        }
        return pollutionData;
    }

    /**
     * @param dimension the dimension to get data for
     * @return the data for the dimension
     */
    @Nullable @UnmodifiableView Long2ObjectMap<PollutionData> getData(int dimension) {
        return data.get(dimension);
    }

    /**
     * Clear the data, call on world unload
     */
    void clear() {
         data.clear();
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        data.clear();

        Version dataVersion = Version.readFromNBT(nbt);
        if (dataVersion == null) {
            GTLog.logger.warn("Could not read Pollution Save Data Version");
            return;
        } else if (this.version == null) {
            this.version = dataVersion;
        } else if (this.version != dataVersion) {
            throw new IllegalStateException("PollutionData current version " + this.version + " mismatched with read version " + dataVersion);
        }

        if (this.version == Version.V1) {
            NBTTagList list = nbt.getTagList("chunks", NBTTagType.COMPOUND.typeId);
            for (NBTBase base : list) {
                if (base instanceof NBTTagCompound serialized) {
                    PollutionData pollutionData = PollutionData.deserializeFromNBT(serialized);
                    Long2ObjectMap<PollutionData> map = data.get(pollutionData.dimension());
                    if (map == null) {
                        map = new Long2ObjectOpenHashMap<>();
                        data.put(pollutionData.dimension(), map);
                    }
                    map.put(pollutionData.pos(), pollutionData);
                }
            }
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        this.version.writeToNBT(nbt);
        if (this.version == Version.V1) {
            NBTTagList list = new NBTTagList();
            nbt.setTag("chunks", list);

            for (var entry : data.values()) {
                for (PollutionData pollutionData : entry.values()) {
                    NBTTagCompound serialized = pollutionData.serializeToNBT();
                    if (serialized != null) {
                        list.appendTag(serialized);
                    }
                }
            }
        }
        return nbt;
    }

    public enum Version {
        V1
        ;

        private static final Version[] VALUES = values();

        void writeToNBT(@NotNull NBTTagCompound tagCompound) {
            tagCompound.setInteger("version", ordinal() + 1);
        }

        static @Nullable Version readFromNBT(@NotNull NBTTagCompound tagCompound) {
            int version = tagCompound.getInteger("version");
            if (version <= 0 || version > VALUES.length) return null;
            return VALUES[version - 1];
        }
    }
}
