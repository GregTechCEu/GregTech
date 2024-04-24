package gregtech.common.mui.widget.workbench;

import gregtech.api.mui.GTGuiTextures;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("DataFlowIssue")
public class RecipeMemorySlot extends Widget<RecipeMemorySlot> implements Interactable {

    private final CraftingRecipeMemory memory;
    private final int index;
    private final RecipeSyncHandler syncHandler;

    public RecipeMemorySlot(CraftingRecipeMemory memory, int index, GuiSyncManager syncManager) {
        this.memory = memory;
        this.index = index;
        this.syncHandler = new RecipeSyncHandler(this.memory, this.index);
        // setSyncHandler(this.syncHandler);
        syncManager.syncValue("recipe_memory", this.index, this.syncHandler);
    }

    @Override
    public void onInit() {
        size(ItemSlot.SIZE);
        background(GTGuiTextures.SLOT);
    }

    @Override
    public void afterInit() {
        this.syncHandler.syncToServer(1);
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        drawStack();
    }

    public void drawStack() {
        GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
        ItemStack itemstack = this.syncHandler.drawableStack;
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
        this.syncHandler.syncToServer(2, data::writeToPacket);
        return Result.ACCEPT;
    }

    private static class RecipeSyncHandler extends SyncHandler {

        public ItemStack drawableStack;
        private final CraftingRecipeMemory memory;
        private final int index;

        public RecipeSyncHandler(CraftingRecipeMemory memory, int index) {
            this.memory = memory;
            this.index = index;
            this.drawableStack = this.memory.getRecipeOutputAtIndex(this.index);
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == 1) {
                this.drawableStack = readStackSafe(buf);
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == 1) {
                this.syncToClient(1, buffer -> buffer.writeItemStack(this.drawableStack));
            } else if (id == 2) {
                // read mouse data
                var data = MouseData.readPacket(buf);
                if (data.shift && data.mouseButton == 0 && memory.hasRecipe(index)) {
                    var recipe = memory.getRecipeAtIndex(index);
                    recipe.setRecipeLocked(!recipe.isRecipeLocked());
                } else if (data.mouseButton == 0) {
                    memory.loadRecipe(index);
                } else if (data.mouseButton == 1) {
                    if (memory.hasRecipe(index) && !memory.getRecipeAtIndex(index).isRecipeLocked())
                        memory.removeRecipe(index);
                }
            }
        }

        private ItemStack readStackSafe(PacketBuffer buffer) {
            ItemStack ret = ItemStack.EMPTY;
            try {
                ret = buffer.readItemStack();
            } catch (IOException ignored) {}
            return ret;
        }
    }
}
