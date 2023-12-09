package gregtech.api.mui.synchandler;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import java.io.IOException;

public class GhostCircuitSyncHandler extends ItemSlotSH {

    public GhostCircuitSyncHandler(ModularSlot slot) {
        super(slot);
    }

    @Override
    protected void phantomClick(MouseData mouseData) {
        if (mouseData.mouseButton == 0) {
            // increment on left-click
            setCircuitValue(getNextCircuitValue(1));
        } else if (mouseData.mouseButton == 1 && mouseData.shift) {
            // clear on shift-right-click
            setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
        } else if (mouseData.mouseButton == 1) {
            // decrement on right-click
            setCircuitValue(getNextCircuitValue(-1));
        }
    }

    @Override
    protected void phantomScroll(MouseData mouseData) {
        setCircuitValue(getNextCircuitValue(mouseData.mouseButton));
    }

    private void setCircuitValue(int value) {
        GhostCircuitItemStackHandler handler = getGhostCircuitHandler();
        handler.setCircuitValue(value);
        syncToClient(1, buf -> {
            buf.writeBoolean(false);
            buf.writeItemStack(handler.getStackInSlot(0));
            buf.writeBoolean(false);
        });
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 10) {
            setCircuitValue(buf.readShort());
        } else {
            super.readOnServer(id, buf);
        }
    }

    private int getNextCircuitValue(int delta) {
        GhostCircuitItemStackHandler handler = getGhostCircuitHandler();

        // if no circuit, skip 0 and return 32 if decrementing,
        // or, skip 0 and return 1 when incrementing
        if (!handler.hasCircuitValue()) {
            return delta == 1 ? 1 : IntCircuitIngredient.CIRCUIT_MAX;
            // if at max, loop around to no circuit
        } else if (handler.getCircuitValue() + delta > IntCircuitIngredient.CIRCUIT_MAX) {
            return GhostCircuitItemStackHandler.NO_CONFIG;
            // if at 1, skip 0 and return to no circuit
        } else if (handler.getCircuitValue() + delta < 1) {
            return GhostCircuitItemStackHandler.NO_CONFIG;
        }

        // normal case: change by "delta" which is either 1 or -1
        return handler.getCircuitValue() + delta;
    }

    public GhostCircuitItemStackHandler getGhostCircuitHandler() {
        IItemHandler handler = getSlot().getItemHandler();
        if (!(handler instanceof GhostCircuitItemStackHandler ghostHandler)) {
            throw new IllegalStateException(
                    "GhostCircuitSyncHandler has IItemHandler that is not GhostCircuitItemStackHandler");
        }
        return ghostHandler;
    }
}
