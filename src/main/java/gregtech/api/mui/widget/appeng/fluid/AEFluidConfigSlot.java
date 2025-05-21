package gregtech.api.mui.widget.appeng.fluid;

import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.widget.appeng.AEConfigSlot;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class AEFluidConfigSlot extends AEConfigSlot<IAEFluidStack>
                               implements Interactable, JeiGhostIngredientSlot<FluidStack> {

    public AEFluidConfigSlot(boolean isStocking, BooleanSupplier isAutoPull) {
        super(isStocking, isAutoPull);
        tooltipAutoUpdate(true);
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEFluidStack config = getSyncHandler().getConfig();
        if (config == null) {
            super.buildTooltip(tooltip);
        } else {
            FluidStack stack = config.getFluidStack();
            tooltip.addLine(IKey.str(stack.getLocalizedName()));
            tooltip.addLine(IKey.str("%,d L", stack.amount));

            for (String fluidTooltip : FluidTooltipUtil.getFluidTooltip(stack)) {
                if (fluidTooltip.isEmpty()) continue;
                tooltip.addLine(IKey.str(fluidTooltip));
            }

            GTFluidSlot.addIngotMolFluidTooltip(stack, tooltip);
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
        IAEFluidStack config = getSyncHandler().getConfig();
        if (config != null) {
            FluidStack stack = config.getFluidStack();
            RenderUtil.drawFluidForGui(stack, stack.amount, 1, 1, 17, 17);

            if (!isStocking) {
                String amount = TextFormattingUtil.formatLongToCompactString(stack.amount, 4);
                RenderUtil.renderTextFixedCorner(amount, 17d, 18d, 0xFFFFFF, true, 0.5f);
            }
        }

        // TODO: replace with RenderUtil.handleJeiGhostHighlight(this); when 2812 merges (thx ghz)
        if (ModularUIJeiPlugin.hasDraggingGhostIngredient() || ModularUIJeiPlugin.hoveringOverIngredient(this)) {
            GlStateManager.colorMask(true, true, true, false);
            drawHighlight(getArea(), isHovering());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (isAutoPull.getAsBoolean()) return Result.IGNORE;

        if (mouseButton == 1) {
            // Right click to clear
            getSyncHandler().clearConfig();
            return Result.SUCCESS;
        } else if (mouseButton == 0) {
            ItemStack heldItem = getSyncHandler().getSyncManager().getCursorItem();
            FluidStack heldFluid = FluidUtil.getFluidContained(heldItem);

            if (heldFluid != null) {
                getSyncHandler().setConfig(heldFluid);
            }

            return Result.SUCCESS;
        }

        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int scrollAmount) {
        if (getSyncHandler().getConfig() == null || isStocking) return false;

        long newStackSize = getSyncHandler().getConfigAmount();

        if (Interactable.hasControlDown()) {
            switch (scrollDirection) {
                case UP -> newStackSize *= 2;
                case DOWN -> newStackSize /= 2;
            }
        } else {
            switch (scrollDirection) {
                case UP -> newStackSize += 1;
                case DOWN -> newStackSize -= 1;
            }
        }

        if (newStackSize > 0 && newStackSize < Integer.MAX_VALUE + 1L) {
            int scaledStackSize = (int) newStackSize;
            getSyncHandler().setConfigAmount(scaledStackSize);
            return true;
        }

        return false;
    }

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        getSyncHandler().setConfig(ingredient);
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return !isAutoPull.getAsBoolean() && ingredient instanceof FluidStack stack ? stack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack config = getSyncHandler().getConfig();
        return config == null ? null : config.getFluidStack();
    }
}
