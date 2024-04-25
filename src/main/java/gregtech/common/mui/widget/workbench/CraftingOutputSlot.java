package gregtech.common.mui.widget.workbench;

import gregtech.api.util.GTLog;
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

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CraftingOutputSlot extends ItemSlot {

    private CraftingSlotSH syncHandler;

    @Override
    public ItemSlot slot(ModularSlot slot) {
        if (slot instanceof CraftingOutputModularSlot craftingSlot) {
            this.syncHandler = new CraftingSlotSH(craftingSlot);
            if (isValidSyncHandler(this.syncHandler))
                setSyncHandler(this.syncHandler);
        } else {
            super.slot(slot);
        }
        return this;
    }

    public static ModularSlot modular(IntSyncValue syncValue, MetaTileEntityWorkbench workbench) {
        return new CraftingOutputModularSlot(workbench.getCraftingRecipeLogic().getCraftingResultInventory(), syncValue,
                workbench);
    }

    @SuppressWarnings("UnstableApiUsage")
    protected static class CraftingSlotSH extends ItemSlotSH {

        private final CraftingRecipeLogic recipeLogic;
        private final IntSyncValue syncValue;
        private final CraftingRecipeMemory recipeMemory;
        private final IItemHandler craftingGrid;

        public CraftingSlotSH(CraftingOutputModularSlot slot) {
            super(slot);
            this.recipeLogic = slot.recipeLogic;
            this.syncValue = slot.syncValue;
            this.recipeMemory = slot.recipeMemory;
            this.craftingGrid = slot.craftingGrid;
        }

        @Override
        public CraftingOutputModularSlot getSlot() {
            return (CraftingOutputModularSlot) super.getSlot();
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {
            if (id == 2) {
                var data = MouseData.readPacket(buf);
                // todo handle shift transfer
                if (data.shift) return;

                if (recipeLogic.isRecipeValid() && getSlot().canTakeStack(getSyncManager().getPlayer())) {
                    recipeLogic.collectAvailableItems();
                    recipeLogic.performRecipe();
                    syncToClient(5, this::syncCraftedStack);
                    handleItemCraft(getSlot().getStack(), getSyncManager().getPlayer());
                }
            } else {
                super.readOnServer(id, buf);
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            super.readOnClient(id, buf);
            if (id == 5) {
                getSyncManager().setCursorItem(readStackSafe(buf));
            } else if (id == 6) {

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
            ItemStack outStack = recipeLogic.getCachedRecipe().getRecipeOutput();
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
                this.syncValue.setValue(this.syncValue.getValue() + resultStack.getCount(), true, true);
                // itemsCrafted += resultStack.getCount();
                recipeMemory.notifyRecipePerformed(craftingGrid, resultStack);
            }
            // call method from recipe logic to sync to client
        }
    }

    protected static class CraftingOutputModularSlot extends ModularSlot {

        private final IntSyncValue syncValue;
        private final CraftingRecipeLogic recipeLogic;
        private final CraftingRecipeMemory recipeMemory;
        private final IItemHandler craftingGrid;

        public CraftingOutputModularSlot(IInventory craftingInventory, IntSyncValue syncValue,
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