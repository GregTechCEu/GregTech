package gregtech.common.covers.ender;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.enderlink.FluidTankSwitchShim;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.enderlink.VirtualTankRegistry;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.filter.FluidFilterContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CoverEnderFluidLink extends CoverEnderLinkBase implements ITickable {

    public static final int TRANSFER_RATE = 8000; // mB/t
    protected CoverPump.PumpMode pumpMode;
    protected final FluidFilterContainer fluidFilter;
    protected FluidTankSwitchShim linkedShim;


    public CoverEnderFluidLink(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        pumpMode = CoverPump.PumpMode.IMPORT;
        this.linkedShim = new FluidTankSwitchShim(VirtualTankRegistry.getTankCreate(makeName(FLUID_IDENTIFIER), null));
        fluidFilter = new FluidFilterContainer(this);
    }


    public FluidFilterContainer getFluidFilterContainer() {
        return this.fluidFilter;
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
    protected void updateLink() {
        this.linkedShim.changeTank(VirtualTankRegistry.getTankCreate(makeName(FLUID_IDENTIFIER), getUUID()));
        coverHolder.markDirty();
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
        IFluidHandler fluidTankSwitchShim = linkedShim;

        if (fluidHandler == null) return;
        if (pumpMode == CoverPump.PumpMode.IMPORT) {
            GTTransferUtils.transferFluids(fluidHandler, fluidTankSwitchShim, TRANSFER_RATE, fluidFilter::testFluidStack);
        } else if (pumpMode == CoverPump.PumpMode.EXPORT) {
            GTTransferUtils.transferFluids(fluidTankSwitchShim, fluidHandler, TRANSFER_RATE, fluidFilter::testFluidStack);
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
                .setValidator(str -> COLOR_INPUT_PATTERN.matcher(str).matches()));
        widgetGroup.addWidget(new TankWidget(this.linkedShim, 123, 18, 18, 18)
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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        tagCompound.setTag("Filter", fluidFilter.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("PumpMode")];
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        updateLink();
    }

    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast((IFluidHandler) linkedShim);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }
}
