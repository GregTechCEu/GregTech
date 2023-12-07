package gregtech.api.gui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingStationInputWidgetGroup extends AbstractWidgetGroup {

    protected CraftingRecipeLogic recipeResolver;
    protected short tintLocations;
    public static final int LIGHT_RED = 0x66FF0000;

    public CraftingStationInputWidgetGroup(int x, int y, ItemStackHandler craftingGrid,
                                           CraftingRecipeLogic recipeResolver) {
        super(new Position(x, y));

        // crafting grid
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addWidget(new PhantomSlotWidget(craftingGrid, j + i * 3, x + j * 18, y + i * 18)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        this.recipeResolver = recipeResolver;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        if (this.widgets.size() == 9) { // In case someone added more...
            for (int i = 0; i < 9; i++) {
                Widget widget = widgets.get(i);
                if (widget instanceof PhantomSlotWidget && ((tintLocations >> i) & 1) == 0) { // In other words, is this
                                                                                              // slot usable?
                    int color = LIGHT_RED;

                    PhantomSlotWidget phantomSlotWidget = (PhantomSlotWidget) widget;
                    drawSolidRect(phantomSlotWidget.getPosition().x + 1, phantomSlotWidget.getPosition().y + 1,
                            phantomSlotWidget.getSize().getWidth() - 2, phantomSlotWidget.getSize().getWidth() - 2,
                            color);
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        short newTintLocations = recipeResolver.getTintLocations();
        if (tintLocations != newTintLocations) {
            this.tintLocations = newTintLocations;
            writeUpdateInfo(2, buffer -> buffer.writeShort(tintLocations));
        }
    }

    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            tintLocations = buffer.readShort();
        }
    }
}
