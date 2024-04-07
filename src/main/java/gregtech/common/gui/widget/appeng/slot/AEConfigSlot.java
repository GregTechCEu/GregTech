package gregtech.common.gui.widget.appeng.slot;

import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.gui.widget.appeng.AEConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEStack;
import mezz.jei.api.gui.IGhostIngredientHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AEConfigSlot<T extends IAEStack<T>> extends Widget implements IGhostIngredientTarget {

    protected static final int DISPLAY_X_OFFSET = 18 * 5;
    protected AEConfigWidget<T> parentWidget;
    protected int index;
    protected final static int REMOVE_ID = 1000;
    protected final static int UPDATE_ID = 1001;
    protected final static int AMOUNT_CHANGE_ID = 1002;
    protected boolean select = false;

    public AEConfigSlot(Position pos, Size size, AEConfigWidget<T> widget, int index) {
        super(pos, size);
        this.parentWidget = widget;
        this.index = index;
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        IConfigurableSlot<T> slot = this.parentWidget.getDisplay(this.index);
        if (slot.getConfig() == null && mouseOverConfig(mouseX, mouseY)) {
            List<String> hoverStringList = new ArrayList<>();
            addHoverText(hoverStringList);
            if (!hoverStringList.isEmpty()) {
                drawHoveringText(ItemStack.EMPTY, hoverStringList, -1, mouseX, mouseY);
            }
        }
    }

    protected void addHoverText(List<String> hoverText) {
        hoverText.add(I18n.format("gregtech.gui.config_slot"));
        if (!parentWidget.isStocking()) {
            hoverText.add(I18n.format("gregtech.gui.config_slot.set"));
            hoverText.add(I18n.format("gregtech.gui.config_slot.scroll"));
        } else {
            hoverText.add(I18n.format("gregtech.gui.config_slot.set_only"));
        }
        hoverText.add(I18n.format("gregtech.gui.config_slot.remove"));
    }

    public void setSelect(boolean val) {
        this.select = val;
    }

    protected boolean mouseOverConfig(int mouseX, int mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x, position.y, 18, 18, mouseX, mouseY);
    }

    protected boolean mouseOverStock(int mouseX, int mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x + DISPLAY_X_OFFSET, position.y, 18, 18, mouseX, mouseY);
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        return Collections.emptyList();
    }

    public AEConfigWidget<T> getParentWidget() {
        return parentWidget;
    }
}
