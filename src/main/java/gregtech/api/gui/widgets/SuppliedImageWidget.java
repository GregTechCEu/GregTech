package gregtech.api.gui.widgets;

import gregtech.api.gui.resources.IGuiTexture;

import java.util.function.Supplier;

public class SuppliedImageWidget extends ImageWidget {

    private final Supplier<IGuiTexture> areaSupplier;

    public SuppliedImageWidget(int xPosition, int yPosition, int width, int height,
                               Supplier<IGuiTexture> areaSupplier) {
        super(xPosition, yPosition, width, height);
        this.areaSupplier = areaSupplier;
    }

    @Override
    protected IGuiTexture getArea() {
        return areaSupplier.get();
    }
}
