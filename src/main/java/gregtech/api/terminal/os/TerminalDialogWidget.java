package gregtech.api.terminal.os;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.terminal.gui.widgets.AnimaWidgetGroup;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.ColorWidget;
import gregtech.api.terminal.gui.widgets.TreeListWidget;
import gregtech.api.terminal.util.FileTree;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TerminalDialogWidget extends AnimaWidgetGroup {

    private static final IGuiTexture DIALOG_BACKGROUND = TextureArea
            .fullImage("textures/gui/terminal/terminal_dialog.png");
    private static final IGuiTexture OK_NORMAL = TextureArea.fullImage("textures/gui/terminal/icon/ok_normal.png");
    private static final IGuiTexture OK_HOVER = TextureArea.fullImage("textures/gui/terminal/icon/ok_hover.png");
    // private static final IGuiTexture OK_DISABLE = TextureArea.fullImage("textures/gui/terminal/icon/ok_disable.png");
    private static final IGuiTexture CANCEL_NORMAL = TextureArea
            .fullImage("textures/gui/terminal/icon/cancel_normal.png");
    private static final IGuiTexture CANCEL_HOVER = TextureArea
            .fullImage("textures/gui/terminal/icon/cancel_hover.png");
    // private static final IGuiTexture CANCEL_DISABLE =
    // TextureArea.fullImage("textures/gui/terminal/icon/cancel_disable.png");
    private static final int HEIGHT = 128;
    private static final int WIDTH = 184;

    private final TerminalOSWidget os;
    private IGuiTexture background;
    private boolean isClient;
    private List<Widget> iNativeWidgets;
    boolean isClosed;

    public TerminalDialogWidget(TerminalOSWidget os, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.os = os;
    }

    public boolean isClient() {
        return isClient;
    }

    public TerminalDialogWidget open() {
        os.openDialog(this);
        if (iNativeWidgets != null) {
            iNativeWidgets.forEach(this::addWidget);
        }
        if (isRemote()) {
            os.desktop.addTopWidget(this);
        }
        return this;
    }

    public void close() {
        if (isClosed) return;
        isClosed = true;
        os.closeDialog(this);
        if (isRemote()) {
            os.desktop.removeTopWidget(this);
        }
    }

    /**
     * Should be a client side dialog.
     * This is very important, please make sure the right side.
     */
    public TerminalDialogWidget setClientSide() {
        this.isClient = true;
        return this;
    }

    public TerminalDialogWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public TerminalDialogWidget addOkButton(Runnable callback) {
        addWidget(new CircleButtonWidget(WIDTH / 2, HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    close();
                    if (callback != null)
                        callback.run();
                })
                .setColors(0, 0, 0)
                .setIcon(OK_NORMAL)
                .setHoverIcon(OK_HOVER));
        return this;
    }

    public TerminalDialogWidget addConfirmButton(Consumer<Boolean> result) {
        addWidget(new CircleButtonWidget(WIDTH / 2 - 30, HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    close();
                    if (result != null)
                        result.accept(true);
                })
                .setColors(0, 0, 0)
                .setIcon(OK_NORMAL)
                .setHoverIcon(OK_HOVER));
        addWidget(new CircleButtonWidget(WIDTH / 2 + 30, HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    close();
                    if (result != null)
                        result.accept(false);
                })
                .setColors(0, 0, 0)
                .setIcon(CANCEL_NORMAL)
                .setHoverIcon(CANCEL_HOVER));
        return this;
    }

    public TerminalDialogWidget addTitle(String title) {
        this.addWidget(new LabelWidget(WIDTH / 2, 11, title, -1).setXCentered(true));
        return this;
    }

    public TerminalDialogWidget addInfo(String info) {
        this.addWidget(new LabelWidget(WIDTH / 2, HEIGHT / 2, info, -1).setWidth(WIDTH - 16).setYCentered(true)
                .setXCentered(true));
        return this;
    }

    public static TerminalDialogWidget createEmptyTemplate(TerminalOSWidget os) {
        Size size = os.getSize();
        return new TerminalDialogWidget(os, (size.width - WIDTH) / 2, (size.height - HEIGHT) / 2, WIDTH, HEIGHT)
                .setBackground(DIALOG_BACKGROUND);
    }

    public static TerminalDialogWidget showInfoDialog(TerminalOSWidget os, String title, String info,
                                                      Runnable callback) {
        return createEmptyTemplate(os).addTitle(title).addInfo(info).addOkButton(callback);
    }

    public static TerminalDialogWidget showInfoDialog(TerminalOSWidget os, String title, String info) {
        return createEmptyTemplate(os).addTitle(title).addInfo(info).addOkButton(null);
    }

    public static TerminalDialogWidget showConfirmDialog(TerminalOSWidget os, String title, String info,
                                                         Consumer<Boolean> result) {
        return createEmptyTemplate(os).addConfirmButton(result).addTitle(title).addInfo(info);
    }

    public static TerminalDialogWidget showTextFieldDialog(TerminalOSWidget os, String title,
                                                           Predicate<String> validator, Consumer<String> result) {
        TextFieldWidget textFieldWidget = new TextFieldWidget(WIDTH / 2 - 50, HEIGHT / 2 - 15, 100, 20,
                new ColorRectTexture(0x2fffffff), null, null).setValidator(validator);
        TerminalDialogWidget dialog = createEmptyTemplate(os).addTitle(title).addConfirmButton(b -> {
            if (b) {
                if (result != null)
                    result.accept(textFieldWidget.getCurrentString());
            } else {
                if (result != null)
                    result.accept(null);
            }
        });
        dialog.addWidget(textFieldWidget);
        return dialog;
    }

    /**
     * Show Color Dialog
     * 
     * @return color (rgba)
     */
    public static TerminalDialogWidget showColorDialog(TerminalOSWidget os, String title, Consumer<Integer> result,
                                                       int startColor) {
        TerminalDialogWidget dialog = createEmptyTemplate(os).addTitle(title);
        ColorWidget colorWidget = new ColorWidget(WIDTH / 2 - 60, HEIGHT / 2 - 35, 80, 10);
        colorWidget.setStartColor(startColor);
        dialog.addWidget(colorWidget);
        dialog.addConfirmButton(b -> {
            if (b) {
                if (result != null)
                    result.accept(colorWidget.getColor());
            } else {
                if (result != null)
                    result.accept(null);
            }
        });
        return dialog;
    }

    /**
     * Show FileDialog
     * 
     * @param dir        root directory
     * @param isSelector select a file or save a file
     * @param result     selected file or (saved)
     */
    public static TerminalDialogWidget showFileDialog(TerminalOSWidget os, String title, File dir, boolean isSelector,
                                                      Consumer<File> result) {
        Size size = os.getSize();
        TerminalDialogWidget dialog = new TerminalDialogWidget(os, 0, 0, size.width, size.height)
                .setBackground(new ColorRectTexture(0x4f000000));
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                return dialog.addInfo(I18n.format("terminal.dialog.error_path") + dir.getPath()).addOkButton(null);
            }
        }
        AtomicReference<File> selected = new AtomicReference<>();
        selected.set(dir);
        dialog.addWidget(
                new TreeListWidget<>(0, 0, 130, size.height, new FileTree(dir), node -> selected.set(node.getKey()))
                        .setNodeTexture(GuiTextures.BORDERED_BACKGROUND)
                        .canSelectNode(true)
                        .setLeafTexture(GuiTextures.SLOT_DARKENED));
        int x = 130 + (size.width - 133 - WIDTH) / 2;
        int y = (size.height - HEIGHT) / 2;
        dialog.addWidget(new ImageWidget(x, y, WIDTH, HEIGHT, DIALOG_BACKGROUND));
        dialog.addWidget(new CircleButtonWidget(x + WIDTH / 2 - 30, y + HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    dialog.close();
                    if (result != null)
                        result.accept(selected.get());
                })
                .setColors(0, 0, 0)
                .setIcon(OK_NORMAL)
                .setHoverIcon(OK_HOVER));
        dialog.addWidget(new CircleButtonWidget(x + WIDTH / 2 + 30, y + HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    dialog.close();
                    if (result != null)
                        result.accept(null);
                })
                .setColors(0, 0, 0)
                .setIcon(CANCEL_NORMAL)
                .setHoverIcon(CANCEL_HOVER));
        if (isSelector) {
            dialog.addWidget(new SimpleTextWidget(x + WIDTH / 2, y + HEIGHT / 2 - 5, "", -1, () -> {
                if (selected.get() != null) {
                    return selected.get().toString();
                }
                return "terminal.dialog.no_file_selected";
            }, true).setWidth(WIDTH - 16));
        } else {
            dialog.addWidget(new TextFieldWidget(x + WIDTH / 2 - 38, y + HEIGHT / 2 - 10, 76, 20,
                    new ColorRectTexture(0x4f000000), null, null)
                            .setTextResponder(res -> {
                                File file = selected.get();
                                if (file == null) return;
                                if (file.isDirectory()) {
                                    selected.set(new File(file, res));
                                } else {
                                    selected.set(new File(file.getParent(), res));
                                }
                            }, true)
                            .setTextSupplier(() -> {
                                File file = selected.get();
                                if (file != null && !file.isDirectory()) {
                                    return selected.get().getName();
                                }
                                return "";
                            }, true)
                            .setMaxStringLength(Integer.MAX_VALUE)
                            .setValidator(s -> true));
        }
        dialog.addWidget(new CircleButtonWidget(x + 17, y + 15, 10, 1, 16)
                .setClickListener(cd -> {
                    File file = selected.get();
                    if (file != null && Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(file.isDirectory() ? file : file.getParentFile());
                        } catch (IOException e) {
                            GTLog.logger.error("Error reading opening file " + file.getPath(), e);
                        }
                    }
                })
                .setColors(0, 0xFFFFFFFF, 0)
                .setHoverText("terminal.dialog.folder")
                .setIcon(GuiTextures.ICON_LOAD));
        dialog.addWidget(new LabelWidget(x + WIDTH / 2, y + 11, title, -1).setXCentered(true));
        if (os.isRemote()) {
            os.menu.hideMenu();
        }
        return dialog.setClientSide();
    }

    public static TerminalDialogWidget showItemSelector(TerminalOSWidget os, String title, boolean cost,
                                                        Predicate<ItemStack> filter, Consumer<ItemStack> result) {
        TerminalDialogWidget dialog = createEmptyTemplate(os);
        dialog.addWidget(new LabelWidget(WIDTH / 2, -7, title, -1).setShadow(true).setXCentered(true));
        IInventory inventoryPlayer = os.getModularUI().entityPlayer.inventory;
        if (dialog.iNativeWidgets == null) {
            dialog.iNativeWidgets = new ArrayList<>();
        }
        int x = 11;
        int y = 30;
        final SlotWidget[] selected = { null };
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                boolean pass = filter == null || filter.test(inventoryPlayer.getStackInSlot(col + row * 9));
                SlotWidget slotWidget = new SlotWidget(inventoryPlayer, col + row * 9, x + col * 18,
                        (int) (y + (row == 0 ? -1.2 : (row - 1)) * 18), false, false) {

                    @Override
                    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
                        super.drawInBackground(mouseX, mouseY, partialTicks, context);
                        if (selected[0] == this) {
                            drawBorder(getPosition().x, getPosition().y, getSize().width, getSize().height, -1, 1);
                        }
                    }

                    @Override
                    public boolean mouseClicked(int mouseX, int mouseY, int button) {
                        if (pass && isMouseOverElement(mouseX, mouseY)) {
                            if (selected[0] == this) {
                                selected[0] = null;
                            } else {
                                selected[0] = this;
                            }
                            writeClientAction(7, buffer -> buffer.writeBoolean(selected[0] == this));
                        }
                        return super.mouseClicked(mouseX, mouseY, button);
                    }

                    @Override
                    public void handleClientAction(int id, PacketBuffer buffer) {
                        if (id == 7) {
                            if (buffer.readBoolean()) {
                                selected[0] = this;
                            } else {
                                selected[0] = null;
                            }
                        }
                        super.readUpdateInfo(id, buffer);
                    }
                }.setBackgroundTexture(TerminalTheme.COLOR_B_1).setLocationInfo(true, false);
                slotWidget.setActive(pass);
                dialog.iNativeWidgets.add(slotWidget);
            }
        }
        dialog.addConfirmButton(confirm -> {
            if (result != null && confirm && selected[0] != null && !selected[0].getHandle().getStack().isEmpty()) {
                ItemStack stack = selected[0].getHandle().getStack().copy();
                if (cost) {
                    selected[0].getHandle().getStack().setCount(stack.getCount() - 1);
                }
                stack.setCount(1);
                result.accept(stack);
            }
        });
        return dialog;
    }

    @Override
    public void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        GlStateManager.disableDepth();
        if (background != null) {
            background.draw(getPosition().x, getPosition().y, getSize().width, getSize().height);
        }
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
        GlStateManager.enableDepth();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.isVisible()) {
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (isClient || isClosed) return;
        super.writeClientAction(id, packetBufferWriter);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) { // esc close
            if (buffer.readBoolean()) {
                close();
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public boolean keyTyped(char charTyped, int keyCode) {
        if (keyCode == 1 && super.interpolator == null) {
            writeClientAction(-1, buffer -> buffer.writeBoolean(true));
            close();
            return true;
        }
        return super.keyTyped(charTyped, keyCode);
    }

    public void onOSSizeUpdate(int width, int height) {
        setSelfPosition(
                Position.ORIGIN.add(new Position((width - getSize().width) / 2, (height - getSize().height) / 2)));
    }
}
