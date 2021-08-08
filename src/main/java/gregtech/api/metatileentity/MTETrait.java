package gregtech.api.metatileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.function.Consumer;

public abstract class MTETrait {

    protected final MetaTileEntity metaTileEntity;

    public MTETrait(MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        metaTileEntity.addMetaTileEntityTrait(this);
    }

    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    public abstract String getName();

    public abstract int getNetworkID();

    public abstract <T> T getCapability(Capability<T> capability);

    public void onFrontFacingSet(EnumFacing newFrontFacing) {
    }

    public void update() {
    }

    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    public void deserializeNBT(NBTTagCompound compound) {
    }

    public void writeInitialData(PacketBuffer buffer) {
    }

    public void receiveInitialData(PacketBuffer buffer) {
    }

    public void receiveCustomData(int id, PacketBuffer buffer) {
    }

    public final void writeCustomData(int id, Consumer<PacketBuffer> writer) {
        metaTileEntity.writeTraitData(this, id, writer);
    }

    protected static final class TraitNetworkIds {
        public static final int TRAIT_ID_ENERGY_CONTAINER = 1;
        public static final int TRAIT_ID_WORKABLE = 2;
    }

}
