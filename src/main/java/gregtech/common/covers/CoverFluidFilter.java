package gregtech.common.covers;

import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.covers.filter.BaseFilter;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CoverFluidFilter extends CoverBase implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final FluidFilterContainer fluidFilterContainer;
    protected FluidFilterMode filterMode;
    protected FluidHandlerFiltered fluidHandler;

    public CoverFluidFilter(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                            @NotNull EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture) {
        super(definition, coverableView, attachedSide);
        this.fluidFilterContainer = new FluidFilterContainer(this);
        this.filterMode = FluidFilterMode.FILTER_FILL;
        this.titleLocale = titleLocale;
        this.texture = texture;
    }

    public void setFilterMode(FluidFilterMode filterMode) {
        this.filterMode = filterMode;
        this.getCoverableView().markDirty();
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        var dropStack = GTUtility.copy(1, itemStack);
        this.fluidFilterContainer.setFilterStack(dropStack);
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        if (TooltipHelper.isCtrlDown())
            return getCoverableView().getStackForm();

        return this.fluidFilterContainer.getFilterStack();
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        packetBuffer.writeByte(this.filterMode.ordinal());
        packetBuffer.writeBoolean(this.fluidFilterContainer.hasFilter());
        if (this.fluidFilterContainer.hasFilter()) {
            packetBuffer.writeItemStack(this.fluidFilterContainer.getFilterStack());
        }
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        this.filterMode = FluidFilterMode.VALUES[packetBuffer.readByte()];
        if (!packetBuffer.readBoolean()) return;
        try {
            this.fluidFilterContainer.setFilterStack(packetBuffer.readItemStack());
        } catch (IOException e) {
            GTLog.logger.error("Failed to read filter for CoverFluidFilter! %s", getPos().toString());
        }
    }

    public FluidFilterMode getFilterMode() {
        return filterMode;
    }

    @SuppressWarnings("DataFlowIssue") // this cover always has a filter
    public @NotNull BaseFilter getFilter() {
        return this.fluidFilterContainer.hasFilter() ?
                this.fluidFilterContainer.getFilter() :
                BaseFilter.ERROR_FILTER;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getAttachedSide()) != null;
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        var filteringMode = new EnumSyncValue<>(FluidFilterMode.class, this::getFilterMode, this::setFilterMode);

        guiSyncManager.syncValue("filtering_mode", filteringMode);
        this.fluidFilterContainer.setMaxTransferSize(1);
        getFilter().getFilterReader().readStack(this.fluidFilterContainer.getFilterStack());

        return getFilter().createPanel(guiSyncManager)
                .size(176, 194).padding(7)
                .child(CoverWithUI.createTitleRow(getPickItem()))
                .child(new Column().widthRel(1f).align(Alignment.TopLeft).top(22).coverChildrenHeight()
                        .child(new EnumRowBuilder<>(FluidFilterMode.class)
                                .value(filteringMode)
                                .lang("cover.filter.mode.title")
                                .overlay(16, GTGuiTextures.FILTER_MODE_OVERLAY)
                                .build())
                        .child(new Rectangle().setColor(UI_TEXT_COLOR).asWidget()
                                .height(1).widthRel(0.95f).margin(0, 4))
                        .child(getFilter().createWidgets(guiSyncManager)))
                .child(SlotGroupWidget.playerInventory().bottom(7).left(7));
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        this.texture.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (defaultValue instanceof IFluidHandler delegate) {
                if (fluidHandler == null || fluidHandler.delegate != delegate) {
                    this.fluidHandler = new FluidHandlerFiltered(delegate);
                }
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
            return null;
        }
        return defaultValue;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", this.filterMode.ordinal());
        tagCompound.setTag("Filter", this.fluidFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = FluidFilterMode.values()[tagCompound.getInteger("FilterMode")];
        if (tagCompound.hasKey("IsBlacklist")) {
            this.fluidFilterContainer.setFilterStack(getDefinition().getDropItemStack());
            this.fluidFilterContainer.handleLegacyNBT(tagCompound);
            this.fluidFilterContainer.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        } else {
            this.fluidFilterContainer.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        }
    }

    private class FluidHandlerFiltered extends FluidHandlerDelegate {

        public FluidHandlerFiltered(@NotNull IFluidHandler delegate) {
            super(delegate);
        }

        public int fill(FluidStack resource, boolean doFill) {
            if (getFilterMode() == FluidFilterMode.FILTER_DRAIN || !fluidFilterContainer.test(resource)) {
                return 0;
            }
            return super.fill(resource, doFill);
        }

        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (getFilterMode() == FluidFilterMode.FILTER_FILL || !fluidFilterContainer.test(resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (getFilterMode() != FluidFilterMode.FILTER_FILL) {
                FluidStack result = super.drain(maxDrain, false);
                if (result == null || result.amount <= 0 || !fluidFilterContainer.test(result)) {
                    return null;
                }
                return doDrain ? super.drain(maxDrain, true) : result;
            }
            return super.drain(maxDrain, doDrain);
        }
    }
}
