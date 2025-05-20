package gregtech.api.mui.widget.appeng;

import gregtech.api.mui.sync.appeng.AEItemSyncHandler;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> implements Interactable,
                              JeiGhostIngredientSlot<ItemStack> {

    public AEItemConfigSlot(boolean isStocking, BooleanSupplier isAutoPull) {
        super(isStocking, isAutoPull);
        size(18, 18);
        tooltipAutoUpdate(true);
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEItemStack stack = getSyncHandler().getConfig();
        if (stack == null) {
            super.buildTooltip(tooltip);
        } else {
            tooltip.addFromItem(stack.createItemStack());
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
        IAEItemStack config = getSyncHandler().getConfig();
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

        // replace with RenderUtil.handleJeiGhostHighlight(this); when 2812 merges (thx ghz)
        if (ModularUIJeiPlugin.hasDraggingGhostIngredient() || ModularUIJeiPlugin.hoveringOverIngredient(this)) {
            GlStateManager.colorMask(true, true, true, false);
            drawHighlight(getArea(), isHovering());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return Result.ACCEPT;
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        getSyncHandler().sendJEIDrop(ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof ItemStack stack ? stack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEItemStack config = getSyncHandler().getConfig();
        return config == null ? null : config.createItemStack();
    }
}
