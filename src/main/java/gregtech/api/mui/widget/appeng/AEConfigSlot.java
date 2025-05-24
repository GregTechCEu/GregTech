package gregtech.api.mui.widget.appeng;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.appeng.AESyncHandler;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public abstract class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>>
                                  implements JeiIngredientProvider, Interactable {

    protected final boolean isStocking;
    protected final int index;
    protected final BooleanSupplier isAutoPull;

    private static final IDrawable normalBackground = IDrawable.of(GTGuiTextures.SLOT, GTGuiTextures.CONFIG_ARROW_DARK);
    private static final IDrawable autoPullBackground = IDrawable.of(GTGuiTextures.SLOT_DARK,
            GTGuiTextures.CONFIG_ARROW);

    public AEConfigSlot(boolean isStocking, int index, BooleanSupplier isAutoPull) {
        this.isStocking = isStocking;
        this.index = index;
        this.isAutoPull = isAutoPull;
        size(18);
    }

    @Override
    public void onInit() {
        tooltipBuilder(this::buildTooltip);
    }

    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot"));

        if (isAutoPull.getAsBoolean()) {
            tooltip.add(I18n.format("gregtech.gui.config_slot.auto_pull_managed"));
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

    // TODO: get rid of these two methods when 2817 merges
    protected void drawSlotOverlay() {
        GlStateManager.colorMask(true, true, true, false);
        GuiDraw.drawRect(1, 1, 16, 16, getSlotHoverColor());
        GlStateManager.colorMask(true, true, true, true);
    }

    public int getSlotHoverColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetSlotTheme slotTheme) {
            return slotTheme.getSlotHoverColor();
        }
        return ITheme.getDefault().getItemSlotTheme().getSlotHoverColor();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return Interactable.super.onMousePressed(mouseButton);
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int scrollAmount) {
        if (getSyncHandler().getConfig(index) == null || isStocking) return false;

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

        if (newStackSize > 0 && newStackSize < Integer.MAX_VALUE + 1L) {
            int scaledStackSize = (int) newStackSize;
            getSyncHandler().setConfigAmount(index, scaledStackSize);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable IDrawable getBackground() {
        return isAutoPull.getAsBoolean() ? autoPullBackground : normalBackground;
    }
}
