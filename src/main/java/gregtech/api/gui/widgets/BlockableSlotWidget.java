package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.BooleanSupplier;

/** Basically just your normal SlotWidget, but can render the slot as "grayed-out" with a Supplier value. */
public class BlockableSlotWidget extends SlotWidget {

    private static final int OVERLAY_COLOR = 0x80404040;

    private BooleanSupplier isBlocked = () -> false;

    public BlockableSlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                               boolean canPutItems) {
        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BlockableSlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition,
                               boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BlockableSlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition,
                               boolean canTakeItems, boolean canPutItems, boolean canShiftClickInto) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems, canShiftClickInto);
    }

    public BlockableSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
    }

    public BlockableSlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition) {
        super(inventory, slotIndex, xPosition, yPosition);
    }

    public BlockableSlotWidget setIsBlocked(BooleanSupplier isBlocked) {
        this.isBlocked = isBlocked;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        if (isBlocked.getAsBoolean()) {
            Position pos = getPosition();
            Size size = getSize();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            drawSolidRect(pos.getX() + 1, pos.getY() + 1, size.getWidth() - 2, size.getHeight() - 2, OVERLAY_COLOR);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }
    }

    @Override
    public boolean isMouseOverElement(int mouseX, int mouseY) {
        // prevent slot removal and hover highlighting when slot is blocked
        return super.isMouseOverElement(mouseX, mouseY) && !isBlocked.getAsBoolean();
    }
}
