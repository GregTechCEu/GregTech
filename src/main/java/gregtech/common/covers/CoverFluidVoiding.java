package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CoverFluidVoiding extends CoverPump {

    protected final NullFluidTank nullFluidTank = new NullFluidTank();

    public CoverFluidVoiding(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide, 0, Integer.MAX_VALUE);
        this.isWorkingAllowed = false;
    }

    @Override
    public void update() {
        long timer = coverHolder.getOffsetTimer();
        if (isWorkingAllowed && timer % 20 == 0) {
            doTransferFluids();
        }
    }

    protected void doTransferFluids() {
        IFluidHandler myFluidHandler = coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
        if (myFluidHandler == null) {
            return;
        }
        GTTransferUtils.transferFluids(myFluidHandler, nullFluidTank, Integer.MAX_VALUE, filterHolder::test);
    }

    @Override
    protected String getUITitle() {
        return "cover.fluid.voiding.title";
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(176, 166)
                .bindPlayerInventory(buildContext.getPlayer())
                .setBackground(GuiTextures.BACKGROUND)
                .widget(new TextWidget(Text.localised(getUITitle()))
                        .setPos(10, 5))
                .widget(filterHolder.createFilterUI(buildContext, this::checkControlsAmount)
                        .setPos(7, 42))
                .build();
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.FLUID_VOIDING.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(nullFluidTank);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }

    class NullFluidTank extends FluidTank {

        public NullFluidTank() {
            super(Integer.MAX_VALUE);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (filterHolder.test(resource)) {
                return resource.amount;
            }
            return 0;
        }
    }
}
