package gregtech.api.gui.widgets;

import gregtech.api.GTValues;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class IndicatorImageWidget extends Widget {

    protected TextureArea normalTexture;
    protected TextureArea warningTexture;
    protected TextureArea errorTexture;

    protected Consumer<List<ITextComponent>> warningTextSupplier;
    private List<ITextComponent> warningText = new ArrayList<>();

    protected Consumer<List<ITextComponent>> errorTextSupplier;
    private List<ITextComponent> errorText = new ArrayList<>();

    public IndicatorImageWidget(int xPosition, int yPosition, int width, int height,
                                TextureArea normalTexture) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.normalTexture = normalTexture;
    }

    /** Widget displays warning status if the supplied List is nonnull and not empty */
    public IndicatorImageWidget setWarningStatus(TextureArea texture,
                                                 Consumer<List<ITextComponent>> warningTextSupplier) {
        this.warningTexture = texture;
        this.warningTextSupplier = warningTextSupplier;
        return this;
    }

    /** Widget displays errored status if the supplied List is nonnull and not empty. Prioritized over warning. */
    public IndicatorImageWidget setErrorStatus(TextureArea texture, Consumer<List<ITextComponent>> errorTextSupplier) {
        this.errorTexture = texture;
        this.errorTextSupplier = errorTextSupplier;
        return this;
    }

    @Override
    public void detectAndSendChanges() {
        List<ITextComponent> textBuffer = new ArrayList<>();
        warningTextSupplier.accept(textBuffer);
        if (!warningText.equals(textBuffer)) {
            this.warningText = textBuffer;
            writeUpdateInfo(1, buffer -> {
                buffer.writeVarInt(warningText.size());
                for (ITextComponent component : warningText) {
                    buffer.writeString(ITextComponent.Serializer.componentToJson(component));
                }
            });
        }
        textBuffer = new ArrayList<>();
        errorTextSupplier.accept(textBuffer);
        if (!errorText.equals(textBuffer)) {
            this.errorText = textBuffer;
            writeUpdateInfo(2, buffer -> {
                buffer.writeVarInt(errorText.size());
                for (ITextComponent component : errorText) {
                    buffer.writeString(ITextComponent.Serializer.componentToJson(component));
                }
            });
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.warningText.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                String jsonText = buffer.readString(32767);
                this.warningText.add(ITextComponent.Serializer.jsonToComponent(jsonText));
            }
        } else if (id == 2) {
            this.errorText.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                String jsonText = buffer.readString(32767);
                this.errorText.add(ITextComponent.Serializer.jsonToComponent(jsonText));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        // every second, alternate which portion of the texture to draw
        double drawnV = GTValues.CLIENT_TIME / 20 % 2 == 0 ? 0 : 0.5;
        if (errorText != null && !errorText.isEmpty()) {
            errorTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, drawnV, 1.0, 0.5);
        } else if (warningText != null && !warningText.isEmpty()) {
            warningTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, drawnV, 1.0, 0.5);
        } else {
            normalTexture.draw(pos.x, pos.y, size.width, size.height);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            List<ITextComponent> displayText = null;
            if (errorText != null && !errorText.isEmpty()) {
                displayText = errorText;
            } else if (warningText != null && !warningText.isEmpty()) {
                displayText = warningText;
            }
            if (displayText != null) {
                List<String> hoverList = new ArrayList<>();
                for (ITextComponent s : displayText) {
                    Collections.addAll(hoverList, s.getFormattedText());
                }
                if (!hoverList.isEmpty()) {
                    drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
                }
            }
        }
    }
}
