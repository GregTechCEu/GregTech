package gregtech.api.gui.widgets;

import gregtech.api.GTValues;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class IndicatorImageWidget extends Widget {

    protected TextureArea normalTexture;
    protected TextureArea warningTexture;
    protected TextureArea errorTexture;

    protected Supplier<List<String>> warningTextSupplier;
    protected Supplier<List<String>> errorTextSupplier;

    public IndicatorImageWidget(int xPosition, int yPosition, int width, int height,
                                TextureArea normalTexture) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.normalTexture = normalTexture;
    }

    /** Widget displays warning status if the supplied List is nonnull and not empty */
    public IndicatorImageWidget setWarningStatus(TextureArea texture, Supplier<List<String>> warningTextSupplier) {
        this.warningTexture = texture;
        this.warningTextSupplier = warningTextSupplier;
        return this;
    }

    /** Widget displays errored status if the supplied List is nonnull and not empty. Prioritized over warning. */
    public IndicatorImageWidget setErrorStatus(TextureArea texture, Supplier<List<String>> errorTextSupplier) {
        this.errorTexture = texture;
        this.errorTextSupplier = errorTextSupplier;
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        // every second, alternate which portion of the texture to draw
        double drawnV = GTValues.CLIENT_TIME / 20 % 2 == 0 ? 0 : 0.5;
        List<String> displayText = errorTextSupplier.get();
        if (displayText != null && !displayText.isEmpty()) {
            errorTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, drawnV, 1.0, 0.5);
        } else {
            displayText = warningTextSupplier.get();
            if (displayText != null && !displayText.isEmpty()) {
                warningTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, drawnV, 1.0, 0.5);
            } else {
                normalTexture.draw(pos.x, pos.y, size.width, size.height);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            List<String> displayText = errorTextSupplier.get();
            if (displayText == null || displayText.isEmpty()) {
                displayText = warningTextSupplier.get();
            }
            if (displayText != null && !displayText.isEmpty()) {
                List<String> hoverList = new ArrayList<>();
                for (String s : displayText) {
                    Collections.addAll(hoverList, LocalizationUtils.formatLines(s));
                }
                if (!hoverList.isEmpty()) {
                    drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
                }
            }
        }
    }
}
