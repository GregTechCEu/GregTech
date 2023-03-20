package gregtech.api.gui.widgets;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;


/**
 * Used for setting a "ghost" IC for a machine
 */
public class GhostCircuitSlotWidget extends SlotWidget {

    private static final int SET_TO_ZERO = 1;
    private static final int SET_TO_EMPTY = 2;
    private static final int SET_TO_N = 3;

    private final GhostCircuitItemStackHandler circuitInventory;

    public GhostCircuitSlotWidget(GhostCircuitItemStackHandler circuitInventory, int slotIndex, int xPosition, int yPosition) {
        super(circuitInventory, slotIndex, xPosition, yPosition, false, false, false);
        this.circuitInventory = circuitInventory;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (button == 0 && !this.circuitInventory.hasCircuitValue()) {
                this.circuitInventory.setCircuitValue(0);
                writeClientAction(SET_TO_ZERO, buf -> {});
            } else if (button == 1 && this.circuitInventory.hasCircuitValue()) {
                this.circuitInventory.setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
                writeClientAction(SET_TO_EMPTY, buf -> {});
            } else {
                this.gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (this.circuitInventory.hasCircuitValue()) {
                int dir = wheelDelta >= 0 ? 1 : -1;
                int prevValue = this.circuitInventory.getCircuitValue();
                this.circuitInventory.addCircuitValue(dir * (isShiftDown() ? 5 : 1));

                int newValue = this.circuitInventory.getCircuitValue();
                if (prevValue != newValue) {
                    writeClientAction(SET_TO_N, buf -> buf.writeVarInt(newValue));
                }
            } else {
                super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return false;
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack stackHeld = player.inventory.getItemStack();

        if (IntCircuitIngredient.isIntegratedCircuit(stackHeld)) {
            this.circuitInventory.setCircuitValueFromStack(stackHeld);
            return this.circuitInventory.getStackInSlot(0).copy();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        switch (id) {
            case SET_TO_ZERO:
                this.circuitInventory.setCircuitValue(0);
                return;
            case SET_TO_EMPTY:
                this.circuitInventory.setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
                return;
            case SET_TO_N:
                this.circuitInventory.setCircuitValue(buffer.readVarInt());
        }
    }
}
