package gregtech.common.mui.widget.workbench;

import gregtech.api.util.GTLog;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CraftingOutputSlot extends Widget<CraftingOutputSlot> implements Interactable {

    private final CraftingSlotSH syncHandler;

    public CraftingOutputSlot(IntSyncValue syncValue, MetaTileEntityWorkbench workbench) {
        this.syncHandler = new CraftingSlotSH(
                new CraftingOutputMS(
                        workbench.getCraftingRecipeLogic().getCraftingResultInventory(),
                        syncValue, workbench));
        setSyncHandler(this.syncHandler);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        MouseData mouseData = MouseData.create(mouseButton);
        this.syncHandler.syncToServer(2, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
        ItemStack itemstack = this.syncHandler.getOutputStack();
        if (itemstack.isEmpty()) return;

        guiScreen.setZ(100f);
        guiScreen.getItemRenderer().zLevel = 100.0F;

        int cachedCount = itemstack.getCount();
        itemstack.setCount(1); // required to not render the amount overlay
        RenderUtil.renderItemInGUI(itemstack, 1, 1);
        itemstack.setCount(cachedCount);

        guiScreen.getItemRenderer().zLevel = 0.0F;
        guiScreen.setZ(0f);
    }

    protected static class CraftingSlotSH extends SyncHandler {

        private final CraftingRecipeLogic recipeLogic;
        private final CraftingOutputMS slot;

        public CraftingSlotSH(CraftingOutputMS slot) {
            this.slot = slot;
            this.recipeLogic = slot.recipeLogic;
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == 2) {
                var data = MouseData.readPacket(buf);
                // todo handle shift transfer
                if (data.shift) return;

                if (recipeLogic.isRecipeValid() && this.slot.canTakeStack(getSyncManager().getPlayer())) {
                    recipeLogic.collectAvailableItems();
                    if (recipeLogic.performRecipe()) {
                        handleItemCraft(this.slot.getStack(), getSyncManager().getPlayer());
                        syncToClient(5, this::syncCraftedStack);
                    }
                }
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == 5) {
                getSyncManager().setCursorItem(readStackSafe(buf));
            }
        }

        private static ItemStack readStackSafe(PacketBuffer buffer) {
            var stack = ItemStack.EMPTY;
            try {
                stack = buffer.readItemStack();
            } catch (IOException ignore) {
                GTLog.logger.warn("A stack was read incorrectly, something is seriously wrong!");
            }
            return stack;
        }

        private void syncCraftedStack(PacketBuffer buf) {
            ItemStack curStack = getSyncManager().getCursorItem();
            ItemStack outStack = this.slot.getStack();
            ItemStack toSync = outStack.copy();
            if (curStack.getItem() == outStack.getItem() &&
                    curStack.getMetadata() == outStack.getMetadata() &&
                    ItemStack.areItemStackTagsEqual(curStack, outStack)) {

                int combined = curStack.getCount() + outStack.getCount();
                if (combined <= outStack.getMaxStackSize()) {
                    toSync.setCount(curStack.getCount() + outStack.getCount());
                } else {
                    toSync.setCount(outStack.getMaxStackSize());
                }
            } else if (!curStack.isEmpty()) {
                toSync = curStack;
            }
            buf.writeItemStack(toSync);
        }

        public ItemStack getOutputStack() {
            return slot.getStack();
        }

        public void handleItemCraft(ItemStack itemStack, EntityPlayer player) {
            itemStack.onCrafting(player.world, player, 1);

            var inventoryCrafting = recipeLogic.getCraftingMatrix();

            // if we're not simulated, fire the event, unlock recipe and add crafted items, and play sounds
            FMLCommonHandler.instance().firePlayerCraftingEvent(player, itemStack, inventoryCrafting);

            var cachedRecipe = recipeLogic.getCachedRecipe();
            if (cachedRecipe != null && !cachedRecipe.isDynamic()) {
                player.unlockRecipes(Lists.newArrayList(cachedRecipe));
            }
            if (cachedRecipe != null) {
                ItemStack resultStack = cachedRecipe.getCraftingResult(inventoryCrafting);
                // itemsCrafted += resultStack.getCount();
                this.slot.notifyRecipePerformed(resultStack);
            }
            // call method from recipe logic to sync to client
        }
    }

    protected static class CraftingOutputMS extends ModularSlot {

        private final IntSyncValue syncValue;
        private final CraftingRecipeLogic recipeLogic;
        private final CraftingRecipeMemory recipeMemory;
        private final IItemHandler craftingGrid;

        public CraftingOutputMS(IInventory craftingInventory, IntSyncValue syncValue,
                                MetaTileEntityWorkbench workbench) {
            super(new InventoryWrapper(craftingInventory, workbench.getCraftingRecipeLogic()), 0, true);
            this.syncValue = syncValue;
            this.recipeLogic = workbench.getCraftingRecipeLogic();
            this.recipeMemory = workbench.getRecipeMemory();
            this.craftingGrid = workbench.getCraftingGrid();
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            ItemStack curStack = playerIn.inventory.getItemStack();
            if (curStack.isEmpty()) return true;

            ItemStack outStack = recipeLogic.getCachedRecipe().getRecipeOutput();
            if (curStack.getItem() == outStack.getItem() &&
                    curStack.getMetadata() == outStack.getMetadata() &&
                    ItemStack.areItemStackTagsEqual(curStack, outStack)) {

                int combined = curStack.getCount() + outStack.getCount();
                return combined <= outStack.getMaxStackSize();
            } else {
                return false;
            }
        }

        public void notifyRecipePerformed(ItemStack stack) {
            this.syncValue.setValue(this.syncValue.getValue() + stack.getCount(), true, true);
            this.recipeMemory.notifyRecipePerformed(this.craftingGrid, stack);
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {
            super.putStack(getStack());
        }

        @Override
        public @NotNull ItemStack decrStackSize(int amount) {
            return getStack();
        }
    }

    private static class InventoryWrapper implements IItemHandlerModifiable {

        private final IInventory inventory;
        private final CraftingRecipeLogic recipeLogic;

        private InventoryWrapper(IInventory inventory, CraftingRecipeLogic recipeLogic) {
            this.inventory = inventory;
            this.recipeLogic = recipeLogic;
        }

        @Override
        public int getSlots() {
            return inventory.getSizeInventory();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot).copy();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getInventoryStackLimit();
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!recipeLogic.isRecipeValid()) {
                inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }

            if (!stack.isEmpty())
                inventory.setInventorySlotContents(slot, stack);
        }
    }
}
