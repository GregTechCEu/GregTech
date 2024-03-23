package gregtech.api.pollution;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class PollutionData {

    private final int dimension;
    private final long pos;
    private int pollution;

    PollutionData(int dimension, long pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    int pollution() {
        return pollution;
    }

    /**
     * If the data is intended to be saved to disk, call {@link PollutionSaveData#markDirty()} after mutating.
     * @param pollution the amount of pollution to set
     */
    void setPollution(int pollution) {
        this.pollution = pollution;
    }

    int dimension() {
        return dimension;
    }

    long pos() {
        return pos;
    }

    /**
     * @return if this is a default instance and should not be saved to disk
     */
    private boolean isDefault() {
        return this.pollution == 0;
    }

    /**
     * @return the NBT tag representing this data, or {@code null} if it does not need serialization
     */
    @ApiStatus.Internal
    public @Nullable NBTTagCompound serializeToNBT() {
        if (isDefault()) {
            return null;
        }

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dimension", this.dimension);
        compound.setLong("pos", this.pos);
        compound.setInteger("pollution", this.pollution);
        return compound;
    }

    /**
     * @param compound the compound to read from
     * @return the data represented by the compound
     */
    @ApiStatus.Internal
    public static @NotNull PollutionData deserializeFromNBT(@NotNull NBTTagCompound compound) {
        PollutionData data = new PollutionData(compound.getInteger("dimension"), compound.getLong("pos"));
        data.setPollution(compound.getInteger("pollution"));
        return data;
    }

    @Override
    public @NotNull String toString() {
        return "PollutionData{" +
                "dimension=" + dimension +
                ", pos{" +
                "x=" + (pos & 0xFFFFFFFFL) +
                ", z=" + ((pos >> 32) & 0xFFFFFFFFL) +
                "}, pollution=" + pollution +
                '}';
    }
}
