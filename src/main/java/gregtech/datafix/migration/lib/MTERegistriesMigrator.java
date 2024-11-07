package gregtech.datafix.migration.lib;

import gregtech.api.util.GTUtility;
import gregtech.datafix.GTDataVersion;
import gregtech.datafix.migration.api.AbstractMTEMigrator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

/**
 * Performs migration to an independent MTE registry.
 * <p>
 * <strong>This only performs migration to a new registry.</strong>
 * Use {@link MTEDataMigrator} for a generic migration.
 */
public final class MTERegistriesMigrator extends AbstractMTEMigrator {

    private static final ResourceLocation OLD_BLOCK_ID = GTUtility.gregtechId("machine");
    private static final String NEW_BLOCK_NAME = "mte";

    private final Short2ObjectMap<String> metaModidMap = new Short2ObjectOpenHashMap<>();
    private final Short2ShortMap metaMetaMap = new Short2ShortOpenHashMap();

    @ApiStatus.Internal
    public MTERegistriesMigrator() {
        super(GTDataVersion.V1_POST_MTE.ordinal());
        metaMetaMap.defaultReturnValue((short) -1);
    }

    /**
     * Register a data fix entry for the multiple MTE registry transition with an additional metadata transition.
     * <p>
     * The general migration is automatically performed.
     *
     * @param preMeta  the original MTE metadata value
     * @param postMeta the new MTE metadata value
     */
    public void migrate(int preMeta, int postMeta) {
        metaMetaMap.put((short) preMeta, (short) postMeta);
    }

    /**
     * Register a single data fix entry for the multiple MTE registry transition.
     * <p>
     * Callers will probably prefer {@link #migrate(String, IntStream)}.
     *
     * @param modid the registry's modid
     * @param meta  the metadata value to migrate
     */
    public void migrate(@NotNull String modid, short meta) {
        metaModidMap.put(meta, modid);
    }

    /**
     * Register data fix entries for the multiple MTE registry transition.
     * <p>
     * Register the range of values allocated to the modid using {@link IntStream#range(int, int)} or
     * {@link IntStream#builder()}.
     *
     * @param modid      the registry's modid
     * @param metaValues the metadata values to migrate
     */
    public void migrate(@NotNull String modid, @NotNull IntStream metaValues) {
        metaValues.forEach(i -> metaModidMap.put((short) i, modid));
    }

    @Override
    public @Nullable ResourceLocation fixMTEid(@NotNull ResourceLocation original) {
        return null;
    }

    @Override
    public void fixMTEData(@NotNull ResourceLocation original, @NotNull NBTTagCompound tag) {}

    @Override
    public @Nullable ResourceLocation fixItemName(@NotNull ResourceLocation original, short originalMeta) {
        if (OLD_BLOCK_ID.equals(original)) {
            String modid = metaModidMap.get(originalMeta);
            if (modid == null) {
                return null;
            }
            return new ResourceLocation(modid, NEW_BLOCK_NAME);
        }
        return null;
    }

    @Override
    public short fixItemMeta(@NotNull ResourceLocation itemName, short meta) {
        short fixed = metaMetaMap.get(meta);
        return fixed < 0 ? meta : fixed;
    }
}
