package gregtech.datafix.migration.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MTEMigrator {

    /**
     * @return the fix version
     */
    int fixVersion();

    /**
     * @param original the original MTE registry name
     * @return the new MTE registry name
     */
    @Nullable
    ResourceLocation fixMTEid(@NotNull ResourceLocation original);

    /**
     * @param original the original MTE registry name
     * @param tag      the MTE's "MetaTileEntity" tag to fix
     */
    void fixMTEData(@NotNull ResourceLocation original, @NotNull NBTTagCompound tag);

    /**
     * @param itemName the original name for the ItemBlock of the MTE
     * @param meta     the original metadata for the ItemBlock
     * @return the new metadata, or the old meta if no migration is needed
     */
    short fixItemMeta(@NotNull ResourceLocation itemName, short meta);

    /**
     * @param original     the original name for the ItemBlock of the MTE
     * @param originalMeta the original metadata for the ItemBlock's ItemStack
     * @return the new name for the ItemBlock, or null if no migration is needed
     */
    @Nullable
    ResourceLocation fixItemName(@NotNull ResourceLocation original, short originalMeta);
}
