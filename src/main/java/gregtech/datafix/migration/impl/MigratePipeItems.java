package gregtech.datafix.migration.impl;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.datafix.util.DataFixConstants.*;

public class MigratePipeItems implements IFixableData {

    private final int version;

    public MigratePipeItems(int version) {
        this.version = version;
    }

    @Override
    public int getFixVersion() {
        return version;
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        final String id = compound.getString(ITEM_ID);
        if (id.isEmpty()) {
            return compound;
        }

        ResourceLocation itemBlockId = new ResourceLocation(id);
        ResourceLocation fixedName = fixItemName(itemBlockId);
        if (fixedName != null) {
            compound.setString(ITEM_ID, fixedName.toString());
        }

        return compound;
    }

    private static @Nullable ResourceLocation fixItemName(@NotNull ResourceLocation itemBlockId) {
        return MigratePipeBlockTE.fixBlockRegistryName(itemBlockId);
    }
}
