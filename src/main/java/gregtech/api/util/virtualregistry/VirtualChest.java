package gregtech.api.util.virtualregistry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

public class VirtualChest extends VirtualEntry implements IItemHandler {

    public static final int DEFAULT_SIZE = 9;

    private final ItemStackHandler handler;

    public VirtualChest() {
        this(DEFAULT_SIZE);
    }

    public VirtualChest(int size) {
        this.handler = new ItemStackHandler(size);
    }

    @Override
    public EntryTypes<VirtualChest> getType() {
        return EntryTypes.ENDER_ITEM;
    }

    @Override
    public int getSlots() {
        return handler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return handler.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setTag("handler", this.handler.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.handler.deserializeNBT(nbt.getCompoundTag("handler"));
        super.deserializeNBT(nbt);
    }
}
