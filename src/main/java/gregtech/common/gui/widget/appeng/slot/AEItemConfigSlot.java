package gregtech.common.gui.widget.appeng.slot;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.gui.widget.appeng.AEConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * @Author GlodBlock
 * @Description A configurable slot for {@link IAEItemStack}
 * @Date 2023/4/22-0:48
 */
public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> {

    public AEItemConfigSlot(int x, int y, AEConfigWidget<IAEItemStack> widget, int index) {
        super(new Position(x, y), new Size(18, 18 * 2), widget, index);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        IConfigurableSlot<IAEItemStack> slot = this.parentWidget.getDisplay(this.index);
        IAEItemStack config = slot.getConfig();
        IAEItemStack stock = slot.getStock();
        GuiTextures.SLOT.draw(position.x, position.y, 18, 18);
        GuiTextures.SLOT.draw(position.x, position.y + 18, 18, 18);
        GuiTextures.CONFIG_ARROW_DARK.draw(position.x, position.y, 18, 18);
        if (this.select) {
            GuiTextures.SELECT_BOX.draw(position.x, position.y, 18, 18);
        }
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            ItemStack stack = config.createItemStack();
            stack.setCount(1);
            drawItemStack(stack, stackX, stackY, null);
        }
        if (stock != null) {
            ItemStack stack = stock.createItemStack();
            stack.setCount(1);
            drawItemStack(stack, stackX, stackY + 18, null);
            String amountStr = TextFormattingUtil.formatLongToCompactString(stock.getStackSize(), 4);
            drawStringFixedCorner(amountStr, stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
        }
        if (mouseOverConfig(mouseX, mouseY)) {
            drawSelectionOverlay(stackX, stackY, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY)) {
            drawSelectionOverlay(stackX, stackY + 18, 16, 16);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        IAEItemStack item = null;
        IConfigurableSlot<IAEItemStack> slot = this.parentWidget.getDisplay(this.index);
        if (mouseOverConfig(mouseX, mouseY)) {
            item = slot.getConfig();
        } else if (mouseOverStock(mouseX, mouseY)) {
            item = slot.getStock();
        }
        if (item != null) {
            drawHoveringText(item.createItemStack(), getItemToolTip(item.createItemStack()), -1, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            if (button == 1) {
                // Right click to clear
                this.parentWidget.disableAmount();
                writeClientAction(REMOVE_ID, buf -> {});
            } else if (button == 0) {
                // Left click to set/select
                ItemStack item = this.gui.entityPlayer.inventory.getItemStack();

                if (!item.isEmpty()) {
                    writeClientAction(UPDATE_ID, buf -> buf.writeItemStack(item));
                }
                this.parentWidget.enableAmount(this.index);
                this.select = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        IConfigurableSlot<IAEItemStack> slot = this.parentWidget.getConfig(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
            this.parentWidget.disableAmount();
            writeUpdateInfo(REMOVE_ID, buf -> {});
        }
        if (id == UPDATE_ID) {
            try {
                ItemStack item = buffer.readItemStack();
                slot.setConfig(AEItemStack.fromItemStack(item));
                this.parentWidget.enableAmount(this.index);
                if (!item.isEmpty()) {
                    writeUpdateInfo(UPDATE_ID, buf -> buf.writeItemStack(item));
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        IConfigurableSlot<IAEItemStack> slot = this.parentWidget.getDisplay(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
        }
        if (id == UPDATE_ID) {
            try {
                ItemStack item = buffer.readItemStack();
                slot.setConfig(AEItemStack.fromItemStack(item));
            } catch (IOException ignored) {
            }
        }
    }

}
