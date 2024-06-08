package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * Represents a text-component based widget, which obtains
 * text from server and automatically synchronizes it with clients
 */
public class RecolorableTextWidget extends AdvancedTextWidget {

    private final IntSupplier colorSupplier;
    private int color;

    public RecolorableTextWidget(int xPosition, int yPosition, Consumer<List<ITextComponent>> text,
                                 IntSupplier colorSupplier) {
        super(xPosition, yPosition, text, 0);
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int color = colorSupplier.getAsInt();
        if (this.color != color) {
            this.color = color;
            writeUpdateInfo(2, buf -> buf.writeVarInt(color));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            color = buffer.readVarInt();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = getPosition();
        for (int i = 0; i < displayText.size(); i++) {
            fontRenderer.drawString(displayText.get(i).getFormattedText(), position.x,
                    position.y + i * (fontRenderer.FONT_HEIGHT + 2), color);
        }
    }
}
