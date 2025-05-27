package gregtech.api.mui.widget.appeng.item;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.appeng.AEItemSyncHandler;
import gregtech.api.mui.widget.appeng.AEConfigSlot;
import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> implements JeiGhostIngredientSlot<ItemStack> {

    public AEItemConfigSlot(boolean isStocking, int index, BooleanSupplier isAutoPull) {
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
        IAEItemStack config = getSyncHandler().getConfig(index);
        if (config == null) {
            super.buildTooltip(tooltip);
        } else {
            tooltip.addFromItem(config.createItemStack());
        }
    }

    @Override
    public @NotNull AEItemSyncHandler getSyncHandler() {
        return (AEItemSyncHandler) super.getSyncHandler();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof AEItemSyncHandler;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        IAEItemStack config = getSyncHandler().getConfig(index);
        if (config != null) {
            ItemStack stack = config.createItemStack();
            if (!stack.isEmpty()) {
                stack.setCount(1);
                RenderUtil.renderItem(stack, 1, 1, 16f, 16f);
            }

            if (!isStocking) {
                String amount = TextFormattingUtil.formatLongToCompactString(config.getStackSize(), 4);
                RenderUtil.renderTextFixedCorner(amount, 17d, 18d, 0xFFFFFF, true, 0.5f);
            }
        }
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetTheme widgetTheme) {
        // TODO: replace with RenderUtil.handleJeiGhostHighlight(this); when 2812 merges (thx ghz)
        if (ModularUIJeiPlugin.hoveringOverIngredient(this)) {
            GlStateManager.colorMask(true, true, true, false);
            drawHighlight(getArea(), isHovering());
            GlStateManager.colorMask(true, true, true, true);
        } else if (isHovering()) {
            drawSlotOverlay();
        }

        super.drawOverlay(context, widgetTheme);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (isAutoPull.getAsBoolean()) return Result.IGNORE;

        if (mouseButton == 0) {
            // Left click to set item/change amount
            ItemStack heldItem = getSyncHandler().getSyncManager().getCursorItem();

            if (!heldItem.isEmpty()) {
                getSyncHandler().setConfig(index, WrappedItemStack.fromItemStack(heldItem));
                return Result.SUCCESS;
            }
        }

        return super.onMousePressed(mouseButton);
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        getSyncHandler().setConfig(index, ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return !isAutoPull.getAsBoolean() && ingredient instanceof ItemStack stack ? stack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEItemStack config = getSyncHandler().getConfig(index);
        return config == null ? null : config.createItemStack();
    }

    @Override
    protected @NotNull AEStackPreviewWidget createPopupDrawable() {
        return new AEStackPreviewWidget((x, y, width, height) -> {
            IAEItemStack stack = getSyncHandler().getConfig(index);
            if (stack != null) {
                GuiDraw.drawItem(stack.getDefinition(), x, y, width, height);
            }
        })
                .background(GTGuiTextures.SLOT);
    }
}
