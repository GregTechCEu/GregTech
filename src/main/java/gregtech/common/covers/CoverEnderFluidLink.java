package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverBehaviorUIFactory;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.FluidTankSwitchShim;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.VirtualTankRegistry;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.FluidFilterContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.UUID;

public class CoverEnderFluidLink extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    public static final int TRANSFER_RATE = 8000; // mB/t

    protected CoverPump.PumpMode pumpMode;
    private int color;
    private UUID playerUUID;
    private boolean isPrivate;
    private boolean workingEnabled = true;
    private boolean ioEnabled;
    private String tempColorStr;
    private boolean isColorTemp;
    private final FluidTankSwitchShim linkedTank;
    protected final FluidFilterContainer fluidFilter;

    public CoverEnderFluidLink(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        pumpMode = CoverPump.PumpMode.IMPORT;
        ioEnabled = false;
        isPrivate = false;
        playerUUID = null;
        color = 0xFFFFFFFF;
        this.linkedTank = new FluidTankSwitchShim(VirtualTankRegistry.getTankCreate(makeTankName(), null));
        fluidFilter = new FluidFilterContainer(this);
    }

    private String makeTankName() {
        return "EFLink#" + Integer.toHexString(this.color).toUpperCase();
    }

    private UUID getTankUUID() {
        return isPrivate ? playerUUID : null;
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return this.fluidFilter;
    }

    public boolean isIOEnabled() {
        return this.ioEnabled;
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ENDER_FLUID_LINK.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
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
        NonNullList<ItemStack> drops = NonNullList.create();
        MetaTileEntity.clearInventory(drops, fluidFilter.getFilterInventory());
        for (ItemStack itemStack : drops) {
            Block.spawnAsEntity(coverHolder.getWorld(), coverHolder.getPos(), itemStack);
        }
    }

    @Override
    public void update() {
        if (workingEnabled && ioEnabled) {
            transferFluids();
        }
    }

    protected void transferFluids() {
        IFluidHandler fluidHandler = coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
        if (fluidHandler == null) return;
        if (pumpMode == CoverPump.PumpMode.IMPORT) {
            GTTransferUtils.transferFluids(fluidHandler, linkedTank, TRANSFER_RATE, fluidFilter::testFluidStack);
        } else if (pumpMode == CoverPump.PumpMode.EXPORT) {
            GTTransferUtils.transferFluids(linkedTank, fluidHandler, TRANSFER_RATE, fluidFilter::testFluidStack);
        }
    }

    public void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        coverHolder.markDirty();
    }

    public CoverPump.PumpMode getPumpMode() {
        return pumpMode;
    }

    @Override
    public void openUI(EntityPlayerMP player) {
        CoverBehaviorUIFactory.INSTANCE.openUI(this, player);
        isColorTemp = false;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new LabelWidget(10, 5, "cover.ender_fluid_link.title"));
        widgetGroup.addWidget(new ToggleButtonWidget(12, 18, 18, 18, GuiTextures.BUTTON_PUBLIC_PRIVATE,
                this::isPrivate, this::setPrivate)
                .setTooltipText("cover.ender_fluid_link.private.tooltip"));
        widgetGroup.addWidget(new SyncableColorRectWidget(35, 18, 18, 18, () -> color)
                .setBorderWidth(1)
                .drawCheckerboard(4, 4));
        widgetGroup.addWidget(new TextFieldWidget(58, 13, 58, 18, true,
                this::getColorStr, this::updateColor, 8)
                .setValidator(str -> str.matches("[0-9a-fA-F]*")));
        widgetGroup.addWidget(new TankWidget(this.linkedTank, 123, 18, 18, 18)
                .setContainerClicking(true, true)
                .setBackgroundTexture(GuiTextures.FLUID_SLOT).setAlwaysShowFull(true));
        widgetGroup.addWidget(new ImageWidget(147, 19, 16, 16)
                .setImage(GuiTextures.INFO_ICON)
                .setPredicate(() -> isColorTemp)
                .setTooltip("cover.ender_fluid_link.incomplete_hex")
                .setIgnoreColor(true));
        widgetGroup.addWidget(new CycleButtonWidget(10, 42, 75, 18,
                CoverPump.PumpMode.class, this::getPumpMode, this::setPumpMode));
        widgetGroup.addWidget(new CycleButtonWidget(92, 42, 75, 18,
                this::isIoEnabled, this::setIoEnabled, "cover.ender_fluid_link.iomode.disabled", "cover.ender_fluid_link.iomode.enabled"));
        this.fluidFilter.initUI(65, widgetGroup::addWidget);
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 221)
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, 139)
                .build(this, player);
    }

    public void updateColor(String str) {
        if (str.length() == 8) {
            isColorTemp = false;
            // stupid java not having actual unsigned ints
            long tmp = Long.parseLong(str, 16);
            if (tmp > 0x7FFFFFFF) {
                tmp -= 0x100000000L;
            }
            this.color = (int) tmp;
            updateTankLink();
        } else {
            tempColorStr = str;
            isColorTemp = true;
        }
    }

    public String getColorStr() {
        return isColorTemp ? tempColorStr : Integer.toHexString(this.color).toUpperCase();
    }

    public void updateTankLink() {
        this.linkedTank.changeTank(VirtualTankRegistry.getTankCreate(makeTankName(), getTankUUID()));
        coverHolder.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("Frequency", color);
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", workingEnabled);
        tagCompound.setBoolean("IOAllowed", ioEnabled);
        tagCompound.setBoolean("Private", isPrivate);
        tagCompound.setString("PlacedUUID", playerUUID.toString());
        tagCompound.setTag("Filter", fluidFilter.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.color = tagCompound.getInteger("Frequency");
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("PumpMode")];
        this.workingEnabled = tagCompound.getBoolean("WorkingAllowed");
        this.ioEnabled = tagCompound.getBoolean("IOAllowed");
        this.isPrivate = tagCompound.getBoolean("Private");
        this.playerUUID = UUID.fromString(tagCompound.getString("PlacedUUID"));
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        updateTankLink();
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

    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(linkedTank);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }

    private boolean isIoEnabled() {
        return ioEnabled;
    }

    private void setIoEnabled(boolean ioEnabled) {
        this.ioEnabled = ioEnabled;
    }

    private boolean isPrivate() {
        return isPrivate;
    }

    private void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        updateTankLink();
    }
}
