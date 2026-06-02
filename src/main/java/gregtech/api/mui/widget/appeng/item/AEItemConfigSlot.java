package gregtech.api.mui.widget.appeng.item;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.appeng.AEItemSyncHandler;
import gregtech.api.mui.widget.appeng.AEConfigSlot;
import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import codechicken.lib.gui.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> implements RecipeViewerGhostIngredientSlot<ItemStack> {

    public AEItemConfigSlot(boolean isStocking, int index, @NotNull BooleanSupplier isAutoPull) {
        super(isStocking, index, isAutoPull);
        tooltipAutoUpdate(true);
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEItemStack config = getSyncHandler().getConfig(index);
        if (config != null) {
            tooltip.addFromItem(config.getDefinition());
            tooltip.addLine((context, x, y, width, height, widgetTheme) -> {
                final int color = Color.GREY.darker(2);
                // TODO: do I need to access the text renderer like this?
                GuiDraw.drawRect(x, y + 3, (int) TextRenderer.SHARED.getLastActualWidth(), 2, color);
            });
        }

        super.buildTooltip(tooltip);
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
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IAEItemStack config = getSyncHandler().getConfig(index);
        if (config != null) {
            RenderUtil.drawItemStack(config.getDefinition(), 1, 1, false);
            if (!isStocking) {
                RenderUtil.renderTextFixedCorner(TextFormattingUtil.formatLongToCompactString(config.getStackSize(), 4),
                        17d, 18d, 0xFFFFFF, true, 0.5f);
            }
        }

        RenderUtil.handleJEIGhostSlotOverlay(this, widgetTheme);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (isAutoPull.getAsBoolean()) return Result.IGNORE;

        if (mouseButton == 0) {
            ItemStack heldItem = getSyncHandler().getSyncManager().getCursorItem();

            if (!heldItem.isEmpty()) {
                getSyncHandler().setConfig(index, heldItem);
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
    protected @NotNull AEStackPreviewWidget<IAEItemStack> createPopupDrawable() {
        return new AEItemStackPreviewWidget(() -> getSyncHandler().getConfig(index))
                .background(GTGuiTextures.SLOT);
    }
}
