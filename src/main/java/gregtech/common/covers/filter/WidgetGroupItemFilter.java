package gregtech.common.covers.filter;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * @deprecated in favor of new MUI
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.10")
public class WidgetGroupItemFilter extends AbstractWidgetGroup {

    private final Supplier<BaseFilter> itemFilterSupplier;
    private BaseFilter itemFilter;
    private int maxStackSize = 1;

    public WidgetGroupItemFilter(int yPosition, Supplier<BaseFilter> itemFilterSupplier) {
        super(new Position(0, yPosition));
        this.itemFilterSupplier = itemFilterSupplier;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        BaseFilter newItemFilter = itemFilterSupplier.get();
        if (itemFilter != newItemFilter) {
            clearAllWidgets();
            this.itemFilter = newItemFilter;
            if (itemFilter != null) {
                this.itemFilter.initUI(this::addWidget);
            }
            writeUpdateInfo(2, buffer -> {
                if (itemFilter != null) {
                    buffer.writeBoolean(true);
                    buffer.writeItemStack(itemFilter.getContainerStack());
                } else {
                    buffer.writeBoolean(false);
                }
            });
        }
        int newMaxStackSize = itemFilter == null ? 1 : itemFilter.getMaxTransferSize();
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
                // int filterId = buffer.readVarInt();
                try {
                    this.itemFilter = BaseFilter.getFilterFromStack(buffer.readItemStack());
                    this.itemFilter.initUI(this::addWidget);
                    this.itemFilter.setMaxTransferSize(maxStackSize);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (id == 3) {
            this.maxStackSize = buffer.readVarInt();
            if (itemFilter != null) {
                itemFilter.setMaxTransferSize(maxStackSize);
            }
        }
    }
}
