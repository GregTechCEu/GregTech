package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.render.SimpleOverlayRenderer;
import gregtech.api.util.GTUtility;
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
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class CoverFluidFilter extends CoverBehavior implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final FluidFilterWrapper fluidFilter;
    protected FluidFilterMode filterMode;
    protected FluidHandlerFiltered fluidHandler;

    public CoverFluidFilter(ICoverable coverHolder, EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture, FluidFilter fluidFilter) {
        super(coverHolder, attachedSide);
        this.filterMode = FluidFilterMode.FILTER_FILL;
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.fluidFilter = new FluidFilterWrapper(this);
        this.fluidFilter.setFluidFilter(fluidFilter);
    }

    protected void setFilterMode(FluidFilterMode filterMode) {
        this.filterMode = filterMode;
        this.coverHolder.markDirty();
    }

    public FluidFilterMode getFilterMode() {
        return filterMode;
    }

    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
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
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 105 + 82)
            .widget(fluidFilterGroup)
            .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
            .build(this, player);
    }

    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        this.texture.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler delegate = (IFluidHandler) defaultValue;
            if (fluidHandler == null || fluidHandler.delegate != delegate) {
                this.fluidHandler = new FluidHandlerFiltered(delegate);
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        }
        return defaultValue;
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", this.filterMode.ordinal());
        tagCompound.setBoolean("IsBlacklist", this.fluidFilter.isBlacklistFilter());
        NBTTagCompound filterComponent = new NBTTagCompound();
        this.fluidFilter.getFluidFilter().writeToNBT(filterComponent);
        tagCompound.setTag("Filter", filterComponent);
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = FluidFilterMode.values()[tagCompound.getInteger("FilterMode")];
        this.fluidFilter.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        //LEGACY SAVE FORMAT SUPPORT
        if(tagCompound.hasKey("FluidFilter")) {
            this.fluidFilter.getFluidFilter().readFromNBT(tagCompound);
        } else {
            NBTTagCompound filterComponent = tagCompound.getCompoundTag("Filter");
            this.fluidFilter.getFluidFilter().readFromNBT(filterComponent);
        }
    }

    private class FluidHandlerFiltered extends FluidHandlerDelegate {

        public FluidHandlerFiltered(IFluidHandler delegate) {
            super(delegate);
        }

        public int fill(FluidStack resource, boolean doFill) {
            FluidFilterMode filterMode = getFilterMode();
            if (filterMode == FluidFilterMode.FILTER_DRAIN) {
                return 0;
            }
            if (!fluidFilter.testFluidStack(resource)) {
                return 0;
            }
            return super.fill(resource, doFill);
        }

        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidFilterMode filterMode = getFilterMode();
            if (filterMode == FluidFilterMode.FILTER_FILL) {
                return null;
            }
            if (!fluidFilter.testFluidStack(resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidFilterMode filterMode = getFilterMode();
            if (filterMode == FluidFilterMode.FILTER_FILL) {
                return null;
            }
            FluidStack fluidTank, fluidDrain, result;
            for (IFluidTankProperties prop : this.delegate.getTankProperties()) {
                fluidTank = prop.getContents();
                if (fluidTank != null && fluidFilter.testFluidStack(fluidTank)) {
                    int drainAmount = Math.min(fluidTank.amount, maxDrain);
                    fluidDrain = new FluidStack(fluidTank.getFluid(), drainAmount, fluidTank.tag);
                    result = super.drain(fluidDrain, doDrain);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }

    }
}
