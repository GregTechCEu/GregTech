package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.SlotGroup;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.Filter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class CoverFluidFilter extends CoverBehavior implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected Filter<FluidStack> fluidFilter;
    protected FluidFilterMode filterMode;
    protected FluidHandlerFiltered fluidHandler;

    public CoverFluidFilter(ICoverable coverHolder, EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture, Filter<FluidStack> fluidFilter) {
        super(coverHolder, attachedSide);
        this.filterMode = FluidFilterMode.FILTER_FILL;
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.fluidFilter = fluidFilter;
    }

    public void setFilterMode(FluidFilterMode filterMode) {
        this.filterMode = filterMode;
        this.coverHolder.markDirty();
    }

    public FluidFilterMode getFilterMode() {
        return filterMode;
    }

    public FluidFilterWrapper getFluidFilter() {
        return this.fluidFilter;
    }

    public boolean testFluidStack(FluidStack stack) {
        return fluidFilter.matches(stack);
    }

    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            GregTechUI.getCoverUi(attachedSide).open(playerIn, coverHolder.getWorld(), coverHolder.getPos());
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        Widget filterUI = fluidFilter.createFilterUI(buildContext.getPlayer());
        int x = filterUI.getSize().width > 0 ? 88 - filterUI.getSize().width / 2 : 7;
        int height = filterUI.getSize().height > 0 ? 46 + SlotGroup.PLAYER_INVENTORY_HEIGHT + filterUI.getSize().height : 166;
        return ModularWindow.builder(176, height)
                .setBackground(GuiTextures.VANILLA_BACKGROUND)
                .bindPlayerInventory(buildContext.getPlayer())
                .widget(new TextWidget(new Text(titleLocale).localise())
                        .setPos(6, 6))
                .widget(new TextWidget(Text.localised("cover.filter_mode.label"))
                        .setTextAlignment(Alignment.CenterLeft)
                        .setSize(80, 14)
                        .setPos(7, 18))
                .widget(new CycleButtonWidget()
                        .setForEnum(FluidFilterMode.class, this::getFilterMode, this::setFilterMode)
                        .setTextureGetter(GuiFunctions.enumStringTextureGetter(FluidFilterMode.class))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setPos(87, 18)
                        .setSize(80, 14))
                .widget(filterUI.setPos(x, 34))
                .build();
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
        NBTTagCompound filterComponent = new NBTTagCompound();
        this.fluidFilter.writeToNBT(filterComponent);
        tagCompound.setTag("Filter", filterComponent);
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = FluidFilterMode.values()[tagCompound.getInteger("FilterMode")];
        this.fluidFilter.readFromNBT(tagCompound.getCompoundTag("Filter"));
        if (tagCompound.hasKey("IsBlacklist")) {
            this.fluidFilter.setInverted(tagCompound.getBoolean("IsBlacklist"));
        }
    }

    private class FluidHandlerFiltered extends FluidHandlerDelegate {

        public FluidHandlerFiltered(IFluidHandler delegate) {
            super(delegate);
        }

        public int fill(FluidStack resource, boolean doFill) {
            if (getFilterMode() == FluidFilterMode.FILTER_DRAIN || !fluidFilter.matches(resource)) {
                return 0;
            }
            return super.fill(resource, doFill);
        }

        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (getFilterMode() == FluidFilterMode.FILTER_FILL || !fluidFilter.matches(resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (getFilterMode() != FluidFilterMode.FILTER_FILL) {
                FluidStack result = super.drain(maxDrain, false);
                if (result == null || result.amount <= 0 || !fluidFilter.matches(result)) {
                    return null;
                }
                return doDrain ? super.drain(maxDrain, true) : result;
            }
            return super.drain(maxDrain, doDrain);
        }
    }
}
