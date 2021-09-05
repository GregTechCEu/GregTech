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
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Interpolator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TerminalDialogWidget extends AnimaWidgetGroup {
    private static final IGuiTexture DIALOG_BACKGROUND = TextureArea.fullImage("textures/gui/terminal/terminal_dialog.png");
    private static final IGuiTexture OK_NORMAL = TextureArea.fullImage("textures/gui/terminal/icon/ok_normal.png");
    private static final IGuiTexture OK_HOVER = TextureArea.fullImage("textures/gui/terminal/icon/ok_hover.png");
    private static final IGuiTexture OK_DISABLE = TextureArea.fullImage("textures/gui/terminal/icon/ok_disable.png");
    private static final IGuiTexture CANCEL_NORMAL = TextureArea.fullImage("textures/gui/terminal/icon/cancel_normal.png");
    private static final IGuiTexture CANCEL_HOVER = TextureArea.fullImage("textures/gui/terminal/icon/cancel_hover.png");
    private static final IGuiTexture CANCEL_DISABLE = TextureArea.fullImage("textures/gui/terminal/icon/cancel_disable.png");
    private static final int HEIGHT = 128;
    private static final int WIDTH = 184;

    protected Interpolator interpolator;
    private final TerminalOSWidget os;
    private IGuiTexture background;
    private boolean isClient;

    private TerminalDialogWidget(TerminalOSWidget os, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.os = os;
    }

    public boolean isClient() {
        return isClient;
    }

    public void open(){
        os.openDialog(this);
    }

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
                    os.closeDialog(this);
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
                    os.closeDialog(this);
                    if (result != null)
                        result.accept(true);
                })
                .setColors(0, 0, 0)
                .setIcon(OK_NORMAL)
                .setHoverIcon(OK_HOVER));
        addWidget(new CircleButtonWidget(WIDTH / 2 + 30, HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    os.closeDialog(this);
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
        this.addWidget(new LabelWidget(WIDTH / 2, HEIGHT / 2, info, -1).setWidth(WIDTH - 16).setYCentered(true).setXCentered(true));
        return this;
    }

    //todo unfinished
    public TerminalDialogWidget addPlayerInventory() {
        IInventory inventoryPlayer = os.getModularUI().entityPlayer.inventory;
        int x = 20;
        int y = 20;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addWidget(new SlotWidget(inventoryPlayer, col + (row + 1) * 9, x + col * 18, y + row * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setLocationInfo(true, false));
            }
        }
        y+=58;
        for (int slot = 0; slot < 9; slot++) {
            this.addWidget(new SlotWidget(inventoryPlayer, slot, x + slot * 18, y, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setLocationInfo(true, true));
        }
        return this;
    }

    public static TerminalDialogWidget createEmptyTemplate(TerminalOSWidget os) {
        Size size = os.getSize();
        return new TerminalDialogWidget(os, (size.width - WIDTH) / 2, (size.height - HEIGHT) / 2, WIDTH, HEIGHT).setBackground(DIALOG_BACKGROUND);
    }

    public static TerminalDialogWidget showInfoDialog(TerminalOSWidget os, String title, String info, Runnable callback) {
        return createEmptyTemplate(os).addTitle(title).addInfo(info).addOkButton(callback);
    }

    public static TerminalDialogWidget showInfoDialog(TerminalOSWidget os, String title, String info) {
        return createEmptyTemplate(os).addTitle(title).addInfo(info).addOkButton(null);
    }

    public static TerminalDialogWidget showConfirmDialog(TerminalOSWidget os, String title, String info, Consumer<Boolean> result) {
        return createEmptyTemplate(os).addConfirmButton(result).addTitle(title).addInfo(info);
    }

    public static TerminalDialogWidget showTextFieldDialog(TerminalOSWidget os, String title, Predicate<String> validator, Consumer<String> result) {
        TextFieldWidget textFieldWidget = new TextFieldWidget(WIDTH / 2 - 50, HEIGHT / 2 - 15, 100, 20, new ColorRectTexture(0x2fffffff), null, null).setValidator(validator);
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

    public static TerminalDialogWidget showColorDialog(TerminalOSWidget os, String title, Consumer<Integer> result) {
        TerminalDialogWidget dialog = createEmptyTemplate(os).addTitle(title);
        ColorWidget colorWidget = new ColorWidget(WIDTH / 2 - 60, HEIGHT / 2 - 35, 80, 10);
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

    public static TerminalDialogWidget showFileDialog(TerminalOSWidget os, String title, File dir, boolean isSelector, Consumer<File> result) {
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
        dialog.addWidget(new TreeListWidget<>(0, 0, 130, size.height, new FileTree(dir), node -> selected.set(node.getKey())).setNodeTexture(GuiTextures.BORDERED_BACKGROUND)
                .canSelectNode(true)
                .setLeafTexture(GuiTextures.SLOT_DARKENED));
        int x = 130 + (size.width - 133 - WIDTH) / 2;
        int y = (size.height - HEIGHT) / 2;
        dialog.addWidget(new ImageWidget(x, y, WIDTH, HEIGHT, DIALOG_BACKGROUND));
        dialog.addWidget(new CircleButtonWidget(x + WIDTH / 2 - 30, y + HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    os.closeDialog(dialog);
                    if (result != null)
                        result.accept(selected.get());
                })
                .setColors(0, 0, 0)
                .setIcon(OK_NORMAL)
                .setHoverIcon(OK_HOVER));
        dialog.addWidget(new CircleButtonWidget(x + WIDTH / 2 + 30, y + HEIGHT - 22, 12, 0, 24)
                .setClickListener(cd -> {
                    os.closeDialog(dialog);
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
            dialog.addWidget(new TextFieldWidget(x + WIDTH / 2 - 38, y + HEIGHT / 2 - 10, 76, 20, new ColorRectTexture(0x4f000000), null, null)
                    .setTextResponder(res->{
                        File file = selected.get();
                        if (file == null) return;
                        if (file.isDirectory()) {
                            selected.set(new File(file, res));
                        } else {
                            selected.set(new File(file.getParent(), res));
                        }
                    },true)
                    .setTextSupplier(()->{
                        File file = selected.get();
                        if (file != null && !file.isDirectory()) {
                            return selected.get().getName();
                        }
                        return "";
                    }, true)
                    .setMaxStringLength(Integer.MAX_VALUE)
                    .setValidator(s->true));
        }
        dialog.addWidget(new CircleButtonWidget(x + 17, y + 15, 10, 1, 16)
                .setClickListener(cd -> {
                    File file = selected.get();
                    if (file != null) {
                        try {
                            Desktop.getDesktop().open(file.isDirectory() ? file : file.getParentFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setColors(0, 0xFFFFFFFF, 0)
                .setHoverText("terminal.dialog.folder")
                .setIcon(GuiTextures.ICON_LOAD));
        dialog.addWidget(new LabelWidget(x + WIDTH / 2, y + 11, title, -1).setXCentered(true));
        os.menu.hideMenu();
        return dialog.setClientSide();
    }

    @Override
    public void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        GlStateManager.translate(0,0,1000);
        if (background != null) {
            background.draw(getPosition().x, getPosition().y, getSize().width, getSize().height);
        }
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
        GlStateManager.translate(0,0,-1000);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.isVisible()) {
                if (widget instanceof SlotWidget) {
                    return false;
                } else if(widget.mouseClicked(mouseX, mouseY, button)){
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (isClient) return;
        super.writeClientAction(id, packetBufferWriter);
    }
}
