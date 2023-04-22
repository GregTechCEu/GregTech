package gregtech.common.gui.widget.appeng.slot;

import appeng.api.storage.data.IAEStack;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.gui.widget.appeng.AEConfigWidget;

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
