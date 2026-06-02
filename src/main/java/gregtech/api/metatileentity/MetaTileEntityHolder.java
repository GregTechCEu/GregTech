package gregtech.api.metatileentity;

import gregtech.api.GregTechAPI;
import gregtech.api.cover.Cover;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.capability.GregtechDataCodes.INITIALIZE_MTE;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.10")
@InterfaceList(value = {
        @Interface(iface = "appeng.api.networking.security.IActionHost",
                   modid = Mods.Names.APPLIED_ENERGISTICS2,
                   striprefs = true),
        @Interface(iface = "appeng.me.helpers.IGridProxyable",
                   modid = Mods.Names.APPLIED_ENERGISTICS2,
                   striprefs = true) })
public class MetaTileEntityHolder extends GTBaseTileEntity {

    MetaTileEntity metaTileEntity;

    @Override
    public MetaTileEntity copy() {
        return metaTileEntity == null ? null : metaTileEntity.copy();
    }

    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    /**
     * Sets this holder's current meta tile entity to copy of given one
     * Note that this method copies given meta tile entity and returns actual instance
     * so it is safe to call it on sample meta tile entities
     * Also can use certain data to preinit the block before data is synced
     */
    @Override
    public MetaTileEntity setMetaTileEntity(@NotNull MetaTileEntity sampleMetaTileEntity,
                                            @Nullable NBTTagCompound tagCompound) {
        Preconditions.checkNotNull(sampleMetaTileEntity, "metaTileEntity");
        setRawMetaTileEntity(sampleMetaTileEntity.copy());
        return super.setMetaTileEntity(sampleMetaTileEntity, tagCompound);
    }

    protected void setRawMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        this.metaTileEntity.holder = this;
    }

    @Override
    public void invalidate() {
        if (metaTileEntity != null) {
            metaTileEntity.invalidate();
        }
        super.invalidate();
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        Object metaTileEntityValue = metaTileEntity == null ? null :
                metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        T metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null ? metaTileEntityValue : super.getCapability(capability, facing);
    }

    @Override
    protected void updateMTE() {
        if (metaTileEntity != null) {
            metaTileEntity.updateMTE();
        }
    }

    @Override
    protected void writeInitialSyncDataMTE(@NotNull PacketBuffer buf) {
        if (metaTileEntity != null) {
            buf.writeBoolean(true);
            buf.writeVarInt(metaTileEntity.getRegistry().getNetworkId());
            buf.writeVarInt(metaTileEntity.getRegistry().getIdByObjectName(metaTileEntity.metaTileEntityId));
            metaTileEntity.writeInitialSyncDataMTE(buf);
        } else buf.writeBoolean(false);
    }

    @Override
    public void receiveInitialSyncDataMTE(@NotNull PacketBuffer buf) {
        if (buf.readBoolean()) {
            receiveMTEInitializationData(buf);
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        setCustomName(buf.readString(Short.MAX_VALUE));
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buffer) {
        if (discriminator == INITIALIZE_MTE) {
            receiveMTEInitializationData(buffer);
        } else if (metaTileEntity != null) {
            metaTileEntity.receiveCustomData(discriminator, buffer);
        }
    }

    /**
     * Sets and initializes the MTE
     *
     * @param buf the buffer to read data from
     */
    private void receiveMTEInitializationData(@NotNull PacketBuffer buf) {
        int networkId = buf.readVarInt();
        int metaTileEntityId = buf.readVarInt();
        MTERegistry registry = GregTechAPI.mteManager.getRegistry(networkId);
        setMetaTileEntity(registry.getObjectById(metaTileEntityId));
        this.metaTileEntity.onPlacement();
        this.metaTileEntity.receiveInitialSyncDataMTE(buf);
        scheduleRenderUpdate();
    }

    @Override
    public boolean isValid() {
        return !super.isInvalid() && metaTileEntity != null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (metaTileEntity != null) {
            metaTileEntity.onLoad();
        }
    }

    @Override
    protected void onUnload() {
        if (metaTileEntity != null) {
            metaTileEntity.onUnload();
        }
    }

    @Override
    public void rotate(@NotNull Rotation rotationIn) {
        if (metaTileEntity != null) {
            metaTileEntity.setFrontFacing(rotationIn.rotate(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public void mirror(@NotNull Mirror mirrorIn) {
        if (metaTileEntity != null) {
            rotate(mirrorIn.toRotation(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (metaTileEntity == null) return false;
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity fastRender && fastRender.shouldRenderInPass(pass)) {
                return true;
            }
        }
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).shouldRenderInPass(pass);
        }
        return false;
    }

    @Override
    public ResourceLocation getMetaID() {
        if (metaTileEntity != null) {
            return metaTileEntity.getMetaID();
        }
        return GTUtility.gregtechId("unnamed");
    }

    @Override
    public int getPaintingColor() {
        if (metaTileEntity != null) {
            return metaTileEntity.getPaintingColor();
        }
        return -1;
    }

    protected void onPlacement() {
        if (metaTileEntity != null) {
            metaTileEntity.onPlacement();
        }
    }

    public NBTTagCompound writeMTETag(NBTTagCompound tagCompound) {
        if (metaTileEntity != null) {
            return metaTileEntity.writeMTETag(tagCompound);
        }
        return null;
    }

    public void readMTETag(NBTTagCompound tagCompound) {
        if (metaTileEntity != null) {
            metaTileEntity.readMTETag(tagCompound);
        }
    }

    protected MTERegistry getRegistry() {
        if (metaTileEntity != null) {
            return metaTileEntity.getRegistry();
        }
        return GregTechAPI.mteManager.getRegistry("gregtech");
    }

    protected boolean doTickProfileMessage() {
        return metaTileEntity != null && metaTileEntity.doTickProfileMessage();
    }

    protected void setFrontFacing(EnumFacing facing) {
        if (metaTileEntity != null) {
            metaTileEntity.setFrontFacing(facing);
        }
    }

    protected EnumFacing getFrontFacing() {
        return metaTileEntity == null ? EnumFacing.NORTH : metaTileEntity.getFrontFacing();
    }

    protected Cover getCoverAtSide(EnumFacing side) {
        return metaTileEntity == null ? null : metaTileEntity.getCoverAtSide(side);
    }

    @NotNull
    @Override
    @Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        return metaTileEntity == null ? AECableType.NONE : metaTileEntity.getCableConnectionType(part);
    }

    @Override
    @Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public AENetworkProxy getProxy() {
        return metaTileEntity == null ? null : metaTileEntity.getProxy();
    }

    @Override
    @Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public void gridChanged() {
        if (metaTileEntity != null) {
            metaTileEntity.gridChanged();
        }
    }
}
