package gregtech.common.metatileentities.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

public class CraftingRecipeMemory {

    private final MemorizedRecipe[] memorizedRecipes;

    public CraftingRecipeMemory(int memorySize) {
        this.memorizedRecipes = new MemorizedRecipe[memorySize];
    }

    public void loadRecipe(int index, IItemHandlerModifiable craftingGrid) {
        MemorizedRecipe recipe = memorizedRecipes[index];
        if (recipe != null) {
            copyInventoryItems(recipe.craftingMatrix, craftingGrid);
        }
    }

    @Nullable
    public MemorizedRecipe getRecipeAtIndex(int index) {
        return memorizedRecipes[index];
    }

    @Nullable
    private MemorizedRecipe offsetRecipe(int startIndex) {
        MemorizedRecipe previousRecipe = memorizedRecipes[startIndex];
        for (int i = startIndex + 1; i < memorizedRecipes.length; i++) {
            MemorizedRecipe recipe = memorizedRecipes[i];
            if (recipe != null && recipe.recipeLocked) continue;
            memorizedRecipes[i] = previousRecipe;
            if (recipe == null) return null;
            previousRecipe = recipe;
        }
        return previousRecipe;
    }

    @Nullable
    private MemorizedRecipe findOrCreateRecipe(ItemStack resultItemStack) {
        // search preexisting recipe with identical recipe result
        for (MemorizedRecipe memorizedRecipe : memorizedRecipes) {
            if (memorizedRecipe != null &&
                    ItemStack.areItemStacksEqual(memorizedRecipe.recipeResult, resultItemStack)) {
                return memorizedRecipe;
            }
        }
        // put new memorized recipe into array
        for (int i = 0; i < memorizedRecipes.length; i++) {
            MemorizedRecipe memorizedRecipe;
            if (memorizedRecipes[i] == null) {
                memorizedRecipe = new MemorizedRecipe();
            } else if (memorizedRecipes[i].recipeLocked) {
                continue;
            } else {
                memorizedRecipe = offsetRecipe(i);
                if (memorizedRecipe == null) {
                    memorizedRecipe = new MemorizedRecipe();
                }
            }
            memorizedRecipe.initialize(resultItemStack);
            memorizedRecipes[i] = memorizedRecipe;
            return memorizedRecipe;
        }
        return null;
    }

    public void notifyRecipePerformed(IItemHandler craftingGrid, ItemStack resultStack) {
        MemorizedRecipe recipe = findOrCreateRecipe(resultStack);
        if (recipe != null) {
            recipe.updateCraftingMatrix(craftingGrid);
            recipe.timesUsed++;
        }
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        NBTTagList resultList = new NBTTagList();
        tagCompound.setTag("Memory", resultList);
        for (int i = 0; i < memorizedRecipes.length; i++) {
            MemorizedRecipe recipe = memorizedRecipes[i];
            if (recipe == null) continue;
            NBTTagCompound entryComponent = new NBTTagCompound();
            entryComponent.setInteger("Slot", i);
            entryComponent.setTag("Recipe", recipe.serializeNBT());
            resultList.appendTag(entryComponent);
        }
        return tagCompound;
    }

    public void deserializeNBT(NBTTagCompound tagCompound) {
        NBTTagList resultList = tagCompound.getTagList("Memory", NBT.TAG_COMPOUND);
        for (int i = 0; i < resultList.tagCount(); i++) {
            NBTTagCompound entryComponent = resultList.getCompoundTagAt(i);
            int slotIndex = entryComponent.getInteger("Slot");
            MemorizedRecipe recipe = MemorizedRecipe.deserializeNBT(entryComponent.getCompoundTag("Recipe"));
            this.memorizedRecipes[slotIndex] = recipe;
        }
    }

    private static void copyInventoryItems(IItemHandler src, IItemHandlerModifiable dest) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack itemStack = src.getStackInSlot(i);
            dest.setStackInSlot(i, itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy());
        }
    }

    public static class MemorizedRecipe {

        private final ItemStackHandler craftingMatrix = new ItemStackHandler(9);
        private ItemStack recipeResult;
        private boolean recipeLocked = false;
        private int timesUsed = 0;

        private MemorizedRecipe() {}

        private NBTTagCompound serializeNBT() {
            NBTTagCompound result = new NBTTagCompound();
            result.setTag("Result", recipeResult.serializeNBT());
            result.setTag("Matrix", craftingMatrix.serializeNBT());
            result.setBoolean("Locked", recipeLocked);
            result.setInteger("TimesUsed", timesUsed);
            return result;
        }

        private static MemorizedRecipe deserializeNBT(NBTTagCompound tagCompound) {
            MemorizedRecipe recipe = new MemorizedRecipe();
            recipe.recipeResult = new ItemStack(tagCompound.getCompoundTag("Result"));
            recipe.craftingMatrix.deserializeNBT(tagCompound.getCompoundTag("Matrix"));
            recipe.recipeLocked = tagCompound.getBoolean("Locked");
            recipe.timesUsed = tagCompound.getInteger("TimesUsed");
            return recipe;
        }

        private void initialize(ItemStack recipeResult) {
            this.recipeResult = recipeResult.copy();
            for (int i = 0; i < this.craftingMatrix.getSlots(); i++) {
                this.craftingMatrix.setStackInSlot(i, ItemStack.EMPTY);
            }
            this.recipeLocked = false;
            this.timesUsed = 0;
        }

        private void updateCraftingMatrix(IItemHandler craftingGrid) {
            // do not modify crafting grid for locked recipes
            if (!recipeLocked) {
                copyInventoryItems(craftingGrid, craftingMatrix);
            }
        }

        public ItemStack getRecipeResult() {
            return recipeResult;
        }

        public boolean isRecipeLocked() {
            return recipeLocked;
        }

        public void setRecipeLocked(boolean recipeLocked) {
            this.recipeLocked = recipeLocked;
        }
    }
}
