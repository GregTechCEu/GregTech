package gregtech.common.terminal.app.hardwaremanager;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.Arrays;
import java.util.Collections;

public class HardwareSlotWidget extends WidgetGroup {

    private final Hardware hardware;
    private final TerminalOSWidget os;
    private Runnable onSelected;

    public HardwareSlotWidget(int x, int y, TerminalOSWidget os, Hardware hardware) {
        super(x, y, 20, 20);
        this.os = os;
        this.hardware = hardware;
    }

    public void setOnSelected(Runnable onSelected) {
        this.onSelected = onSelected;
    }

    private void showDialog(int button) {
        if (button == 0) {
            if (hardware.hasHW()) {
                onSelected.run();
            } else {
                TerminalDialogWidget.showItemSelector(os, "terminal.hardware.select", true,
                        itemStack -> hardware.acceptItemStack(itemStack) != null,
                        itemStack -> {
                            NBTTagCompound tag = hardware.acceptItemStack(itemStack);
                            if (tag != null) {
                                tag.setTag("item", itemStack.serializeNBT());
                                os.hardwareProvider.getOrCreateHardwareCompound().setTag(hardware.getRegistryName(),
                                        tag);
                                os.hardwareProvider.cleanCache(hardware.getRegistryName());
                            }
                        }).open();
            }
        } else {
            if (hardware.hasHW()) {
                boolean emptySlot = false;
                for (ItemStack itemStack : gui.entityPlayer.inventory.mainInventory) {
                    if (itemStack.isEmpty()) {
                        emptySlot = true;
                        break;
                    }
                }
                if (emptySlot) {
                    TerminalDialogWidget
                            .showConfirmDialog(os, "terminal.hardware.remove", "terminal.component.confirm", result -> {
                                if (result) {
                                    NBTTagCompound tag = os.hardwareProvider.getOrCreateHardwareCompound()
                                            .getCompoundTag(hardware.getRegistryName());
                                    if (!tag.isEmpty() && tag.hasKey("item")) {
                                        gui.entityPlayer.inventory.addItemStackToInventory(
                                                hardware.onHardwareRemoved(new ItemStack(tag.getCompoundTag("item"))));
                                    }
                                    os.hardwareProvider.getOrCreateHardwareCompound()
                                            .removeTag(hardware.getRegistryName());
                                    os.hardwareProvider.cleanCache(hardware.getRegistryName());
                                }
                            }).open();
                } else {
                    TerminalDialogWidget
                            .showInfoDialog(os, "terminal.component.warning", "terminal.hardware.remove.full").open();
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            writeClientAction(1, buffer -> buffer.writeVarInt(button));
            showDialog(button);
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            showDialog(buffer.readVarInt());
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (hardware != null && isMouseOverElement(mouseX, mouseY)) {
            if (!hardware.hasHW()) {
                drawHoveringText(ItemStack.EMPTY, Collections.singletonList(hardware.getLocalizedName()), 300, mouseX,
                        mouseY);
            } else {
                String info = hardware.addInformation();
                if (info == null) {
                    drawHoveringText(ItemStack.EMPTY,
                            Arrays.asList(hardware.getLocalizedName(), I18n.format("terminal.hardware.tip.remove")),
                            300, mouseX, mouseY);
                } else {
                    drawHoveringText(ItemStack.EMPTY,
                            Arrays.asList(String.format("%s (%s)", hardware.getLocalizedName(), info),
                                    I18n.format("terminal.hardware.tip.remove")),
                            300, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        boolean missing = !hardware.hasHW();
        drawBorder(x - 1, y - 1, width + 2, height + 1, missing ? 0x6fffffff : -1, 1);
        hardware.getIcon().draw(x, y, width, height);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
