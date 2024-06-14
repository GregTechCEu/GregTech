package gregtech.datafix.fixes;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.datafix.GTDataFixers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

import org.jetbrains.annotations.NotNull;

public class Fix0PostMTERegistriesItems implements IFixableData {

    private static final String OLD_BLOCK_ID = "gregtech:machine";
    private static final String NEW_BLOCK_ID_EXTENSION = ":mte";

    private static final String NEW_GT_BLOCK_ID = GTValues.MODID + NEW_BLOCK_ID_EXTENSION;

    @Override
    public int getFixVersion() {
        return GTDataFixers.V1_POST_MTE;
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        if (OLD_BLOCK_ID.equals(compound.getString("id"))) {
            final short meta = compound.getShort("Damage");
            if (GregTechAPI.mteManager.needsDataFix(meta)) {
                short fixedMeta = GregTechAPI.mteManager.getFixedMeta(meta);
                if (fixedMeta != meta) {
                    compound.setShort("Damage", fixedMeta);
                }

                String fixedName = GregTechAPI.mteManager.getFixedModid(meta);
                if (fixedName == null) {
                    compound.setString("id", NEW_GT_BLOCK_ID);
                } else {
                    compound.setString("id", fixedName + NEW_BLOCK_ID_EXTENSION);
                }
            }
        }

        return compound;
    }
}
