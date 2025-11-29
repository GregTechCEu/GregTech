package gregtech.api.mui.widget.appeng;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.sync.appeng.AESyncHandler;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.LongValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public abstract class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>>
                                  implements RecipeViewerIngredientProvider, Interactable {

    protected final boolean isStocking;
    protected final int index;
    protected final BooleanSupplier isAutoPull;

    private static final IDrawable normalBackground = IDrawable.of(GTGuiTextures.SLOT, GTGuiTextures.CONFIG_ARROW_DARK);
    private static final IDrawable autoPullBackground = IDrawable.of(GTGuiTextures.SLOT_DARK,
            GTGuiTextures.CONFIG_ARROW);

    @Nullable
    private IPanelHandler amountPanel;
    protected boolean selected = false;

    @Nullable
    protected Runnable onSelect;

    public AEConfigSlot(boolean isStocking, int index, @NotNull BooleanSupplier isAutoPull) {
        this.isStocking = isStocking;
        this.index = index;
        this.isAutoPull = isAutoPull;
        size(18);
        tooltipBuilder(this::buildTooltip);
    }

    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        if (isAutoPull.getAsBoolean()) {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.auto_pull_managed"));
        } else {
            if (isStocking) {
                tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set_only"));
            } else {
                tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set"));
                tooltip.addLine(IKey.lang("gregtech.gui.config_slot.scroll"));
            }
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.remove"));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull AESyncHandler<T> getSyncHandler() {
        return (AESyncHandler<T>) super.getSyncHandler();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof AESyncHandler<?>;
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);

        if (selected) {
            GTGuiTextures.SELECT_BOX.draw(0, 0, 18, 18);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton == 1) {
            getSyncHandler().clearConfig(index);
            deselect();
            return Result.SUCCESS;
        } else if (!isStocking && mouseButton == 0 && !isAmountPanelOpen() && getSyncHandler().hasConfig(index)) {
            if (onSelect != null) {
                onSelect.run();
            }

            selected = true;
            getAmountPanel().openPanel();

            return Result.SUCCESS;
        }

        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int scrollAmount) {
        if (!getSyncHandler().hasConfig(index) || isStocking) return false;

        long newStackSize = getSyncHandler().getConfigAmount(index);

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

        if (newStackSize > 0) {
            getSyncHandler().setConfigAmount(index, newStackSize);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable IDrawable getBackground() {
        return isAutoPull.getAsBoolean() ? autoPullBackground : normalBackground;
    }

    protected IPanelHandler getAmountPanel() {
        if (amountPanel == null) {
            amountPanel = IPanelHandler.simple(getPanel(),
                    (parentPanel, player) -> GTGuis.blankPopupPanel("ae_slot_amount." + index, 150, 18 + 5 * 2)
                            .closeListener(onSelect)
                            .child(createPopupDrawable()
                                    .size(18)
                                    .left(5)
                                    .top(5))
                            .child(new TextFieldWidget()
                                    .setNumbersLong(test -> test < 1 ? 1 : test)
                                    .setDefaultNumber(1)
                                    .value(new LongValue.Dynamic(() -> getSyncHandler().getConfigAmount(index),
                                            newAmount -> getSyncHandler().setConfigAmount(index, newAmount)))
                                    .size(100, 10)
                                    .left(18 + 5 * 2)
                                    // alignY didn't work :whar:
                                    .top(7)),
                    true);
        }

        return amountPanel;
    }

    public boolean isAmountPanelOpen() {
        return getAmountPanel().isPanelOpen();
    }

    public void deselect() {
        selected = false;
        if (isAmountPanelOpen()) {
            getAmountPanel().closePanel();
        }
    }

    public void onSelect(@Nullable Runnable onSelect) {
        this.onSelect = onSelect;
    }

    protected abstract @NotNull AEStackPreviewWidget<T> createPopupDrawable();
}
