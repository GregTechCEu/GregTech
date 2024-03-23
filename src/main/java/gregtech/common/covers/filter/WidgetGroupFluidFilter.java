package gregtech.common.covers.filter;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * @deprecated in favor of new MUI
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.10")
public class WidgetGroupFluidFilter extends AbstractWidgetGroup {

    private final Supplier<BaseFilter> fluidFilterSupplier;
    private final Supplier<Boolean> showTipSupplier;
    private BaseFilter fluidFilter;

    public WidgetGroupFluidFilter(int yPosition, Supplier<BaseFilter> fluidFilterSupplier,
                                  Supplier<Boolean> showTipSupplier) {
        super(new Position(18 + 5, yPosition));
        this.fluidFilterSupplier = fluidFilterSupplier;
        this.showTipSupplier = showTipSupplier;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        BaseFilter newFluidFilter = fluidFilterSupplier.get();
        if (fluidFilter != newFluidFilter) {
            clearAllWidgets();
            this.fluidFilter = newFluidFilter;
            if (fluidFilter != null) {
                this.fluidFilter.initUI(this::addWidget);
            }
            writeUpdateInfo(2, buffer -> {
                if (fluidFilter != null) {
                    buffer.writeBoolean(true);
                    buffer.writeItemStack(fluidFilter.getContainerStack());
                } else {
                    buffer.writeBoolean(false);
                }
            });
        }
        // if (fluidFilter != null && showTipSupplier != null && fluidFilter.showTip != showTipSupplier.get()) {
        // fluidFilter.showTip = showTipSupplier.get();
        // writeUpdateInfo(3, buffer -> buffer.writeBoolean(fluidFilter.showTip));
        // }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            clearAllWidgets();
            if (buffer.readBoolean()) {
                ItemStack stack;
                try {
                    stack = buffer.readItemStack();
                } catch (IOException e) {
                    GTLog.logger.warn(e);
                    return;
                }
                this.fluidFilter = BaseFilter.getFilterFromStack(stack);
                this.fluidFilter.initUI(this::addWidget);
            }
        } else if (id == 3) {
            // fluidFilter.showTip = buffer.readBoolean();
        }
    }
}
