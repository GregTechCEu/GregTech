package gregtech.common.covers;

import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.FilterTypeRegistry;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

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
        this.fluidFilterContainer.setFluidFilter(FilterTypeRegistry.getFluidFilterForStack(itemStack));
    }

    public FluidFilterMode getFilterMode() {
        return filterMode;
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return this.fluidFilterContainer;
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

    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup fluidFilterGroup = new WidgetGroup();
        fluidFilterGroup.addWidget(new LabelWidget(10, 5, "cover.fluid_filter.title"));
        fluidFilterGroup.addWidget(new CycleButtonWidget(10, 20, 110, 20,
                GTUtility.mapToString(FluidFilterMode.values(), (it) -> it.localeName), () -> this.filterMode.ordinal(),
                (newMode) -> this.setFilterMode(FluidFilterMode.values()[newMode])));
        this.fluidFilterContainer.initUI(45, fluidFilterGroup::addWidget);
        this.fluidFilterContainer.blacklistUI(45, fluidFilterGroup::addWidget, () -> true);
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 105 + 82)
                .widget(fluidFilterGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, GuiSyncManager guiSyncManager) {
        return GTGuis.createPanel(this, 100, 100)
                .child(getFluidFilterContainer().getFluidFilter().createWidgets(guiSyncManager))
                .bindPlayerInventory();
    }

    @Override
    public @NotNull @Unmodifiable List<@NotNull ItemStack> getDrops() {
        return Collections.singletonList(this.fluidFilterContainer.getFluidFilter().getContainerStack());
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
//        tagCompound.setBoolean("IsBlacklist", this.fluidFilter.isBlacklistFilter());
//        NBTTagCompound filterComponent = new NBTTagCompound();
//        this.fluidFilter.getFluidFilter().writeToNBT(filterComponent);
//        tagCompound.setTag("Filter", filterComponent);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = FluidFilterMode.values()[tagCompound.getInteger("FilterMode")];
        this.fluidFilterContainer.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        this.fluidFilterContainer.getFluidFilter().readFromNBT(tagCompound.getCompoundTag("Filter"));
    }

    private class FluidHandlerFiltered extends FluidHandlerDelegate {

        public FluidHandlerFiltered(@NotNull IFluidHandler delegate) {
            super(delegate);
        }

        public int fill(FluidStack resource, boolean doFill) {
            if (getFilterMode() == FluidFilterMode.FILTER_DRAIN || !fluidFilterContainer.testFluidStack(resource)) {
                return 0;
            }
            return super.fill(resource, doFill);
        }

        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (getFilterMode() == FluidFilterMode.FILTER_FILL || !fluidFilterContainer.testFluidStack(resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (getFilterMode() != FluidFilterMode.FILTER_FILL) {
                FluidStack result = super.drain(maxDrain, false);
                if (result == null || result.amount <= 0 || !fluidFilterContainer.testFluidStack(result)) {
                    return null;
                }
                return doDrain ? super.drain(maxDrain, true) : result;
            }
            return super.drain(maxDrain, doDrain);
        }
    }
}
