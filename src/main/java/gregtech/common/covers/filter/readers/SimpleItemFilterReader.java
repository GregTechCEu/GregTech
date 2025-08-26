package gregtech.common.covers.filter.readers;

import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

public class SimpleItemFilterReader extends BaseFilterReader implements IItemHandlerModifiable {

    public static final String COUNT = "Count";
    protected static final String LEGACY_ITEM_KEY = "ItemFilter";
    protected static final String LEGACY_STACK_SIZE = "BigStackSize";
    public static final String RESPECT_NBT = "IgnoreNBT";
    public static final String RESPECT_DAMAGE = "IgnoreDamage";

    public SimpleItemFilterReader(ItemStack container, int slots) {
        super(container, slots);
    }

    public void setIgnoreDamage(boolean ignoreDamage) {
        if (!getStackTag().getBoolean(RESPECT_DAMAGE) == ignoreDamage)
            return;

        if (ignoreDamage)
            getStackTag().removeTag(RESPECT_DAMAGE);
        else
            getStackTag().setBoolean(RESPECT_DAMAGE, true);
        markDirty();
    }

    @Override
    public int getSlots() {
        return getSize();
    }

    public void setIgnoreNBT(boolean ignoreNBT) {
        if (!getStackTag().getBoolean(RESPECT_NBT) == ignoreNBT)
            return;

        if (ignoreNBT)
            getStackTag().removeTag(RESPECT_NBT);
        else
            getStackTag().setBoolean(RESPECT_NBT, true);
        markDirty();
    }

    public boolean isIgnoreDamage() {
        return !getStackTag().getBoolean(RESPECT_DAMAGE);
    }

    public boolean isIgnoreNBT() {
        return !getStackTag().getBoolean(RESPECT_NBT);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getMaxTransferRate();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (validateSlotIndex(slot)) {
            NBTTagCompound item = getTagAt(slot);
            return item.isEmpty() ? ItemStack.EMPTY : new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (validateSlotIndex(slot)) {
            if (!stack.isEmpty()) {
                stack.setCount(Math.min(stack.getCount(), isBlacklistFilter() ? 1 : getMaxTransferRate()));
            }
            NBTTagList list = getInventoryNbt();
            list.set(slot, stack.isEmpty() ? new NBTTagCompound() : stack.serializeNBT());
            markDirty();
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        ItemStack existing = getStackInSlot(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0) return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
                setStackInSlot(slot, existing);
            }
        }

        return reachedLimit ? GTUtility.copy(stack.getCount() - limit, stack) : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;

        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                setStackInSlot(slot, ItemStack.EMPTY);
            }
            return existing;
        } else {
            if (!simulate) {
                setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            }

            return GTUtility.copy(toExtract, existing);
        }
    }

    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public void onTransferRateChange() {
        for (int i = 0; i < getSlots(); i++) {
            ItemStack itemStack = getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                itemStack.setCount(Math.min(itemStack.getCount(), isBlacklistFilter() ? 1 : getMaxTransferRate()));
                setStackInSlot(i, itemStack);
            }
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);

        if (nbt.hasKey(RESPECT_DAMAGE))
            this.setIgnoreDamage(nbt.getBoolean(RESPECT_DAMAGE));

        if (nbt.hasKey(RESPECT_NBT))
            this.setIgnoreNBT(nbt.getBoolean(RESPECT_NBT));
    }

    @Override
    public void handleLegacyNBT(NBTTagCompound tag) {
        super.handleLegacyNBT(tag);
        NBTTagCompound legacyFilter = tag.getCompoundTag(KEY_LEGACY_FILTER);

        if (legacyFilter.hasKey(LEGACY_ITEM_KEY)) {
            var temp = new ItemStackHandler();
            var legacyTag = legacyFilter.getCompoundTag(LEGACY_ITEM_KEY);
            var stackSizes = legacyTag.getCompoundTag(LEGACY_STACK_SIZE);

            temp.deserializeNBT(legacyTag);
            for (int i = 0; i < temp.getSlots(); i++) {
                var stack = temp.getStackInSlot(i);
                if (stack.isEmpty())
                    continue;

                if (stackSizes.hasKey(String.valueOf(i)))
                    stack.setCount(stackSizes.getInteger(String.valueOf(i)));

                var stackTag = stack.serializeNBT();
                stackTag.setInteger(COUNT, stack.getCount());
                getInventoryNbt().set(i, stackTag);
            }
        }
    }
}
