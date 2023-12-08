package gregtech.api.mui.widget;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import gregtech.api.mui.synchandler.GhostCircuitSyncHandler;
import gregtech.client.utils.TooltipHelper;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.ItemSlot;
import org.jetbrains.annotations.NotNull;

public class GhostCircuitSlotWidget extends ItemSlot {

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton == 0 && TooltipHelper.isShiftDown()) {
            // todo open popup on shift-left-click

        } else {
            MouseData mouseData = MouseData.create(mouseButton);
            getSyncHandler().syncToServer(2, mouseData::writeToPacket);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        getSyncHandler().syncToServer(3, mouseData::writeToPacket);
        return false;
    }

    @Override
    public ItemSlot slot(ModularSlot slot) {
        ItemSlotSH sh = new GhostCircuitSyncHandler(slot);
        isValidSyncHandler(sh);
        setSyncHandler(sh);
        return this;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {}

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }
}
