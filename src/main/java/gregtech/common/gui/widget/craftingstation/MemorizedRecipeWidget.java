package gregtech.common.gui.widget.craftingstation;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.util.Position;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory.MemorizedRecipe;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collections;
import java.util.List;

public class MemorizedRecipeWidget extends SlotWidget {

    private final CraftingRecipeMemory recipeMemory;
    private final int recipeIndex;
    private boolean recipeLocked = false;
    private final IItemHandlerModifiable craftingGrid;

    public MemorizedRecipeWidget(CraftingRecipeMemory recipeMemory, int index, IItemHandlerModifiable craftingGrid, int xPosition, int yPosition) {
        super(new ItemStackHandler(1), 0, xPosition, yPosition, true, false);
        this.recipeMemory = recipeMemory;
        this.recipeIndex = index;
        this.craftingGrid = craftingGrid;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        MemorizedRecipe recipe = recipeMemory.getRecipeAtIndex(recipeIndex);
        ItemStack resultStack = recipe == null ? ItemStack.EMPTY : recipe.getRecipeResult();
        if (!ItemStack.areItemStacksEqual(resultStack, slotReference.getStack())) {
            slotReference.putStack(resultStack);
            uiAccess.sendSlotUpdate(this);
        }
        boolean recipeLocked = recipe != null && recipe.isRecipeLocked();
        if (this.recipeLocked != recipeLocked) {
            this.recipeLocked = recipeLocked;
            writeUpdateInfo(1, buf -> buf.writeBoolean(recipeLocked));
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (recipeLocked) {
            Position pos = getPosition();
            GlStateManager.disableDepth();
            GuiTextures.LOCK.draw(pos.x, pos.y + 10, 8, 8);
            GlStateManager.enableDepth();
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            ((ISlotWidget) slotReference).setHover(false);
            List<String> tooltip = getItemToolTip(slotReference.inventory.getStackInSlot(0));
            tooltip.add(I18n.format("gregtech.recipe_memory_widget.tooltip.1"));
            tooltip.add(I18n.format("gregtech.recipe_memory_widget.tooltip.2"));
            drawHoveringText(slotReference.inventory.getStackInSlot(0), tooltip, -1, mouseX, mouseY);
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) this.recipeLocked = buffer.readBoolean();
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (!player.world.isRemote) {
            MemorizedRecipe recipe = recipeMemory.getRecipeAtIndex(recipeIndex);
            if (recipe != null && !recipe.getRecipeResult().isEmpty()) {
                if (clickTypeIn == ClickType.PICKUP) {
                    recipeMemory.loadRecipe(recipeIndex, craftingGrid);
                    player.openContainer.detectAndSendChanges();
                } else if (clickTypeIn == ClickType.QUICK_MOVE) {
                    recipe.setRecipeLocked(!recipe.isRecipeLocked());
                }
            }
        }
        return ItemStack.EMPTY;
    }

}
