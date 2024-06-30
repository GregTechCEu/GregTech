package gregtech.common.covers.ender;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.FluidTankSwitchShim;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.virtualregistry.EntryTypes;
import gregtech.api.util.virtualregistry.VirtualTankRegistry;
import gregtech.api.util.virtualregistry.entries.VirtualTank;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CoverEnderFluidLink extends CoverAbstractEnderLink<VirtualTank>
                                 implements CoverWithUI, ITickable, IControllable {

    public static final int TRANSFER_RATE = 8000; // mB/t

    protected CoverPump.PumpMode pumpMode = CoverPump.PumpMode.IMPORT;
    private final FluidTankSwitchShim linkedTank;
    protected final FluidFilterContainer fluidFilter;

    public CoverEnderFluidLink(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                               @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.linkedTank = new FluidTankSwitchShim(this.activeEntry);
        this.fluidFilter = new FluidFilterContainer(this);
    }

    @Override
    protected VirtualTank createEntry(String name, UUID owner) {
        var tank = VirtualTankRegistry.getTankCreate(name, owner);

        if (this.linkedTank != null)
            this.linkedTank.changeTank(tank);

        return tank;
    }

    @Override
    protected String identifier() {
        return "EFLink#";
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return this.fluidFilter;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.ENDER_FLUID_LINK.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(fluidFilter);
    }

    @Override
    public void update() {
        if (isWorkingEnabled() && isIoEnabled()) {
            transferFluids();
        }
    }

    protected void transferFluids() {
        IFluidHandler fluidHandler = getCoverableView().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                getAttachedSide());
        if (fluidHandler == null) return;
        if (pumpMode == CoverPump.PumpMode.IMPORT) {
            GTTransferUtils.transferFluids(fluidHandler, activeEntry, TRANSFER_RATE, fluidFilter::test);
        } else if (pumpMode == CoverPump.PumpMode.EXPORT) {
            GTTransferUtils.transferFluids(activeEntry, fluidHandler, TRANSFER_RATE, fluidFilter::test);
        }
    }

    public void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        markDirty();
    }

    public CoverPump.PumpMode getPumpMode() {
        return pumpMode;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        getFluidFilterContainer().setMaxTransferSize(1);
        return super.buildUI(guiData, guiSyncManager);
    }

    protected Column createWidgets(ModularPanel panel, PanelSyncManager syncManager) {
        var name = new StringSyncValue(activeEntry::getColorStr, this::updateColor);

        var pumpMode = new EnumSyncValue<>(CoverPump.PumpMode.class, this::getPumpMode, this::setPumpMode);
        syncManager.syncValue("pump_mode", pumpMode);
        pumpMode.updateCacheFromSource(true);

        var fluidTank = new FluidSlotSyncHandler(this.linkedTank);
        fluidTank.updateCacheFromSource(true);

        var panelSH = new PanelSyncHandler(panel) {

            @Override
            public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
                return GTGuis.createPopupPanel("entry_selector", 168, 112)
                        .child(IKey.str("Known Channels") // todo lang
                                .color(UI_TITLE_COLOR).asWidget()
                                .top(6)
                                .left(4))
                        .child(createEntryList(EntryTypes.ENDER_FLUID,
                                (name, entry) -> new Row()
                                        .left(4)
                                        .marginBottom(2)
                                        .height(18)
                                        .widthRel(0.98f)
                                        .setEnabledIf(row -> VirtualTankRegistry.hasTank(name, getOwner()))
                                        .child(new Rectangle()
                                                .setColor(entry.getColor())
                                                .asWidget()
                                                .marginRight(4)
                                                .size(16)
                                                .background(GTGuiTextures.SLOT.asIcon().size(18))
                                                .top(1))
                                        .child(IKey.str(entry.getColorStr())
                                                .alignment(Alignment.CenterLeft)
                                                .color(Color.WHITE.darker(1))
                                                .asWidget()
                                                .tooltipBuilder(tooltip -> {
                                                    String desc = entry.getDescription();
                                                    if (desc != null && !desc.isEmpty())
                                                        tooltip.addLine(desc);
                                                })
                                                .width(64)
                                                .height(16)
                                                .top(1)
                                                .marginRight(4))
                                        .child(new ButtonWidget<>()
                                                .overlay(GuiTextures.GEAR)
                                                // todo lang
                                                .tooltipBuilder(tooltip -> tooltip.addLine("Set Description"))
                                                .onMousePressed(i -> {
                                                    // open entry settings
                                                    Interactable.playButtonClickSound();
                                                    return true;
                                                }))
                                        .child(new FluidSlot()
                                                .syncHandler(new FluidSlotSyncHandler(entry)
                                                        .canDrainSlot(false)
                                                        .canFillSlot(false))
                                                .marginRight(2))
                                        .child(new ButtonWidget<>()
                                                .overlay(GTGuiTextures.BUTTON_CROSS)
                                                // todo lang
                                                .tooltipBuilder(tooltip -> tooltip.addLine("Delete Entry"))
                                                .onMousePressed(i -> {
                                                    // todo option to force delete, maybe as a popup?
                                                    VirtualTankRegistry.delTank(name, getOwner(), false);
                                                    Interactable.playButtonClickSound();
                                                    return true;
                                                }))));
            }
        };

        syncManager.syncValue("entry_selector", panelSH);

        return new Column().coverChildrenHeight().top(24)
                .margin(7, 0).widthRel(1f)
                .child(new Row().marginBottom(2)
                        .coverChildrenHeight()
                        .child(createPrivateButton())
                        .child(createColorIcon())
                        .child(new TextFieldWidget().height(18)
                                .value(name)
                                .setPattern(COLOR_INPUT_PATTERN)
                                .widthRel(0.5f)
                                .marginRight(2))
                        .child(new FluidSlot()
                                .size(18)
                                .syncHandler(fluidTank)
                                .marginRight(2))
                        .child(new ButtonWidget<>()
                                .background(GTGuiTextures.MC_BUTTON)
                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                                .onMousePressed(i -> {
                                    if (!panelSH.isPanelOpen()) {
                                        panelSH.openPanel();
                                    } else {
                                        panelSH.closePanel();
                                    }
                                    Interactable.playButtonClickSound();
                                    return true;
                                })))
                .child(createIoRow())
                .child(getFluidFilterContainer().initUI(panel, syncManager))
                .child(new EnumRowBuilder<>(CoverPump.PumpMode.class)
                        .value(pumpMode)
                        .overlay(GTGuiTextures.CONVEYOR_MODE_OVERLAY)
                        .lang("cover.pump.mode")
                        .build());
    }

    public String getColorStr() {
        return this.activeEntry.getColorStr();
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("Frequency", activeEntry.getColor());
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        tagCompound.setTag("Filter", fluidFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("PumpMode")];
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        int color = tagCompound.getInteger("Frequency");
        this.activeEntry = createEntry(identifier() + Integer.toHexString(color).toUpperCase(), this.getOwner());
        this.activeEntry.setColor(Integer.toHexString(color));
    }

    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.activeEntry);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }
}
