package gregtech.common.metatileentities.multi.multiblockpart.appeng.stack;

import gregtech.api.util.NetworkUtil;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrappedItemStack implements IAEItemStack, IWrappedStack<IAEItemStack, ItemStack> {

    @NotNull
    private ItemStack delegate;
    private long stackSize;

    private WrappedItemStack(@NotNull ItemStack itemStack, long stackSize) {
        this.delegate = itemStack;
        this.stackSize = stackSize;
    }

    public static WrappedItemStack fromItemStack(@Nullable ItemStack stack) {
        if (stack == null) return null;
        return stack.isEmpty() ? null : new WrappedItemStack(stack, stack.getCount());
    }

    public static WrappedItemStack fromItemStack(@Nullable ItemStack stack, long amount) {
        if (stack == null) return null;
        return stack.isEmpty() ? null : new WrappedItemStack(stack, amount);
    }

    public static WrappedItemStack fromNBT(@Nullable NBTTagCompound tag) {
        if (tag == null) {
            return null;
        } else {
            // Migrate old NBT entries from the old format
            if (tag.getBoolean("wrapped")) {
                return new WrappedItemStack(new ItemStack(tag.getCompoundTag("stack")), tag.getLong("stackSize"));
            } else {
                ItemStack itemStack = new ItemStack(tag);
                return fromItemStack(itemStack);
            }
        }
    }

    public static WrappedItemStack fromPacket(@NotNull PacketBuffer data) {
        WrappedItemStack wrappedItemStack = fromItemStack(NetworkUtil.readItemStack(data));
        wrappedItemStack.setStackSize(data.readLong());
        return wrappedItemStack;
    }

    public AEItemStack getAEStack() {
        AEItemStack aeItemStack = AEItemStack.fromItemStack(this.delegate);
        assert aeItemStack != null;
        aeItemStack.setStackSize(stackSize);
        return aeItemStack;
    }

    @Override
    public ItemStack createItemStack() {
        return this.delegate.copy();
    }

    @Override
    public boolean hasTagCompound() {
        return this.delegate.hasTagCompound();
    }

    @Override
    public void add(IAEItemStack iaeItemStack) {
        if (equals(iaeItemStack)) {
            incStackSize(iaeItemStack.getStackSize());
        }
    }

    @Override
    public long getStackSize() {
        return stackSize;
    }

    @Override
    public IAEItemStack setStackSize(long newStackSize) {
        this.stackSize = newStackSize;
        return this;
    }

    @Override
    public long getCountRequestable() {
        return 0;
    }

    @Override
    public IAEItemStack setCountRequestable(long l) {
        return this;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack setCraftable(boolean b) {
        return this;
    }

    @Override
    public IAEItemStack reset() {
        this.delegate.setCount(0);
        this.stackSize = 0;
        return this;
    }

    @Override
    public boolean isMeaningful() {
        return !this.delegate.isEmpty();
    }

    @Override
    public void incStackSize(long add) {
        if (add < 1) return;
        this.stackSize += Math.min(Long.MAX_VALUE - this.stackSize, add);
    }

    @Override
    public void decStackSize(long sub) {
        if (sub < 1) return;
        this.stackSize -= Math.min(this.stackSize, sub);
    }

    @Override
    public void incCountRequestable(long l) {
        // NO-OP
    }

    @Override
    public void decCountRequestable(long l) {
        // NO-OP
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setTag("stack", this.delegate.serializeNBT());
        nbtTagCompound.setLong("stackSize", this.stackSize);
        nbtTagCompound.setBoolean("wrapped", true);
    }

    @Override
    public boolean fuzzyComparison(IAEItemStack stack, FuzzyMode fuzzyMode) {
        return stack.createItemStack().isItemEqual(this.delegate);
    }

    @Override
    public void writeToPacket(ByteBuf byteBuf) {
        writeToPacketBuffer(new PacketBuffer(byteBuf));
    }

    public void writeToPacketBuffer(@NotNull PacketBuffer packetBuffer) {
        NetworkUtil.writeItemStack(packetBuffer, this.delegate);
        packetBuffer.writeLong(this.stackSize);
    }

    @Override
    public IAEItemStack copy() {
        return new WrappedItemStack(delegate.copy(), stackSize);
    }

    @Override
    public @NotNull IAEItemStack copyAsAEStack() {
        IAEItemStack stack = AEItemStack.fromItemStack(delegate.copy());
        if (stack == null) {
            throw new IllegalStateException("Error creating AEItemStack from delegate");
        }

        stack.setStackSize(stackSize);
        return stack;
    }

    @Override
    public @NotNull IWrappedStack<IAEItemStack, ItemStack> copyWrapped() {
        return new WrappedItemStack(delegate.copy(), stackSize);
    }

    @Override
    public IAEItemStack empty() {
        IAEItemStack copy = copy();
        copy.reset();
        return copy;
    }

    @Override
    public boolean isItem() {
        return true;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        return this.delegate;
    }

    @Override
    public Item getItem() {
        return this.delegate.getItem();
    }

    @Override
    public int getItemDamage() {
        return this.delegate.getItemDamage();
    }

    @Override
    public boolean sameOre(IAEItemStack iaeItemStack) {
        return false;
    }

    @Override
    public boolean isSameType(IAEItemStack iaeItemStack) {
        if (iaeItemStack == null) return false;
        IAEItemStack aeStack = AEItemStack.fromItemStack(this.delegate);
        if (aeStack == null) return false;
        return aeStack.isSameType(iaeItemStack);
    }

    @Override
    public boolean isSameType(ItemStack itemStack) {
        if (this.delegate.isEmpty()) return itemStack.isEmpty();
        IAEItemStack aeStack = AEItemStack.fromItemStack(this.delegate);
        if (aeStack == null) return false;
        return aeStack.isSameType(itemStack);
    }

    @Override
    public @NotNull ItemStack getDefinition() {
        delegate.setCount((int) stackSize);
        return this.delegate;
    }

    @Override
    public boolean equals(ItemStack itemStack) {
        return this.delegate.isItemEqual(itemStack);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IAEItemStack stack) {
            ItemStack otherStack = stack.getCachedItemStack(stack.getStackSize());
            NBTTagCompound thisTag = delegate.getTagCompound();
            NBTTagCompound otherTag = otherStack.getTagCompound();

            boolean nbtMatch;
            if (thisTag == null) {
                nbtMatch = otherTag == null;
            } else {
                // noinspection PointlessNullCheck
                nbtMatch = otherTag != null && thisTag.equals(otherTag);
            }

            return this.delegate.isItemEqual(otherStack) && nbtMatch;
        } else if (other instanceof ItemStack itemStack) {
            return this.equals(itemStack);
        }

        return false;
    }

    @Override
    public boolean delegateAndSizeEqual(@Nullable IWrappedStack<IAEItemStack, ItemStack> wrappedStack) {
        if (wrappedStack == null) return false;
        return delegate.isItemEqual(wrappedStack.getDefinition()) && stackSize == wrappedStack.getStackSize();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.delegate.getItem().hashCode();
        result = 31 * result + this.delegate.getItemDamage();
        result = 31 * result + (this.delegate.getTagCompound() == null ? 0 : this.delegate.getTagCompound().hashCode());
        return result;
    }

    @Override
    public ItemStack getCachedItemStack(long l) {
        ItemStack copy = this.delegate.copy();
        copy.setCount((int) l);
        return copy;
    }

    @Override
    public void setCachedItemStack(ItemStack itemStack) {
        this.delegate = itemStack;
    }

    @Override
    public String toString() {
        return String.format("Wrapped: %s, Stack Size: %d", delegate, stackSize);
    }
}
