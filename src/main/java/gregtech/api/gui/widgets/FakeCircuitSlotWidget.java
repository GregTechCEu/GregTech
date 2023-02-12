package gregtech.api.gui.widgets;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;


/**
 * Used for setting a "ghost" IC for a machine
 */
public class FakeCircuitSlotWidget extends SlotWidget {

    public FakeCircuitSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, false, false, false);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (button == 0 && slotReference.getStack().isEmpty()) {
                slotReference.putStack(IntCircuitIngredient.getIntegratedCircuit(0));
                writeClientAction(1, buf -> {});
            }
            else if (button == 1 && !slotReference.getStack().isEmpty()){
                slotReference.putStack(ItemStack.EMPTY);
                writeClientAction(2, buf -> {});
            }
            else {
                gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (!slotReference.getStack().isEmpty()) {
                int dir = wheelDelta >= 0 ? 1 : -1;
                int currentConfig = IntCircuitIngredient.getCircuitConfiguration(slotReference.getStack());
                int newConfig = currentConfig + dir;

                // return early if invalid
                if (newConfig < 0 || newConfig > IntCircuitIngredient.CIRCUIT_MAX){
                    return true;
                }

                slotReference.putStack(IntCircuitIngredient.getIntegratedCircuit(newConfig));
                writeClientAction(3, buf -> buf.writeInt(newConfig));
            }
            else {
                super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) { return false; }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack stackHeld = player.inventory.getItemStack();

        if (IntCircuitIngredient.isIntegratedCircuit(stackHeld)){
            ItemStack ic = stackHeld.copy();
            ic.setCount(1);
            slotReference.putStack(ic);
            return ic;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) { return false; }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            slotReference.putStack(IntCircuitIngredient.getIntegratedCircuit(0));
        } else if (id == 2) {
            slotReference.putStack(ItemStack.EMPTY);
        } else if (id == 3) {
            slotReference.putStack(IntCircuitIngredient.getIntegratedCircuit(buffer.readInt()));
        }
    }
}
