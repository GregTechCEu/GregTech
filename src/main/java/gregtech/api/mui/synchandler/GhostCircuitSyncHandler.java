package gregtech.api.mui.synchandler;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;

public class GhostCircuitSyncHandler extends ItemSlotSH {

    public GhostCircuitSyncHandler(ModularSlot slot) {
        super(slot);
    }

    @Override
    protected void phantomClick(MouseData mouseData) {
        if (mouseData.mouseButton == 0) {
            // increment on left-click
            setCircuitValue(getNextCircuitValue(true));
        } else if (mouseData.mouseButton == 1 && mouseData.shift) {
            // clear on shift-right-click
            setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
        } else if (mouseData.mouseButton == 1) {
            // decrement on right-click
            setCircuitValue(getNextCircuitValue(false));
        }
    }

    @Override
    protected void phantomScroll(MouseData mouseData) {
        setCircuitValue(getNextCircuitValue(mouseData.mouseButton == 1));
    }

    public void setCircuitValue(int value) {
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

    private int getNextCircuitValue(boolean increment) {
        GhostCircuitItemStackHandler handler = getGhostCircuitHandler();
        if (increment) {
            // if at max, loop around to no circuit
            if (handler.getCircuitValue() == IntCircuitIngredient.CIRCUIT_MAX) {
                return GhostCircuitItemStackHandler.NO_CONFIG;
            }
            // if at no circuit, skip 0 and return 1
            if (!handler.hasCircuitValue()) {
                return 1;
            }
            // normal case: increment by 1
            return handler.getCircuitValue() + 1;
        } else {
            // if at no circuit, loop around to max
            if (!handler.hasCircuitValue()) {
                return IntCircuitIngredient.CIRCUIT_MAX;
            }
            // if at 1, skip 0 and return no circuit
            if (handler.getCircuitValue() == 1) {
                return GhostCircuitItemStackHandler.NO_CONFIG;
            }
            // normal case: decrement by 1
            return handler.getCircuitValue() - 1;
        }
    }

    private GhostCircuitItemStackHandler getGhostCircuitHandler() {
        IItemHandler handler = getSlot().getItemHandler();
        if (!(handler instanceof GhostCircuitItemStackHandler ghostHandler)) {
            throw new IllegalStateException(
                    "GhostCircuitSyncHandler has IItemHandler that is not GhostCircuitItemStackHandler");
        }
        return ghostHandler;
    }
}
