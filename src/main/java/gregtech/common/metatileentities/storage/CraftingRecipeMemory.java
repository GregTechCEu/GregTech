package gregtech.common.metatileentities.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public class CraftingRecipeMemory extends SyncHandler {

    private final MemorizedRecipe[] memorizedRecipes;
    private final IItemHandlerModifiable craftingMatrix;

    public CraftingRecipeMemory(int memorySize, IItemHandlerModifiable craftingMatrix) {
        this.memorizedRecipes = new MemorizedRecipe[memorySize];
        this.craftingMatrix = craftingMatrix;
    }

    public void loadRecipe(int index) {
        MemorizedRecipe recipe = memorizedRecipes[index];
        if (recipe != null) {
            copyInventoryItems(recipe.craftingMatrix, this.craftingMatrix);
        }
    }

    @Nullable
    public MemorizedRecipe getRecipeAtIndex(int index) {
        return memorizedRecipes[index];
    }

    @SuppressWarnings("DataFlowIssue")
    public @NotNull ItemStack getRecipeOutputAtIndex(int index) {
        return hasRecipe(index) ? getRecipeAtIndex(index).getRecipeResult() : ItemStack.EMPTY;
    }

    @Nullable
    private MemorizedRecipe offsetRecipe(int startIndex) {
        MemorizedRecipe previousRecipe = memorizedRecipes[startIndex];
        for (int i = startIndex + 1; i < memorizedRecipes.length; i++) {
            MemorizedRecipe recipe = memorizedRecipes[i];
            if (recipe != null && recipe.recipeLocked) continue;
            memorizedRecipes[i] = previousRecipe;
            if (recipe == null) return null;
            recipe.index = i;
            syncToClient(3, buffer -> {
                buffer.writeByte(recipe.index);
                buffer.writeItemStack(recipe.recipeResult);
            });
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
                memorizedRecipe = new MemorizedRecipe(i);
            } else if (memorizedRecipes[i].recipeLocked) {
                continue;
            } else {
                memorizedRecipe = offsetRecipe(i);
                if (memorizedRecipe == null) {
                    memorizedRecipe = new MemorizedRecipe(i);
                }
            }
            memorizedRecipe.initialize(resultItemStack);
            memorizedRecipes[i] = memorizedRecipe;
            var sync = memorizedRecipes[i];
            syncToClient(3, buffer -> {
                buffer.writeByte(sync.index);
                buffer.writeItemStack(sync.recipeResult);
            });
            return memorizedRecipe;
        }
        return null;
    }

    public void notifyRecipePerformed(IItemHandler craftingGrid, ItemStack resultStack) {
        MemorizedRecipe recipe = findOrCreateRecipe(resultStack);
        if (recipe != null) {
            // notify slot and sync to client
            recipe.updateCraftingMatrix(craftingGrid);
            recipe.timesUsed++;
            syncToClient(4, buffer -> buffer.writeByte(recipe.index));
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
            MemorizedRecipe recipe = MemorizedRecipe.deserializeNBT(entryComponent.getCompoundTag("Recipe"), slotIndex);
            this.memorizedRecipes[slotIndex] = recipe;
        }
    }

    private static void copyInventoryItems(IItemHandler src, IItemHandlerModifiable dest) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack itemStack = src.getStackInSlot(i);
            dest.setStackInSlot(i, itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy());
        }
    }

    public final void removeRecipe(int index) {
        if (hasRecipe(index)) {
            memorizedRecipes[index] = null;
            syncToClient(4, buffer -> buffer.writeByte(index));
        }
    }

    public final boolean hasRecipe(int index) {
        return memorizedRecipes[index] != null;
    }

    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        this.writeRecipes(buf);
    }

    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        this.readRecipes(buf);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            this.readRecipes(buf);
        } else if (id == 2) {
            this.removeRecipe(buf.readByte());
        } else if (id == 3) {
            int index = buf.readByte();
            if (!hasRecipe(index)) memorizedRecipes[index] = new MemorizedRecipe(index);
            memorizedRecipes[index].recipeResult = readStackSafe(buf);
        } else if (id == 4) {
            memorizedRecipes[buf.readByte()].timesUsed++;
        }
    }

    public void writeRecipes(PacketBuffer buf) {
        Map<Integer, ItemStack> written = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < memorizedRecipes.length; i++) {
            var stack = getRecipeOutputAtIndex(i);
            if (stack.isEmpty()) continue;
            written.put(i, stack);
        }
        buf.writeByte(written.size());
        for (var entry : written.entrySet()) {
            var recipe = memorizedRecipes[entry.getKey()];
            buf.writeByte(recipe.index);
            buf.writeItemStack(recipe.recipeResult);
            buf.writeInt(recipe.timesUsed);
        }
    }

    public void readRecipes(PacketBuffer buf) {
        int size = buf.readByte();
        for (int i = 0; i < size; i++) {
            int index = buf.readByte();
            if (!hasRecipe(index))
                memorizedRecipes[index] = new MemorizedRecipe(index);

            memorizedRecipes[index].recipeResult = readStackSafe(buf);
            memorizedRecipes[index].timesUsed = buf.readInt();
        }
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            syncToClient(1, this::writeRecipes);
        } else if (id == 2) {
            // read mouse data
            int index = buf.readByte();
            var data = MouseData.readPacket(buf);
            if (data.shift && data.mouseButton == 0 && hasRecipe(index)) {
                var recipe = getRecipeAtIndex(index);
                recipe.setRecipeLocked(!recipe.isRecipeLocked());
            } else if (data.mouseButton == 0) {
                loadRecipe(index);
            } else if (data.mouseButton == 1) {
                if (hasRecipe(index) && !getRecipeAtIndex(index).isRecipeLocked())
                    removeRecipe(index);
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

    public static class MemorizedRecipe {

        private final ItemStackHandler craftingMatrix = new ItemStackHandler(9);
        private ItemStack recipeResult;
        private boolean recipeLocked = false;
        public int timesUsed = 0;
        public int index;

        private MemorizedRecipe(int index) {
            this.index = index;
        }

        private NBTTagCompound serializeNBT() {
            NBTTagCompound result = new NBTTagCompound();
            result.setTag("Result", recipeResult.serializeNBT());
            result.setTag("Matrix", craftingMatrix.serializeNBT());
            result.setBoolean("Locked", recipeLocked);
            result.setInteger("TimesUsed", timesUsed);
            return result;
        }

        private static MemorizedRecipe deserializeNBT(NBTTagCompound tagCompound, int index) {
            MemorizedRecipe recipe = new MemorizedRecipe(index);
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
