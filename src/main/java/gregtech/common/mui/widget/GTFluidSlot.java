package gregtech.common.mui.widget;

import gregtech.api.GTValues;
import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GTFluidSlot extends Widget<GTFluidSlot> implements Interactable, JeiIngredientProvider {

    private final TextRenderer textRenderer = new TextRenderer();
    private GTFluidSyncHandler syncHandler;

    public GTFluidSlot() {
        tooltip().setAutoUpdate(true).setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            var fluid = this.syncHandler.getFluid();
            if (fluid == null) return;

            tooltip.addLine(fluid.getLocalizedName());
            tooltip.addLine(IKey.lang("gregtech.fluid.amount", fluid.amount, this.syncHandler.getCapacity()));

            // Add various tooltips from the material
            List<String> formula = FluidTooltipUtil.getFluidTooltip(fluid);
            if (formula != null) {
                for (String s : formula) {
                    if (s.isEmpty()) continue;
                    tooltip.addLine(s);
                }
            }

            addIngotMolFluidTooltip(fluid, tooltip);
        });
    }

    public static GTFluidSyncHandler sync(IFluidTank tank) {
        return new GTFluidSyncHandler(tank);
    }

    @Override
    public void onInit() {
        this.textRenderer.setShadow(true);
        this.textRenderer.setScale(0.5f);
        this.textRenderer.setColor(Color.WHITE.main);
    }

    public GTFluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new GTFluidSyncHandler(fluidTank));
    }

    public GTFluidSlot syncHandler(GTFluidSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return this;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        FluidStack content = this.syncHandler.getFluid();
        if (content != null) {
            GuiDraw.drawFluidTexture(content, 1, 1, getArea().w() - 2, getArea().h() - 2, 0);

            String s = NumberFormat.formatWithMaxDigits(getBaseUnitAmount(content.amount)) + getBaseUnit();
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - 1f);
            this.textRenderer.setPos(0, 12);
            this.textRenderer.draw(s);
        }
        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, getArea().w() - 2, getArea().h() - 2,
                    getWidgetTheme(context.getTheme()).getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    protected double getBaseUnitAmount(double amount) {
        return amount / 1000;
    }

    protected String getBaseUnit() {
        return "L";
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
            this.syncHandler.syncToServer(1, buffer -> buffer.writeBoolean(mouseButton == 0));
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public WidgetSlotTheme getWidgetTheme(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    @Override
    public @Nullable Object getIngredient() {
        return this.syncHandler.getFluid();
    }

    public static void addIngotMolFluidTooltip(FluidStack fluidStack, Tooltip tooltip) {
        // Add tooltip showing how many "ingot moles" (increments of 144) this fluid is if shift is held
        if (TooltipHelper.isShiftDown() && fluidStack.amount > GTValues.L) {
            int numIngots = fluidStack.amount / GTValues.L;
            int extra = fluidStack.amount % GTValues.L;
            String fluidAmount = String.format(" %,d L = %,d * %d L", fluidStack.amount, numIngots, GTValues.L);
            if (extra != 0) {
                fluidAmount += String.format(" + %d L", extra);
            }
            tooltip.addLine(TextFormatting.GRAY + LocalizationUtils.format("gregtech.gui.amount_raw") + fluidAmount);
        }
    }
}
