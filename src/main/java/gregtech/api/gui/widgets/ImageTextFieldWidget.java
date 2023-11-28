package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.SizedTextureArea;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ImageTextFieldWidget extends TextFieldWidget {

    SizedTextureArea textureArea;

    public ImageTextFieldWidget(int xPosition, int yPosition, int width, int height, SizedTextureArea textureArea,
                                Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(xPosition, yPosition, width, height, false, textSupplier, textResponder);
        this.textureArea = textureArea;
    }

    public ImageTextFieldWidget(int xPosition, int yPosition, int width, int height, SizedTextureArea textureArea,
                                Supplier<String> textSupplier, Consumer<String> textResponder, int maxStringLength) {
        super(xPosition, yPosition, width, height, false, textSupplier, textResponder, maxStringLength);
        this.textureArea = textureArea;
    }

    public ImageTextFieldWidget(int xPosition, int yPosition, int width, int height, SizedTextureArea textureArea,
                                Supplier<String> textSupplier, Consumer<String> textResponder, int maxStringLength,
                                int color) {
        this(xPosition, yPosition, width, height, textureArea, textSupplier, textResponder, maxStringLength);
        if (isClientSide())
            this.textField.setTextColor(color);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        this.textureArea.drawHorizontalCutArea(this.getPosition().x - 2, this.getPosition().y, this.getSize().width,
                this.getSize().height);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
