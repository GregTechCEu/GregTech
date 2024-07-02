package gregtech.datafix.migration.impl;

import gregtech.datafix.migration.api.MTEMigrator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;

import org.jetbrains.annotations.NotNull;

import static gregtech.datafix.util.DataFixConstants.*;

public class MigrateMTEItems implements IFixableData {

    private final MTEMigrator migrator;

    public MigrateMTEItems(@NotNull MTEMigrator migrator) {
        this.migrator = migrator;
    }

    @Override
    public int getFixVersion() {
        return migrator.fixVersion();
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        final String id = compound.getString(ITEM_ID);
        if (id.isEmpty()) {
            return compound;
        }

        // must check hasKey() since non-items can have ITEM_ID but not have ITEM_DAMAGE and ITEM_COUNT
        if (compound.hasKey(ITEM_DAMAGE) && compound.hasKey(ITEM_COUNT)) {
            ResourceLocation itemBlockId = new ResourceLocation(id);
            final short meta = compound.getShort(ITEM_DAMAGE);
            ResourceLocation fixedName = migrator.fixItemName(itemBlockId, meta);
            if (fixedName != null) {
                compound.setString(ITEM_ID, fixedName.toString());
            }

            short fixedMeta = migrator.fixItemMeta(itemBlockId, meta);
            if (fixedMeta != meta) {
                compound.setShort(ITEM_DAMAGE, fixedMeta);
            }
        }

        return compound;
    }
}
