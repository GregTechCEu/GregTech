package gregtech.common.covers.filter;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import net.minecraft.network.PacketBuffer;

import java.util.function.Supplier;

public class WidgetGroupItemFilter extends AbstractWidgetGroup {

    private final Supplier<ItemFilter> itemFilterSupplier;
    private ItemFilter itemFilter;
    private int maxStackSize = 1;

    public WidgetGroupItemFilter(int yPosition, Supplier<ItemFilter> itemFilterSupplier) {
        super(new Position(0, yPosition));
        this.itemFilterSupplier = itemFilterSupplier;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        ItemFilter newItemFilter = itemFilterSupplier.get();
        if (itemFilter != newItemFilter) {
            clearAllWidgets();
            this.itemFilter = newItemFilter;
            if (itemFilter != null) {
                this.itemFilter.initUI(this::addWidget);
            }
            writeUpdateInfo(2, buffer -> {
                if (itemFilter != null) {
                    buffer.writeBoolean(true);
                    int filterId = FilterTypeRegistry.getIdForItemFilter(itemFilter);
                    buffer.writeVarInt(filterId);
                } else {
                    buffer.writeBoolean(false);
                }
            });
        }
        int newMaxStackSize = itemFilter == null ? 1 : itemFilter.getMaxStackSize();
        if (maxStackSize != newMaxStackSize) {
            this.maxStackSize = newMaxStackSize;
            writeUpdateInfo(3, buffer -> buffer.writeVarInt(maxStackSize));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            clearAllWidgets();
            if (buffer.readBoolean()) {
                int filterId = buffer.readVarInt();
                this.itemFilter = FilterTypeRegistry.createItemFilterById(filterId);
                this.itemFilter.initUI(this::addWidget);
                this.itemFilter.setMaxStackSize(maxStackSize);
            }
        } else if (id == 3) {
            this.maxStackSize = buffer.readVarInt();
            if (itemFilter != null) {
                itemFilter.setMaxStackSize(maxStackSize);
            }
        }
    }
}
