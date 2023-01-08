package gregtech.common.datafix.fixes;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.datafix.GregTechDataFixers;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

import javax.annotation.Nonnull;

public class Fix0PostMachineBlockChange implements IFixableData {

    private static final String OLD_MACHINE_ID = "gregtech:machine";

    @Override
    public int getFixVersion() {
        return GregTechDataFixers.V0_POST_MACHINE_BLOCK_CHANGE;
    }

    @Nonnull
    @Override
    public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound) {
        String id = compound.getString("id");
        if (OLD_MACHINE_ID.equals(id)) {
            int damage = compound.getShort("Damage");
            MetaTileEntity mte = GregTechAPI.MTE_REGISTRY.getObjectById(damage);
            if (mte != null) {
                compound.setString("id", mte.getBlock().getRegistryName().toString());
            }
        }
        return compound;
    }
}
