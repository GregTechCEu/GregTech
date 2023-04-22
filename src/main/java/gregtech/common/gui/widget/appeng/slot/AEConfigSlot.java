package gregtech.common.gui.widget.appeng.slot;

import appeng.api.storage.data.IAEStack;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.gui.widget.appeng.AEConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author GlodBlock
 * @Description A configurable slot
 * @Date 2023/4/22-0:30
 */
public class AEConfigSlot<T extends IAEStack<T>> extends Widget {

    protected AEConfigWidget<T> parentWidget;
    protected int index;
    protected final static int REMOVE_ID = 1000;
    protected final static int UPDATE_ID = 1001;
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
            hoverStringList.add(I18n.format("gregtech.gui.config_slot"));
            hoverStringList.add(I18n.format("gregtech.gui.config_slot.set"));
            hoverStringList.add(I18n.format("gregtech.gui.config_slot.remove"));
            drawHoveringText(ItemStack.EMPTY, hoverStringList, -1, mouseX, mouseY);
        }
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
        return isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
    }

}
