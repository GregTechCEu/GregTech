package gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTLog;
import gregtech.api.util.SlotUtil;
import gregtech.client.utils.TooltipHelper;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GhostCircuitSlotWidget extends SlotWidget {

    public GhostCircuitSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, false, false, false);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (button == 0 && slotReference.getStack().isEmpty()) {
                slotReference.putStack(IntCircuitIngredient.getIntegratedCircuit(0));
                writeClientAction(1, buf -> {});
            }
            else if (button == 1 && TooltipHelper.isShiftDown() && !slotReference.getStack().isEmpty()){
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
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) { return false; }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack stackHeld = player.inventory.getItemStack();

        if (IntCircuitIngredient.isIntegratedCircuit(stackHeld)){
            ItemStack ic = stackHeld.copy();
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
        }
    }
}
