package gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.util.SlotUtil;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PhantomSlotWidget extends SlotWidget implements IGhostIngredientTarget {

    public PhantomSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, false, true);
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack stackHeld = player.inventory.getItemStack();
        return SlotUtil.slotClickPhantom(slotReference, dragType, clickTypeIn, stackHeld);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<Object>() {
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    int mouseButton = Mouse.getEventButton();
                    boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                    writeClientAction(1, buffer -> {
                        buffer.writeItemStack((ItemStack) ingredient);
                        buffer.writeVarInt(mouseButton);
                        buffer.writeBoolean(shiftDown);
                    });
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            ItemStack stackHeld;
            try {
                stackHeld = buffer.readItemStack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int mouseButton = buffer.readVarInt();
            boolean shiftKeyDown = buffer.readBoolean();
            ClickType clickType = shiftKeyDown ? ClickType.QUICK_MOVE : ClickType.PICKUP;
            SlotUtil.slotClickPhantom(slotReference, mouseButton, clickType, stackHeld);
        }
    }
}
