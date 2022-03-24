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
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CraftingSlotWidget extends SlotWidget implements IRecipeTransferHandlerWidget {

    private final CraftingRecipeLogic recipeResolver;
    private boolean canTakeStack = false;

    public CraftingSlotWidget(CraftingRecipeLogic recipeResolver, int slotIndex, int xPosition, int yPosition) {
        super(createInventory(recipeResolver), slotIndex, xPosition, yPosition, false, false);
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
        if (id == 2) {
            if (recipeResolver.isRecipeValid()) {
                ClickData clickData = ClickData.readFromBuf(buffer);
                boolean isShiftDown = clickData.isShiftClick;
                boolean isLeftClick = clickData.button == 0;
                boolean isRightClick = clickData.button == 1;
                EntityPlayer player = gui.entityPlayer;
                if (isShiftDown) {
                    if (isLeftClick) {
                        //limit shift click to one stack at a time
                        int maxCrafts = this.slotReference.getStack().getMaxStackSize() / this.slotReference.getStack().getCount();
                        for (int i = 0; i < maxCrafts; i++) {
                            if (canMergeToInv(this.slotReference.getStack()) && recipeResolver.performRecipe(gui.entityPlayer)) {
                                recipeResolver.handleItemCraft(this.slotReference.getStack(), gui.entityPlayer);
                                ItemStack result = this.slotReference.getStack();
                                if (!player.inventory.addItemStackToInventory(result)) {
                                    player.dropItem(result, false);
                                }
                                this.recipeResolver.refreshOutputSlot();
                            }
                        }
                    } else if (isRightClick) {
                        while (canMergeToInv(this.slotReference.getStack()) && recipeResolver.performRecipe(gui.entityPlayer)) {
                            recipeResolver.handleItemCraft(this.slotReference.getStack(), gui.entityPlayer);
                            ItemStack result = this.slotReference.getStack();
                            if (!player.inventory.addItemStackToInventory(result)) {
                                player.dropItem(result, false);
                            }
                            this.recipeResolver.refreshOutputSlot();
                        }
                    }
                } else {
                    if (isLeftClick) {
                        if (canMerge(player.inventory.getItemStack(), this.slotReference.getStack()) && recipeResolver.performRecipe(gui.entityPlayer)) {
                            recipeResolver.handleItemCraft(this.slotReference.getStack(), gui.entityPlayer);
                            //send slot changes now, both of consumed items in inventory and result slot
                            ItemStack result = this.slotReference.getStack();
                            mergeToHand(result);
                            this.recipeResolver.refreshOutputSlot();
                        }
                    } else if (isRightClick) {
                        while (canMerge(player.inventory.getItemStack(), this.slotReference.getStack()) && recipeResolver.performRecipe(gui.entityPlayer)) {
                            recipeResolver.handleItemCraft(this.slotReference.getStack(), gui.entityPlayer);
                            ItemStack result = this.slotReference.getStack();
                            mergeToHand(result);
                            this.recipeResolver.refreshOutputSlot();
                        }
                    }
                }
                uiAccess.sendHeldItemUpdate();
                //send slot changes now, both of consumed items in inventory and result slot
                gui.entityPlayer.openContainer.detectAndSendChanges();
                uiAccess.sendSlotUpdate(this);
            }
        }
    }

    private boolean canMerge(ItemStack stack, ItemStack stack1) {
        if (stack.isEmpty()) return true;
        if (ItemStack.areItemsEqual(stack, stack1) && ItemStack.areItemStackTagsEqual(stack, stack1)) {
            return stack.getCount() + stack1.getCount() <= stack.getMaxStackSize();
        }
        return false;
    }

    private boolean canMergeToInv(ItemStack stack) {
        PlayerMainInvWrapper playerInv = new PlayerMainInvWrapper(gui.entityPlayer.inventory);
        for (int i = 0; i < playerInv.getSlots(); i++) {
            if (playerInv.insertItem(i, stack, true).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void mergeToHand(ItemStack toMerge) {
        EntityPlayer player = gui.entityPlayer;
        ItemStack itemInHand = gui.entityPlayer.inventory.getItemStack();
        if (itemInHand.isEmpty()) {
            itemInHand = toMerge;
            player.inventory.setItemStack(itemInHand);
        } else if (ItemStack.areItemsEqual(itemInHand, toMerge) && ItemStack.areItemStackTagsEqual(itemInHand, toMerge)) {
            //if the hand is not empty, try to merge the result with the hand
            if (itemInHand.getCount() + toMerge.getCount() <= itemInHand.getMaxStackSize()) {
                //if the result of the merge is smaller than the max stack size, merge
                itemInHand.grow(toMerge.getCount());
                player.inventory.setItemStack(itemInHand);
            }
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
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown());
            writeClientAction(2, clickData::writeToBuf);
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
