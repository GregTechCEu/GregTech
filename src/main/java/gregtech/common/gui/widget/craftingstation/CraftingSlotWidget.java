package gregtech.common.gui.widget.craftingstation;

import com.google.common.base.Preconditions;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.ingredient.IRecipeTransferHandlerWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CraftingSlotWidget extends SlotWidget implements IRecipeTransferHandlerWidget {

    private final CraftingRecipeLogic recipeResolver;
    private boolean canTakeStack = false;

    public CraftingSlotWidget(CraftingRecipeLogic recipeResolver, int slotIndex, int xPosition, int yPosition) {
        super(createInventory(recipeResolver), slotIndex, xPosition, yPosition, true, false);
        this.recipeResolver = recipeResolver;
    }

    private static IInventory createInventory(CraftingRecipeLogic resolver) {
        return resolver == null ? new InventoryCraftResult() : resolver.getCraftingResultInventory();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            HashMap<Integer, ItemStack> ingredients = new HashMap<>();
            int ingredientAmount = buffer.readVarInt();
            try {
                for (int i = 0; i < ingredientAmount; i++) {
                    ingredients.put(buffer.readVarInt(), buffer.readItemStack());
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            recipeResolver.fillCraftingGrid(ingredients);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (recipeResolver == null) {
            return;
        }
        boolean isRecipeValid = recipeResolver.isRecipeValid();
        if (isRecipeValid != canTakeStack) {
            this.canTakeStack = isRecipeValid;
            writeUpdateInfo(1, buf -> buf.writeBoolean(canTakeStack));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.canTakeStack = buffer.readBoolean();
        }
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        if (!super.canTakeStack(player)) {
            return false;
        }
        if (recipeResolver == null) {
            return canTakeStack;
        }
        return recipeResolver.isRecipeValid();
    }

    @Override
    public ItemStack onItemTake(EntityPlayer thePlayer, ItemStack stack, boolean simulate) {
        if (recipeResolver == null) {
            return ItemStack.EMPTY;
        }
        recipeResolver.handleItemCraft(stack, thePlayer, simulate);
        if (simulate) {
            return stack;
        }
        recipeResolver.performRecipe(thePlayer);
        recipeResolver.refreshOutputSlot();
        //send slot changes now, both of consumed items in inventory and result slot
        gui.entityPlayer.openContainer.detectAndSendChanges();
        uiAccess.sendSlotUpdate(this);
        return stack;
    }

    @Override
    public String transferRecipe(ModularUIContainer container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }
        Map<Integer, IGuiIngredient<ItemStack>> ingredients = new HashMap<>(recipeLayout.getItemStacks().getGuiIngredients());
        ingredients.values().removeIf(it -> it.getAllIngredients().isEmpty() || !it.isInput());
        writeClientAction(1, buf -> {
            buf.writeVarInt(ingredients.size());
            for (Entry<Integer, IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
                buf.writeVarInt(entry.getKey());
                ItemStack itemStack = entry.getValue().getDisplayedIngredient();
                Preconditions.checkNotNull(itemStack);
                buf.writeItemStack(itemStack);
            }
        });
        return null;
    }
}
