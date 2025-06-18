package gregtech.api.mui.widget.appeng.fluid;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.widget.appeng.AEConfigSlot;
import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class AEFluidConfigSlot extends AEConfigSlot<IAEFluidStack>
                               implements Interactable, JeiGhostIngredientSlot<FluidStack> {

    public AEFluidConfigSlot(boolean isStocking, int index, BooleanSupplier isAutoPull) {
        super(isStocking, index, isAutoPull);
        tooltipAutoUpdate(true);
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEFluidStack config = getSyncHandler().getConfig(index);
        if (config == null) {
            super.buildTooltip(tooltip);
        } else {
            KeyUtil.fluidInfo(((WrappedFluidStack) config).getDelegate(), tooltip, false, true, true);
        }
    }

    @Override
    public @NotNull AEFluidSyncHandler getSyncHandler() {
        return (AEFluidSyncHandler) super.getSyncHandler();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof AEFluidSyncHandler;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        IAEFluidStack config = getSyncHandler().getConfig(index);
        if (config != null) {
            FluidStack stack = ((WrappedFluidStack) config).getDelegate();
            RenderUtil.drawFluidForGui(stack, stack.amount, 1, 1, 17, 17);

            if (!isStocking) {
                String amount = TextFormattingUtil.formatLongToCompactString(stack.amount, 4);
                RenderUtil.renderTextFixedCorner(amount, 17d, 18d, 0xFFFFFF, true, 0.5f);
            }
        }
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (!RenderUtil.handleJeiGhostHighlight(this)) {
            drawSlotOverlay();
        }

        super.drawOverlay(context, widgetTheme);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (isAutoPull.getAsBoolean()) return Result.IGNORE;

        if (mouseButton == 0) {
            ItemStack heldItem = getSyncHandler().getSyncManager().getCursorItem();
            FluidStack heldFluid = FluidUtil.getFluidContained(heldItem);

            if (heldFluid != null) {
                getSyncHandler().setConfig(index, heldFluid);
                return Result.SUCCESS;
            }
        }

        return super.onMousePressed(mouseButton);
    }

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        getSyncHandler().setConfig(index, ingredient);
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return !isAutoPull.getAsBoolean() && ingredient instanceof FluidStack stack ? stack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack config = getSyncHandler().getConfig(index);
        return config == null ? null : config.getFluidStack();
    }

    @Override
    protected @NotNull AEStackPreviewWidget<IAEFluidStack> createPopupDrawable() {
        return new AEFluidStackPreviewWidget(() -> getSyncHandler().getConfig(index))
                .background(GTGuiTextures.FLUID_SLOT);
    }
}
