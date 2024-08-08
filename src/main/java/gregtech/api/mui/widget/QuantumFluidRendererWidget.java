package gregtech.api.mui.widget;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class QuantumFluidRendererWidget extends Widget<QuantumFluidRendererWidget> implements Interactable,
                                        JeiGhostIngredientSlot<FluidStack> {

    private final QuentumFluidSH syncHandler;
    private final TextRenderer textRenderer = new TextRenderer();

    public QuantumFluidRendererWidget(FluidTank fluidTank) {
        this.syncHandler = new QuentumFluidSH(fluidTank);
        setSyncHandler(this.syncHandler);
    }

    @Override
    public void onInit() {
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        // todo interaction maybe?
        return Result.IGNORE;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        // draw stuff
        FluidStack content = this.syncHandler.getFluidStack();
        if (content != null) {
            GuiDraw.drawFluidTexture(content, 0, 0, getArea().width, getArea().height, 0);
            // String s = NumberFormat.formatWithMaxDigits(content.amount) + " L";
            // this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width);
            // this.textRenderer.setPos(0, 5);
            // this.textRenderer.setColor(Color.WHITE.main);
            // this.textRenderer.draw(s);
        }
    }

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {}

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof FluidStack ? (FluidStack) ingredient : null;
    }

    private static class QuentumFluidSH extends SyncHandler {

        public final FluidTank fluidHandler;

        private QuentumFluidSH(FluidTank fluidHandler) {
            this.fluidHandler = fluidHandler;
        }

        @Nullable
        public FluidStack getFluidStack() {
            return fluidHandler.getFluid();
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) throws IOException {}

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {}
    }
}
