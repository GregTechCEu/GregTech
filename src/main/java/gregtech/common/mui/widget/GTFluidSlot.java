package gregtech.common.mui.widget;

import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.client.utils.RenderUtil;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GTFluidSlot extends Widget<GTFluidSlot> implements Interactable, JeiIngredientProvider,
                               JeiGhostIngredientSlot<FluidStack> {

    private final TextRenderer textRenderer = new TextRenderer();
    private GTFluidSyncHandler syncHandler;
    private boolean disableBackground = false;

    public GTFluidSlot() {
        tooltip().titleMargin();
    }

    public static GTFluidSyncHandler sync(IFluidTank tank) {
        return new GTFluidSyncHandler(tank);
    }

    @Override
    public void onInit() {
        this.textRenderer.setShadow(true);
        this.textRenderer.setScale(0.5f);
        this.textRenderer.setColor(Color.WHITE.main);
        if (syncHandler.canLockFluid() || syncHandler.isPhantom()) {
            getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
        }
        tooltipBuilder(syncHandler::handleTooltip);
        syncHandler.setChangeConsumer($ -> markTooltipDirty());
    }

    public GTFluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(sync(fluidTank));
    }

    public GTFluidSlot syncHandler(GTFluidSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return this;
    }

    public GTFluidSlot disableBackground() {
        this.disableBackground = true;
        return this;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof GTFluidSyncHandler;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (disableBackground) return;
        super.drawBackground(context, widgetTheme);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (widgetTheme instanceof WidgetSlotTheme slotTheme) {
            draw(context, slotTheme);
        }
    }

    public void draw(ModularGuiContext context, WidgetSlotTheme widgetTheme) {
        FluidStack content = this.syncHandler.getFluid();
        if (content == null)
            content = this.syncHandler.getLockedFluid();

        float height = getArea().h() - 2;
        int y = 1;

        if (!this.syncHandler.drawAlwaysFull()) {
            float amt = content == null ? 0f : content.amount;
            float newHeight = height * (amt / this.syncHandler.getCapacity());
            y += (int) (height - newHeight);
            height = newHeight;
        }

        GuiDraw.drawFluidTexture(content, 1, y, getArea().w() - 2, height, 0);

        if (content != null && this.syncHandler.showAmountOnSlot()) {
            String amount = NumberFormat.formatWithMaxDigits(content.amount, 3) + "L";
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - 1f);
            this.textRenderer.setPos(0, 12);
            this.textRenderer.draw(amount);
        }

        RenderUtil.handleJEIGhostSlotOverlay(this, widgetTheme);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        var data = MouseData.create(mouseButton);
        if (this.syncHandler.isPhantom() ||
                this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
            this.syncHandler.handleClick(data);

            if (this.syncHandler.canLockFluid())
                this.syncHandler.toggleLockFluid();

            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (!this.syncHandler.isPhantom()) return false;
        if ((scrollDirection.isUp() && !this.syncHandler.canFillSlot()) ||
                (scrollDirection.isDown() && !this.syncHandler.canDrainSlot())) {
            return false;
        }
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        this.syncHandler.handlePhantomScroll(mouseData);
        return true;
    }

    @Override
    protected WidgetTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        if (this.syncHandler.isPhantom()) {
            this.syncHandler.setFluid(ingredient);
            this.syncHandler.syncToServer(GTFluidSyncHandler.UPDATE_TANK,
                    buffer -> NetworkUtils.writeFluidStack(buffer, ingredient));
        } else {
            this.syncHandler.lockFluid(ingredient);
        }
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        if (!(syncHandler.canLockFluid() || syncHandler.isPhantom())) return null;

        if (ingredient instanceof FluidStack stack) {
            return stack;
        }

        if (ingredient instanceof ItemStack stack) {
            return FluidUtil.getFluidContained(stack);
        }

        return null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return this.syncHandler.getFluid();
    }
}
