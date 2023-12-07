package gregtech.common.gui.widget.appeng;

import gregtech.api.gui.Widget;
import gregtech.common.gui.widget.appeng.slot.AEItemDisplayWidget;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.io.IOException;

/**
 * @Author GlodBlock
 * @Description Display item list
 * @Date 2023/4/19-21:33
 */
public class AEItemGridWidget extends AEListGridWidget<IAEItemStack> {

    private final Object2LongMap<IAEItemStack> changeMap = new Object2LongOpenHashMap<>();
    protected final IItemList<IAEItemStack> cached = new ItemList();
    protected final IItemList<IAEItemStack> displayList = new ItemList();

    public AEItemGridWidget(int x, int y, int slotsY, IItemList<IAEItemStack> internalList) {
        super(x, y, slotsY, internalList);
    }

    @Override
    public IAEItemStack getAt(int index) {
        int cnt = 0;
        for (IAEItemStack item : this.displayList) {
            if (cnt == index) {
                return item;
            }
            cnt++;
        }
        return null;
    }

    @Override
    protected void addSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            int widgetAmount = this.widgets.size();
            Widget widget = new AEItemDisplayWidget(0, 0, this, widgetAmount);
            this.addWidget(widget);
        }
    }

    @Override
    protected void writeListChange() {
        this.changeMap.clear();
        // Remove item
        for (IAEItemStack item : this.cached) {
            if (this.list.findPrecise(item) == null || this.list.findPrecise(item).getStackSize() == 0) {
                this.changeMap.put(item.copy(), -item.getStackSize());
                item.reset();
            }
        }
        // Change/Add item
        for (IAEItemStack item : this.list) {
            IAEItemStack cachedItem = this.cached.findPrecise(item);
            if (cachedItem == null || cachedItem.getStackSize() == 0) {
                this.changeMap.put(item.copy(), item.getStackSize());
                this.cached.add(item.copy());
            } else {
                if (cachedItem.getStackSize() != item.getStackSize()) {
                    this.changeMap.put(item.copy(), item.getStackSize() - cachedItem.getStackSize());
                    this.cached.add(item.copy().setStackSize(item.getStackSize() - cachedItem.getStackSize()));
                }
            }
        }
        this.writeUpdateInfo(CONTENT_CHANGE_ID, buf -> {
            buf.writeVarInt(this.changeMap.size());
            for (IAEItemStack item : this.changeMap.keySet()) {
                buf.writeItemStack(item.createItemStack());
                buf.writeVarLong(this.changeMap.get(item));
            }
        });
    }

    @Override
    protected void readListChange(PacketBuffer buffer) {
        int size = buffer.readVarInt();
        try {
            for (int i = 0; i < size; i++) {
                ItemStack item = buffer.readItemStack();
                item.setCount(1);
                long delta = buffer.readVarLong();
                if (!item.isEmpty()) {
                    this.displayList.add(AEItemStack.fromItemStack(item).setStackSize(delta));
                }
            }
        } catch (IOException ignore) {}
    }
}
