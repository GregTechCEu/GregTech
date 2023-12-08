package gregtech.api.mui.widget;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.client.utils.TooltipHelper;

import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.ItemSlot;
import org.jetbrains.annotations.NotNull;

public class GhostCircuitSlotWidget extends ItemSlot {

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton == 0 && TooltipHelper.isShiftDown()) {
            // open popup on shift-left-click
            // todo
        } else if (mouseButton == 0) {
            // increment on left-click
            setCircuitValue(getNextCircuitValue(true));
        } else if (mouseButton == 1 && TooltipHelper.isShiftDown()) {
            // clear on shift-right-click
            setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
        } else if (mouseButton == 1) {
            // decrement on right-click
            setCircuitValue(getNextCircuitValue(false));
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        setCircuitValue(getNextCircuitValue(scrollDirection.isUp()));
        return false;
    }

    private void setCircuitValue(int value) {
        GhostCircuitItemStackHandler handler = getGhostCircuitHandler();
        handler.setCircuitValue(value);
        getSyncHandler().updateFromClient(handler.getStackInSlot(0));
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
        IItemHandler handler = getSyncHandler().getSlot().getItemHandler();
        if (!(handler instanceof GhostCircuitItemStackHandler ghostHandler)) {
            throw new IllegalStateException(
                    "GhostCircuitSlotWidget has IItemHandler that is not GhostCircuitItemStackHandler");
        }
        return ghostHandler;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {}

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }
}
