package gregtech.api.gui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.common.metatileentities.storage.CraftingRecipeResolver;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingStationInputWidgetGroup extends AbstractWidgetGroup {
    protected CraftingRecipeResolver recipeResolver;
    protected short tintLocations;

    public CraftingStationInputWidgetGroup(int x, int y, ItemStackHandler craftingGrid, CraftingRecipeResolver recipeResolver) {
        super(new Position(x, y));

        //crafting grid
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addWidget(new PhantomSlotWidget(craftingGrid, j + i * 3, x + j * 18, y + i * 18).setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        this.recipeResolver = recipeResolver;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, context);
        if(this.widgets.size() == 9) { // In case someone added more...
            for (int i = 0; i < 9; i++) {
                Widget widget = widgets.get(i);
                if (widget instanceof PhantomSlotWidget && ((tintLocations >> i) & 1) == 0) { // In other words, is this slot usable?
                    int color = 0x00005555;

                    PhantomSlotWidget phantomSlotWidget = (PhantomSlotWidget) widget;
                    drawSolidRect(phantomSlotWidget.getPosition().x, phantomSlotWidget.getPosition().y,
                            phantomSlotWidget.getSize().getWidth(), phantomSlotWidget.getSize().getWidth(), color);
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (recipeResolver.getCachedRecipeData().attemptMatchRecipe() != tintLocations) {
            this.tintLocations = recipeResolver.getCachedRecipeData().attemptMatchRecipe();
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
