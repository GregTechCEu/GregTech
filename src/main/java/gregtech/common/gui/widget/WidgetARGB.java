package gregtech.common.gui.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class WidgetARGB extends WidgetGroup {

    public int color;
    private final int height;

    public WidgetARGB(int x, int y, int height, int initColor, Consumer<Integer> onColorChanged) {
        super(new Position(x, y));
        this.color = initColor;
        this.height = height;
        Predicate<String> validator = (data) -> {
            if (data.isEmpty()) return true;
            try {
                int num = Integer.parseInt(data, 16);
                if (num > 255 || num < 0) return false;
            } catch (Exception e) {
                return false;
            }
            return true;
        };
        Consumer<PacketBuffer> update = (buf) -> {
            buf.writeInt(color);
        };
        TextFieldWidget[] ARGB = new TextFieldWidget[4];
        ARGB[0] = new TextFieldWidget(0, 0, 20, height, true,
                () -> Integer.toHexString(color >> 24 & 0XFF).toUpperCase(), (data) -> {
                    this.color = (data.isEmpty() ? 0 : Integer.parseInt(data, 16)) << 24 | (color & 0X00FFFFFF);
                    onColorChanged.accept(color);
                    writeUpdateInfo(2, update);
                }).setValidator(validator);
        ARGB[1] = new TextFieldWidget(22, 0, 20, height, true,
                () -> Integer.toHexString(color >> 16 & 0XFF).toUpperCase(), (data) -> {
                    this.color = (data.isEmpty() ? 0 : Integer.parseInt(data, 16)) << 16 | (color & 0XFF00FFFF);
                    onColorChanged.accept(color);
                    writeUpdateInfo(2, update);
                }).setValidator(validator);
        ARGB[2] = new TextFieldWidget(44, 0, 20, height, true,
                () -> Integer.toHexString(color >> 8 & 0XFF).toUpperCase(), (data) -> {
                    this.color = (data.isEmpty() ? 0 : Integer.parseInt(data, 16)) << 8 | (color & 0X00FF00FF);
                    onColorChanged.accept(color);
                    writeUpdateInfo(2, update);
                }).setValidator(validator);
        ARGB[3] = new TextFieldWidget(66, 0, 20, height, true, () -> Integer.toHexString(color & 0XFF).toUpperCase(),
                (data) -> {
                    this.color = (data.isEmpty() ? 0 : Integer.parseInt(data, 16)) | (color & 0XFFFFFF00);
                    onColorChanged.accept(color);
                    writeUpdateInfo(2, update);
                }).setValidator(validator);
        this.addWidget(ARGB[0]);
        this.addWidget(ARGB[1]);
        this.addWidget(ARGB[2]);
        this.addWidget(ARGB[3]);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            this.color = buffer.readInt();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Gui.drawRect(this.getPosition().x + 88, this.getPosition().y, this.getPosition().x + 100,
                this.getPosition().y + height, color);
    }
}
