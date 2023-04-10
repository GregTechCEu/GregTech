package gregtech.api.util.enderlink;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverBehaviorUIFactory;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import java.util.UUID;
import java.util.regex.Pattern;

public abstract class CoverEnderLinkBase<T> extends CoverBehavior implements CoverWithUI, IControllable {
    protected static final String FLUID_IDENTIFIER = "EFLink#";
    protected static final String ITEM_IDENTIFIER = "EILink#";
    protected static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("[0-9a-fA-F]*");
    protected int color;
    protected UUID playerUUID;
    protected boolean isPrivate;
    protected boolean workingEnabled = true;
    protected boolean ioEnabled;
    protected String tempColorStr;
    protected boolean isColorTemp;
    protected SwitchShimBase<T> linkedShim;

    public CoverEnderLinkBase(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        ioEnabled = false;
        isPrivate = false;
        playerUUID = null;
        color = 0xFFFFFFFF;
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onAttached(ItemStack itemStack, EntityPlayer player) {
        super.onAttached(itemStack, player);
        if (player != null) {
            this.playerUUID = player.getUniqueID();
        }
    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void openUI(EntityPlayerMP player) {
        CoverBehaviorUIFactory.INSTANCE.openUI(this, player);
        isColorTemp = false;
    }

    protected String makeName(String identifier) {
        return identifier + Integer.toHexString(this.color).toUpperCase();
    }
    protected UUID getUUID() {
        return isPrivate ? playerUUID : null;
    }

    protected void updateColor(String str) {
        if (str.length() == 8) {
            isColorTemp = false;
            // stupid java not having actual unsigned ints
            long tmp = Long.parseLong(str, 16);
            if (tmp > 0x7FFFFFFF) {
                tmp -= 0x100000000L;
            }
            this.color = (int) tmp;
            updateLink();
        } else {
            tempColorStr = str;
            isColorTemp = true;
        }
    }

    public String getColorStr() {
        return isColorTemp ? tempColorStr : Integer.toHexString(this.color).toUpperCase();
    }

    protected void updateLink() {
        /*
        if (linkedShim instanceof IFluidTank) {
            this.linkedShim.changeInventory((T) VirtualTankRegistry.getTankCreate(makeName(FLUID_IDENTIFIER), getUUID()));
        } else {
            this.linkedShim.changeInventory((T) VirtualContainerRegistry.getContainerCreate(makeName(ITEM_IDENTIFIER), getUUID()));
        }
        */
        coverHolder.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("Frequency", color);
        tagCompound.setBoolean("WorkingAllowed", workingEnabled);
        tagCompound.setBoolean("IOAllowed", ioEnabled);
        tagCompound.setBoolean("Private", isPrivate);
        tagCompound.setString("PlacedUUID", playerUUID.toString());
        // tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        // tagCompound.setTag("Filter", fluidFilter.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.color = tagCompound.getInteger("Frequency");
        this.workingEnabled = tagCompound.getBoolean("WorkingAllowed");
        this.ioEnabled = tagCompound.getBoolean("IOAllowed");
        this.isPrivate = tagCompound.getBoolean("Private");
        this.playerUUID = UUID.fromString(tagCompound.getString("PlacedUUID"));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(this.color);
        packetBuffer.writeString(this.playerUUID == null ? "null" : this.playerUUID.toString());
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        this.color = packetBuffer.readInt();
        //does client even need uuid info? just in case
        String uuidStr = packetBuffer.readString(36);
        this.playerUUID = uuidStr.equals("null") ? null : UUID.fromString(uuidStr);
        //client does not need the actual tank reference, the default one will do just fine
    }
    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workingEnabled = isActivationAllowed;
    }


    public boolean isIoEnabled() {
        return ioEnabled;
    }

    public void setIoEnabled(boolean ioEnabled) {
        this.ioEnabled = ioEnabled;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        updateLink();
    }
}
