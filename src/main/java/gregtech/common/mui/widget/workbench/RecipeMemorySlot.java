package gregtech.common.mui.widget.workbench;

import gregtech.api.mui.GTGuiTextures;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("DataFlowIssue")
public class RecipeMemorySlot extends Widget<RecipeMemorySlot> implements Interactable {

    private final CraftingRecipeMemory memory;
    private final int index;

    public RecipeMemorySlot(CraftingRecipeMemory memory, int index) {
        this.memory = memory;
        this.index = index;
    }

    @Override
    public void onInit() {
        size(ItemSlot.SIZE);
        background(GTGuiTextures.SLOT);
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        drawStack();
    }

    public void drawStack() {
        GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
        ItemStack itemstack = this.memory.getRecipeOutputAtIndex(this.index);
        if (itemstack.isEmpty()) return;

        guiScreen.setZ(100f);
        guiScreen.getItemRenderer().zLevel = 100.0F;

        // GuiDraw.drawRect(1, 1, 16, 16, -2130706433);

        GlStateManager.enableDepth();
        // render the item itself
        guiScreen.getItemRenderer().renderItemAndEffectIntoGUI(guiScreen.mc.player, itemstack, 1, 1);

        // render the amount overlay
        // String amountText = NumberFormat.formatWithMaxDigits(1);
        // textRenderer.setShadow(true);
        // textRenderer.setColor(Color.WHITE.main);
        // textRenderer.setAlignment(Alignment.BottomRight, getArea().width - 1, getArea().height - 1);
        // textRenderer.setPos(1, 1);
        // GlStateManager.disableLighting();
        // GlStateManager.disableDepth();
        // GlStateManager.disableBlend();
        // textRenderer.draw(amountText);
        // GlStateManager.enableLighting();
        // GlStateManager.enableDepth();
        // GlStateManager.enableBlend();

        int cachedCount = itemstack.getCount();
        itemstack.setCount(1); // required to not render the amount overlay
        // render other overlays like durability bar
        guiScreen.getItemRenderer().renderItemOverlayIntoGUI(guiScreen.getFontRenderer(), itemstack, 1, 1,
                null);
        itemstack.setCount(cachedCount);
        GlStateManager.disableDepth();

        guiScreen.getItemRenderer().zLevel = 0.0F;
        guiScreen.setZ(0f);
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        var data = MouseData.create(mouseButton);
        this.memory.syncToServer(2, buffer -> {
            buffer.writeByte(this.index);
            data.writeToPacket(buffer);
        });
        return Result.ACCEPT;
    }
}
