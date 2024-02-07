package gregtech.common.covers.filter;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

/**
 * @deprecated in favor of new MUI
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.10")
public class WidgetGroupFluidFilter extends AbstractWidgetGroup {

    private final Supplier<IFluidFilter> fluidFilterSupplier;
    private final Supplier<Boolean> showTipSupplier;
    private IFluidFilter fluidFilter;

    public WidgetGroupFluidFilter(int yPosition, Supplier<IFluidFilter> fluidFilterSupplier,
                                  Supplier<Boolean> showTipSupplier) {
        super(new Position(18 + 5, yPosition));
        this.fluidFilterSupplier = fluidFilterSupplier;
        this.showTipSupplier = showTipSupplier;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        IFluidFilter newFluidFilter = fluidFilterSupplier.get();
        if (fluidFilter != newFluidFilter) {
            clearAllWidgets();
            this.fluidFilter = newFluidFilter;
            if (fluidFilter != null) {
                this.fluidFilter.initUI(this::addWidget);
            }
            writeUpdateInfo(2, buffer -> {
                if (fluidFilter != null) {
                    buffer.writeBoolean(true);
                    int filterId = FilterTypeRegistry.getIdForFluidFilter(fluidFilter);
                    buffer.writeVarInt(filterId);
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
                int filterId = buffer.readVarInt();
                this.fluidFilter = FilterTypeRegistry.createFluidFilterById(filterId);
                this.fluidFilter.initUI(this::addWidget);
            }
        } else if (id == 3) {
            // fluidFilter.showTip = buffer.readBoolean();
        }
    }
}
