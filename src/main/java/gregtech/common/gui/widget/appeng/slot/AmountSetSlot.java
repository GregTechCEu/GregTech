package gregtech.common.gui.widget.appeng.slot;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.Position;
import gregtech.common.gui.widget.appeng.AEConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;

import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEStack;

/**
 * @Author GlodBlock
 * @Description The amount set widget for config slot
 * @Date 2023/4/21-21:20
 */
public class AmountSetSlot<T extends IAEStack<T>> extends Widget {

    private int index = -1;
    private final TextFieldWidget2 amountText;
    private final AEConfigWidget<T> parentWidget;

    public AmountSetSlot(int x, int y, AEConfigWidget<T> widget) {
        super(x, y, 80, 30);
        this.parentWidget = widget;
        this.amountText = new TextFieldWidget2(x + 3, y + 14, 60, 15, this::getAmountStr, this::setNewAmount)
                .setNumbersOnly(0, Integer.MAX_VALUE)
                .setMaxLength(10);
    }

    public void setSlotIndex(int slotIndex) {
        this.index = slotIndex;
        writeClientAction(0, buf -> buf.writeVarInt(this.index));
    }

    public TextFieldWidget2 getText() {
        return this.amountText;
    }

    public String getAmountStr() {
        if (this.index < 0) {
            return "0";
        }
        IConfigurableSlot<T> slot = this.parentWidget.getConfig(this.index);
        if (slot.getConfig() != null) {
            return String.valueOf(slot.getConfig().getStackSize());
        }
        return "0";
    }

    public void setNewAmount(String amount) {
        try {
            long newAmount = Long.parseLong(amount);
            writeClientAction(1, buf -> buf.writeVarLong(newAmount));
        } catch (NumberFormatException ignore) {}
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 0) {
            this.index = buffer.readVarInt();
        } else if (id == 1) {
            if (this.index < 0) {
                return;
            }
            IConfigurableSlot<T> slot = this.parentWidget.getConfig(this.index);
            long newAmt = buffer.readVarLong();
            if (newAmt > 0 && slot.getConfig() != null) {
                slot.getConfig().setStackSize(newAmt);
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        GuiTextures.BACKGROUND.draw(position.x, position.y, 80, 30);
        drawStringSized("Amount", position.x + 3, position.y + 3, 0x404040, false, 1f, false);
        GuiTextures.DISPLAY.draw(position.x + 3, position.y + 11, 65, 14);
    }
}
