package gregtech.api.metatileentity;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class MTETrait {

    private static final Object2IntFunction<String> traitIds = new Object2IntOpenHashMap<>();
    private static int rollingNetworkId = 0;

    private static final int NO_NETWORK_ID = -1;

    static {
        traitIds.defaultReturnValue(NO_NETWORK_ID);
    }

    protected final MetaTileEntity metaTileEntity;
    private final int networkId;

    /**
     * Create a new MetaTileEntity trait.
     *
     * @param metaTileEntity the MetaTileEntity to reference, and add the trait to
     */
    public MTETrait(@Nonnull MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;

        final String traitName = getName();
        int networkId = traitIds.getInt(traitName);
        if (networkId == NO_NETWORK_ID) {
            networkId = rollingNetworkId++;
            traitIds.put(traitName, networkId);
        }
        this.networkId = networkId;
        metaTileEntity.addMetaTileEntityTrait(this);
    }

    @Nonnull
    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    /**
     * @return the name of the MetaTileEntity Trait
     */
    @Nonnull
    public abstract String getName();

    /**
     * @return the network ID of the MetaTileEntity Trait
     */
    public final int getNetworkID() {
        return this.networkId;
    }

    public abstract <T> T getCapability(Capability<T> capability);

    public void onFrontFacingSet(EnumFacing newFrontFacing) {
    }

    public void update() {
    }

    @Nonnull
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
    }

    public void writeInitialData(@Nonnull PacketBuffer buffer) {
    }

    public void receiveInitialData(@Nonnull PacketBuffer buffer) {
    }

    public void receiveCustomData(int id, @Nonnull PacketBuffer buffer) {
    }

    public final void writeCustomData(int id, @Nonnull Consumer<PacketBuffer> writer) {
        metaTileEntity.writeTraitData(this, id, writer);
    }

    @Override
    public String toString() {
        return "MTETrait{" +
                "metaTileEntity=" + metaTileEntity +
                ", networkId=" + networkId +
                ", name='" + getName() + '\'' +
                '}';
    }
}
