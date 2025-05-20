package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>> {

    protected final IConfigurableSlot<T> backingSlot;
    protected final boolean isStocking;

    public AEConfigSlot(IConfigurableSlot<T> backingSlot, boolean isStocking) {
        this.backingSlot = backingSlot;
        this.isStocking = isStocking;
    }

    @Override
    public void onInit() {
        tooltipBuilder(this::buildTooltip);
    }

    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot"));
        if (isStocking) {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set_only"));
        } else {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set"));
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.scroll"));
        }
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot.remove"));
    }

    protected abstract class AEConfigSyncHandler<T extends IAEStack<T>> extends SyncHandler {

        public static final int jeiDropSyncID = 0;
        public static final int configSyncID = 1;

        protected final IConfigurableSlot<T> config;
        @Nullable
        protected T cache;

        public AEConfigSyncHandler(IConfigurableSlot<T> config) {
            this.config = config;
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            T newConfig = config.getConfig();
            if (!areAEStackCountEquals(newConfig, cache)) {
                cache = newConfig == null ? null : newConfig.copy();
                syncToClient(configSyncID, buf -> {
                    if (newConfig == null) {
                        buf.writeBoolean(false);
                    } else {
                        buf.writeBoolean(true);
                        newConfig.writeToPacket(buf);
                    }
                });
            }
        }

        public final boolean areAEStackCountEquals(T stack1, T stack2) {
            if (stack2 == stack1) {
                return true;
            }

            if (stack1 != null && stack2 != null) {
                return stack1.getStackSize() == stack2.getStackSize() && stack1.equals(stack2);
            }

            return false;
        }

        protected void setConfig(T config) {

        }
    }
}
