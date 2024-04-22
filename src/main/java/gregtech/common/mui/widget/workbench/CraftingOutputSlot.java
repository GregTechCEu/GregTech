package gregtech.common.mui.widget.workbench;

import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.google.common.collect.Lists;

import gregtech.common.metatileentities.storage.CraftingRecipeLogic;

import gregtech.common.metatileentities.storage.CraftingRecipeMemory;

import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;

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
                if (recipeLogic.isRecipeValid() && getSlot().canTakeStack(getSyncManager().getPlayer())) {
                    recipeLogic.performRecipe();
                    handleItemCraft(getSlot().getStack(), getSyncManager().getPlayer());
                }
            } else {
                super.readOnServer(id, buf);
            }
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

    public static class CraftingOutputModularSlot extends ModularSlot {

        private final IntSyncValue syncValue;
        private final CraftingRecipeLogic recipeLogic;
        private final CraftingRecipeMemory recipeMemory;
        private final IItemHandler craftingGrid;

        public CraftingOutputModularSlot(IItemHandler itemHandler, IntSyncValue syncValue, MetaTileEntityWorkbench workbench) {
            super(itemHandler, 0, true);
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
}
