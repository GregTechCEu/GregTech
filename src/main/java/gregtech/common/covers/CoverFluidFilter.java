package gregtech.common.covers;

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
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.FluidFilter;
import gregtech.common.covers.filter.FluidFilterWrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

public class CoverFluidFilter extends CoverBase implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final FluidFilterWrapper fluidFilter;
    protected FluidFilterMode filterMode;
    protected FluidHandlerFiltered fluidHandler;

    public CoverFluidFilter(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                            @NotNull EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture,
                            FluidFilter fluidFilter) {
        super(definition, coverableView, attachedSide);
        this.filterMode = FluidFilterMode.FILTER_FILL;
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.fluidFilter = new FluidFilterWrapper(this);
        this.fluidFilter.setFluidFilter(fluidFilter);
    }

    public void setFilterMode(FluidFilterMode filterMode) {
        this.filterMode = filterMode;
        this.getCoverableView().markDirty();
    }

    public FluidFilterMode getFilterMode() {
        return filterMode;
    }

    public FluidFilterWrapper getFluidFilter() {
        return this.fluidFilter;
    }

    public boolean testFluidStack(FluidStack stack) {
        return fluidFilter.testFluidStack(stack);
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
        this.fluidFilter.initUI(45, fluidFilterGroup::addWidget);
        this.fluidFilter.blacklistUI(45, fluidFilterGroup::addWidget, () -> true);
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 105 + 82)
                .widget(fluidFilterGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        this.texture.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler delegate = (IFluidHandler) defaultValue;
            if (fluidHandler == null || fluidHandler.delegate != delegate) {
                this.fluidHandler = new FluidHandlerFiltered(delegate);
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        }
        return defaultValue;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", this.filterMode.ordinal());
        tagCompound.setBoolean("IsBlacklist", this.fluidFilter.isBlacklistFilter());
        NBTTagCompound filterComponent = new NBTTagCompound();
        this.fluidFilter.getFluidFilter().writeToNBT(filterComponent);
        tagCompound.setTag("Filter", filterComponent);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = FluidFilterMode.values()[tagCompound.getInteger("FilterMode")];
        this.fluidFilter.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        this.fluidFilter.getFluidFilter().readFromNBT(tagCompound.getCompoundTag("Filter"));
    }

    private class FluidHandlerFiltered extends FluidHandlerDelegate {

        public FluidHandlerFiltered(IFluidHandler delegate) {
            super(delegate);
        }

        public int fill(FluidStack resource, boolean doFill) {
            if (getFilterMode() == FluidFilterMode.FILTER_DRAIN || !fluidFilter.testFluidStack(resource)) {
                return 0;
            }
            return super.fill(resource, doFill);
        }

        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (getFilterMode() == FluidFilterMode.FILTER_FILL || !fluidFilter.testFluidStack(resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (getFilterMode() != FluidFilterMode.FILTER_FILL) {
                FluidStack result = super.drain(maxDrain, false);
                if (result == null || result.amount <= 0 || !fluidFilter.testFluidStack(result)) {
                    return null;
                }
                return doDrain ? super.drain(maxDrain, true) : result;
            }
            return super.drain(maxDrain, doDrain);
        }
    }
}
