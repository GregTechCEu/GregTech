package gregtech.common.mui.widget;

import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.client.utils.RenderUtil;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GTFluidSlot extends Widget<GTFluidSlot> implements Interactable, RecipeViewerIngredientProvider,
                               RecipeViewerGhostIngredientSlot<FluidStack> {

    private final TextRenderer textRenderer = new TextRenderer();
    private GTFluidSyncHandler syncHandler;

    public GTFluidSlot() {
        tooltip().titleMargin();
        tooltipAutoUpdate(true);
        tooltipBuilder(tooltip -> {
            if (isSynced()) {
                syncHandler.handleTooltip(tooltip);
            }
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
        if (syncHandler.canLockFluid() || syncHandler.isPhantom()) {
            getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
        }
    }

    public GTFluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(sync(fluidTank));
    }

    public GTFluidSlot syncHandler(GTFluidSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        return this;
    }

    public GTFluidSlot disableBackground() {
        return background(IDrawable.NONE);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof GTFluidSyncHandler;
    }

    @Override
    protected void setSyncHandler(@Nullable SyncHandler syncHandler) {
        super.setSyncHandler(syncHandler);
        this.syncHandler = (GTFluidSyncHandler) syncHandler;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        FluidStack content = this.syncHandler.getFluid();
        if (content == null)
            content = this.syncHandler.getLockedFluid();

        float height = getArea().h() - 2;
        int y = 1;

        if (!this.syncHandler.drawAlwaysFull()) {
            float amt = content == null ? 0f : content.amount;
            float newHeight = height * (amt / this.syncHandler.getCapacity());
            y += (int) (height - newHeight);
            height = (float) Math.ceil(newHeight);
        }

        GuiDraw.drawFluidTexture(content, 1, y, getArea().w() - 2, height, 0);

        if (content != null && this.syncHandler.showAmountOnSlot()) {
            String amount = NumberFormat.DEFAULT.format(content.amount) + "L";
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

            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
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
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
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
