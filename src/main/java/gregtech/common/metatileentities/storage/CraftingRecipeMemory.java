package gregtech.common.metatileentities.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CraftingRecipeMemory extends SyncHandler {

    // client and server
    public static final int UPDATE_RECIPES = 1;

    // client only
    public static final int SYNC_RECIPE = 4;
    public static final int OFFSET_RECIPE = 5;
    public static final int REMOVE_RECIPE = 2;
    public static final int MAKE_RECIPE = 3;

    // server only
    public static final int MOUSE_CLICK = 2;

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

    /**
     * Offsets recipes from {@code startIndex} to the right, skipping locked recipes
     *
     * @param startIndex the index to start offsetting recipes
     */
    private void offsetRecipe(int startIndex) {
        MemorizedRecipe previousRecipe = removeRecipe(startIndex);
        if (previousRecipe == null) return;
        for (int i = startIndex + 1; i < memorizedRecipes.length; i++) {
            MemorizedRecipe recipe = memorizedRecipes[i];
            if (recipe != null && recipe.recipeLocked) continue;
            memorizedRecipes[i] = previousRecipe;
            memorizedRecipes[i].index = i;

            // we found a null recipe and there's no more recipes to check,
            if (recipe == null) return;

            previousRecipe = recipe;
        }
    }

    @Nullable
    private MemorizedRecipe findOrCreateRecipe(ItemStack resultItemStack) {
        // search preexisting recipe with identical recipe result
        MemorizedRecipe existing = null;
        for (MemorizedRecipe memorizedRecipe : memorizedRecipes) {
            if (memorizedRecipe != null &&
                    ItemStack.areItemStacksEqual(memorizedRecipe.recipeResult, resultItemStack)) {
                existing = memorizedRecipe;
                break;
            }
        }

        // we already have a recipe that matches
        // move it to the front
        if (existing != null && !existing.recipeLocked) {
            if (existing.index == 0) return existing; // it's already at the front
            int removed = existing.index;
            removeRecipe(existing.index);
            syncToClient(REMOVE_RECIPE, buffer -> buffer.writeByte(removed));
            offsetRecipe(0);
            syncToClient(OFFSET_RECIPE, buffer -> buffer.writeByte(0));
            existing.index = 0;
            return memorizedRecipes[0] = existing;
        }

        // put new memorized recipe into array
        for (int i = 0; i < memorizedRecipes.length; i++) {
            MemorizedRecipe memorizedRecipe;
            if (memorizedRecipes[i] == null) {
                memorizedRecipe = new MemorizedRecipe(i);
            } else if (memorizedRecipes[i].recipeLocked) {
                continue;
            } else {
                offsetRecipe(i);
                memorizedRecipe = new MemorizedRecipe(i);
                syncToClient(OFFSET_RECIPE, buffer -> buffer.writeByte(memorizedRecipe.index));
            }
            memorizedRecipe.initialize(resultItemStack);
            return memorizedRecipes[i] = memorizedRecipe;
        }
        return null;
    }

    public void notifyRecipePerformed(IItemHandler craftingGrid, ItemStack resultStack) {
        MemorizedRecipe recipe = findOrCreateRecipe(resultStack);
        if (recipe != null) {
            // notify slot and sync to client
            recipe.updateCraftingMatrix(craftingGrid);
            recipe.timesUsed++;
            syncToClient(SYNC_RECIPE, recipe::writeToBuffer);
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

    public final MemorizedRecipe removeRecipe(int index) {
        if (hasRecipe(index)) {
            MemorizedRecipe removed = memorizedRecipes[index];
            memorizedRecipes[index] = null;
            return removed;
        }
        return null;
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
        if (id == UPDATE_RECIPES) {
            this.readRecipes(buf);
        } else if (id == REMOVE_RECIPE) {
            this.removeRecipe(buf.readByte());
        } else if (id == MAKE_RECIPE) {
            int index = buf.readByte();
            var recipe = memorizedRecipes[index];
            if (recipe == null) recipe = new MemorizedRecipe(index);
            recipe.recipeResult = NetworkUtils.readItemStack(buf);
            recipe.index = index;
            memorizedRecipes[index] = recipe;
        } else if (id == SYNC_RECIPE) {
            var recipe = MemorizedRecipe.fromBuffer(buf);
            memorizedRecipes[recipe.index] = recipe;
        } else if (id == OFFSET_RECIPE) {
            this.offsetRecipe(buf.readByte());
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
            NetworkUtils.writeItemStack(buf, recipe.recipeResult);
            buf.writeInt(recipe.timesUsed);
            buf.writeBoolean(recipe.isRecipeLocked());
        }
    }

    public void readRecipes(PacketBuffer buf) {
        int size = buf.readByte();
        for (int i = 0; i < size; i++) {
            int index = buf.readByte();
            if (!hasRecipe(index))
                memorizedRecipes[index] = new MemorizedRecipe(index);

            memorizedRecipes[index].recipeResult = NetworkUtils.readItemStack(buf);
            memorizedRecipes[index].timesUsed = buf.readInt();
            memorizedRecipes[index].recipeLocked = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == UPDATE_RECIPES) {
            syncToClient(UPDATE_RECIPES, this::writeRecipes);
        } else if (id == MOUSE_CLICK) {
            // read mouse data
            int index = buf.readByte();
            var data = MouseData.readPacket(buf);
            var recipe = getRecipeAtIndex(index);
            if (recipe == null) return;

            if (data.shift && data.mouseButton == 0) {
                recipe.setRecipeLocked(!recipe.isRecipeLocked());
            } else if (data.mouseButton == 0) {
                loadRecipe(index);
            } else if (data.mouseButton == 1 && !recipe.isRecipeLocked()) {
                removeRecipe(index);
            }
        }
    }

    public static class MemorizedRecipe {

        private final ItemStackHandler craftingMatrix = new ItemStackHandler(9);
        private ItemStack recipeResult = ItemStack.EMPTY;
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

        private void writeToBuffer(PacketBuffer buffer) {
            buffer.writeByte(this.index);
            buffer.writeInt(this.timesUsed);
            buffer.writeBoolean(this.recipeLocked);
            NetworkUtils.writeItemStack(buffer, this.recipeResult);
        }

        private static @NotNull MemorizedRecipe fromBuffer(PacketBuffer buffer) {
            var recipe = new MemorizedRecipe(buffer.readByte());
            recipe.timesUsed = buffer.readInt();
            recipe.recipeLocked = buffer.readBoolean();
            recipe.recipeResult = NetworkUtils.readItemStack(buffer);
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

        public MemorizedRecipe copy() {
            var recipe = new MemorizedRecipe(this.index);
            recipe.initialize(this.recipeResult);
            recipe.updateCraftingMatrix(this.craftingMatrix);
            recipe.recipeLocked = this.recipeLocked;
            recipe.timesUsed = this.timesUsed;
            return recipe;
        }

        @Override
        public String toString() {
            return String.format("MemorizedRecipe{%dx %s, locked: %s, times used: %d}",
                    getRecipeResult().getCount(),
                    getRecipeResult().getDisplayName(),
                    recipeLocked,
                    timesUsed);
        }
    }
}
