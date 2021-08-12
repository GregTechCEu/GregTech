package gregtech.api.terminal.app.recipegraph.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.IDraggable;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class RGNode extends WidgetGroup implements IDraggable {
    IItemHandlerModifiable itemHandler = new SingleItemStackHandler(1);

    public RGNode(int x, int y) {
        super(x, y, 50, 40);
        this.addWidget(new SimpleTextWidget(25, 5, "", -1, () -> itemHandler.getStackInSlot(0).getDisplayName(), true));
        this.addWidget(new PhantomSlotWidget(itemHandler, 0, 0, 20)
                .setChangeListener(this::onItemChanged)
                .setBackgroundTexture(TerminalTheme.COLOR_B_2));
    }

    private void onItemChanged() {
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        drawSolidRect(x, y, width, height, TerminalTheme.COLOR_B_1.color);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public boolean allowDrag(int mouseX, int mouseY, int button) {
        return isMouseOverElement(mouseX, mouseY);
    }
}
