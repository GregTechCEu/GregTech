package gregtech.api.mui.widget;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widgets.ItemSlot;

import java.util.function.BooleanSupplier;

/**
 * Basically just your normal SlotWidget, but can render the slot as "grayed-out" with a Supplier value.
 * MUI2 version of {@link gregtech.api.gui.widgets.BlockableSlotWidget}
 */
public class BlockableSlotWidget extends ItemSlot {

    private static final int OVERLAY_COLOR = 0x80404040;

    private BooleanSupplier isBlocked = () -> false;

    public BlockableSlotWidget() {
        super();
    }

    public BlockableSlotWidget setIsBlocked(BooleanSupplier isBlocked) {
        this.isBlocked = isBlocked;
        return this;
    }

    @Override
    public boolean isHovering() {
        return super.isHovering() && !isBlocked.getAsBoolean();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        super.draw(context, widgetTheme);
        if (isBlocked.getAsBoolean()) {
            GuiDraw.drawRect(1, 1, 16, 16, OVERLAY_COLOR);
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        if (isBlocked.getAsBoolean()) return;
        super.drawForeground(context);
    }
}
