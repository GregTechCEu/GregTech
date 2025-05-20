package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.storage.data.IAEItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ExportOnlyAEItemSlot extends ExportOnlyAESlot<IAEItemStack> implements IItemHandlerModifiable {

    protected Consumer<Integer> trigger;

    public ExportOnlyAEItemSlot(IAEItemStack config, IAEItemStack stock) {
        super(config, stock);
    }

    public ExportOnlyAEItemSlot() {
        super();
    }

    public void setTrigger(Consumer<Integer> trigger) {
        this.trigger = trigger;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(CONFIG_TAG)) {
            this.config = WrappedItemStack.fromNBT(nbt.getCompoundTag(CONFIG_TAG));
        }
        if (nbt.hasKey(STOCK_TAG)) {
            this.stock = WrappedItemStack.fromNBT(nbt.getCompoundTag(STOCK_TAG));
        }
    }

    @Override
    public @NotNull IConfigurableSlot<IAEItemStack> copy() {
        return new ExportOnlyAEItemSlot(
                this.config == null ? null : this.config.copy(),
                this.stock == null ? null : this.stock.copy());
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {}

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == 0 && this.stock != null) {
            return this.stock.getDefinition();
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0 && this.stock != null) {
            int extracted = (int) Math.min(this.stock.getStackSize(), amount);
            ItemStack result = this.stock.createItemStack();
            result.setCount(extracted);
            if (!simulate) {
                this.stock.decStackSize(extracted);
                if (this.stock.getStackSize() == 0) {
                    this.stock = null;
                }
            }
            if (this.trigger != null) {
                this.trigger.accept(0);
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public IAEItemStack requestStack() {
        IAEItemStack result = super.requestStack();
        if (result instanceof WrappedItemStack) {
            return ((WrappedItemStack) result).getAEStack();
        } else {
            return result;
        }
    }

    @Override
    public IAEItemStack exceedStack() {
        IAEItemStack result = super.exceedStack();
        if (result instanceof WrappedItemStack) {
            return ((WrappedItemStack) result).getAEStack();
        } else {
            return result;
        }
    }

    @Override
    public void addStack(IAEItemStack stack) {
        if (this.stock == null) {
            this.stock = WrappedItemStack.fromItemStack(stack.createItemStack());
        } else {
            this.stock.add(stack);
        }
        this.trigger.accept(0);
    }

    @Override
    public void setStack(IAEItemStack stack) {
        if (this.stock == null && stack == null) {
            return;
        } else if (stack == null) {
            this.stock = null;
        } else {
            // todo this could maybe be improved with better comparison check
            this.stock = WrappedItemStack.fromItemStack(stack.createItemStack());
        }
        this.trigger.accept(0);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }
}
