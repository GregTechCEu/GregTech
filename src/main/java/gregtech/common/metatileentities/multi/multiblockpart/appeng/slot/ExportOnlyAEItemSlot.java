package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
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
            NBTTagCompound tag = nbt.getCompoundTag(CONFIG_TAG);
            // Check if the Cnt tag is present. If it isn't, the config was written with the old wrapped stacks.
            if (tag.hasKey("Cnt", Constants.NBT.TAG_LONG)) {
                this.config = AEItemStack.fromNBT(tag);
            } else {
                this.config = AEItemStack.fromItemStack(new ItemStack(tag));
            }
        }

        if (nbt.hasKey(STOCK_TAG)) {
            NBTTagCompound tag = nbt.getCompoundTag(STOCK_TAG);
            // Check if the Cnt tag is present. If it isn't, the config was written with the old wrapped stacks.
            if (tag.hasKey("Cnt", Constants.NBT.TAG_LONG)) {
                this.stock = AEItemStack.fromNBT(tag);
            } else {
                this.stock = AEItemStack.fromItemStack(new ItemStack(tag));
            }
        }
    }

    @Override
    public @NotNull IConfigurableSlot<IAEItemStack> copy() {
        return new ExportOnlyAEItemSlot(
                this.config == null ? null : this.config.copy(),
                this.stock == null ? null : this.stock.copy());
    }

    @Override
    public void decrementStock(long amount) {
        if (stock == null) return;
        stock.decStackSize(amount);
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
    public void addStack(IAEItemStack stack) {
        if (this.stock == null) {
            this.stock = stack.copy();
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
            this.stock = stack.copy();
        }
        this.trigger.accept(0);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }
}
