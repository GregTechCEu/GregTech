package gregtech.common.gui.widget.appeng;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ScrollableListWidget;

import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

/**
 * @Author GlodBlock
 * @Description A display only widget for {@link IItemList}
 * @Date 2023/4/19-0:18
 */
public abstract class AEListGridWidget<T extends IAEStack<T>> extends ScrollableListWidget {

    protected final IItemList<T> list;
    private final int slotAmountY;
    private int slotRowsAmount = 0;
    protected final static int ROW_CHANGE_ID = 2;
    protected final static int CONTENT_CHANGE_ID = 3;

    public AEListGridWidget(int x, int y, int slotsY, IItemList<T> internalList) {
        super(x, y, 18 + 140, slotsY * 18);
        this.list = internalList;
        this.slotAmountY = slotsY;
    }

    public abstract T getAt(int index);

    protected abstract void addSlotRows(int amount);

    private void removeSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            Widget slotWidget = this.widgets.remove(this.widgets.size() - 1);
            removeWidget(slotWidget);
        }
    }

    private void modifySlotRows(int delta) {
        if (delta > 0) {
            addSlotRows(delta);
        } else {
            removeSlotRows(delta);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.list == null) return;
        int amountOfTypes = this.list.size();
        int slotRowsRequired = Math.max(this.slotAmountY, amountOfTypes);
        if (this.slotRowsAmount != slotRowsRequired) {
            int slotsToAdd = slotRowsRequired - this.slotRowsAmount;
            this.slotRowsAmount = slotRowsRequired;
            this.writeUpdateInfo(ROW_CHANGE_ID, buf -> buf.writeVarInt(slotsToAdd));
            this.modifySlotRows(slotsToAdd);
        }
        this.writeListChange();
    }

    protected abstract void writeListChange();

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == ROW_CHANGE_ID) {
            int slotsToAdd = buffer.readVarInt();
            this.modifySlotRows(slotsToAdd);
        }
        if (id == CONTENT_CHANGE_ID) {
            this.readListChange(buffer);
        }
    }

    protected abstract void readListChange(PacketBuffer buffer);
}
