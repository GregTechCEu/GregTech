package gregtech.common.metatileentities.multi.multiblockpart.appeng.stack;

import gregtech.api.util.GTUtility;
import gregtech.api.util.NetworkUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WrappedFluidStack implements IAEFluidStack, IWrappedStack<IAEFluidStack, FluidStack> {

    @NotNull
    private final FluidStack delegate;
    private long stackSize;

    private WrappedFluidStack(@NotNull FluidStack stack, long stackSize) {
        this.delegate = stack;
        this.stackSize = stackSize;
    }

    public static WrappedFluidStack fromFluidStack(@Nullable FluidStack fluidStack) {
        return fluidStack == null ? null : new WrappedFluidStack(fluidStack, fluidStack.amount);
    }

    public static WrappedFluidStack fromFluidStack(@Nullable FluidStack fluidStack, long amount) {
        return fluidStack == null ? null : new WrappedFluidStack(fluidStack, amount);
    }

    public static WrappedFluidStack fromNBT(@NotNull NBTTagCompound data) {
        // Migrate old NBT entries from the old format
        if (data.getBoolean("wrapped")) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("stack"));
            if (stack == null) return null;
            return new WrappedFluidStack(stack, data.getLong("stackSize"));
        } else {
            return fromFluidStack(FluidStack.loadFluidStackFromNBT(data));
        }
    }

    public static WrappedFluidStack fromPacket(@NotNull PacketBuffer buffer) {
        return new WrappedFluidStack(Objects.requireNonNull(NetworkUtil.readFluidStack(buffer)), buffer.readLong());
    }

    @NotNull
    public AEFluidStack getAEStack() {
        AEFluidStack aeFluidStack = AEFluidStack.fromFluidStack(this.delegate);
        aeFluidStack.setStackSize(stackSize);
        return aeFluidStack;
    }

    @Override
    public FluidStack getFluidStack() {
        FluidStack newStack = this.delegate.copy();
        newStack.amount = GTUtility.safeCastLongToInt(stackSize);
        return newStack;
    }

    @Override
    public void add(IAEFluidStack iaeFluidStack) {
        if (equals(iaeFluidStack)) {
            incStackSize(iaeFluidStack.getStackSize());
        }
    }

    @Override
    public long getStackSize() {
        return stackSize;
    }

    @Override
    public IAEFluidStack setStackSize(long newStackSize) {
        this.stackSize = newStackSize;
        return this;
    }

    @Override
    public long getCountRequestable() {
        return 0;
    }

    @Override
    public IAEFluidStack setCountRequestable(long l) {
        return this;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEFluidStack setCraftable(boolean b) {
        return this;
    }

    @Override
    public IAEFluidStack reset() {
        this.delegate.amount = 0;
        this.stackSize = 0L;
        return this;
    }

    @Override
    public boolean isMeaningful() {
        return this.delegate.amount > 0;
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
        nbtTagCompound.setTag("stack", this.delegate.writeToNBT(new NBTTagCompound()));
        nbtTagCompound.setLong("stackSize", this.stackSize);
        nbtTagCompound.setBoolean("wrapped", true);
    }

    public NBTTagCompound serializeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return tag;
    }

    @Override
    public boolean fuzzyComparison(IAEFluidStack stack, FuzzyMode fuzzyMode) {
        return this.delegate.getFluid() == stack.getFluid();
    }

    @Override
    public void writeToPacket(ByteBuf buffer) {
        writeToPacketBuffer(new PacketBuffer(buffer));
    }

    public void writeToPacketBuffer(@NotNull PacketBuffer packetBuffer) {
        NetworkUtil.writeFluidStack(packetBuffer, this.delegate);
        packetBuffer.writeLong(this.stackSize);
    }

    @Override
    public IAEFluidStack copy() {
        return new WrappedFluidStack(delegate.copy(), stackSize);
    }

    @Override
    public @NotNull IAEFluidStack copyAsAEStack() {
        IAEFluidStack stack = AEFluidStack.fromFluidStack(delegate.copy());
        stack.setStackSize(stackSize);
        return stack;
    }

    @Override
    public @NotNull IWrappedStack<IAEFluidStack, FluidStack> copyWrapped() {
        return new WrappedFluidStack(delegate.copy(), stackSize);
    }

    @Override
    public IAEFluidStack empty() {
        IAEFluidStack dup = copy();
        dup.reset();
        return dup;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        return ItemStack.EMPTY;
    }

    @Override
    public Fluid getFluid() {
        return this.delegate.getFluid();
    }

    @Override
    public @NotNull FluidStack getDefinition() {
        delegate.amount = GTUtility.safeCastLongToInt(stackSize);
        return delegate;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof WrappedFluidStack wrappedFluidStack) {
            return wrappedFluidStack.delegate.isFluidEqual(this.delegate);
        } else if (other instanceof AEFluidStack aeFluidStack) {
            // noinspection EqualsBetweenInconvertibleTypes
            return aeFluidStack.equals(delegate);
        } else if (other instanceof FluidStack fluidStack) {
            return fluidStack.isFluidEqual(this.delegate);
        }

        return false;
    }

    @Override
    public boolean delegateAndSizeEqual(@Nullable IWrappedStack<IAEFluidStack, FluidStack> wrappedStack) {
        if (wrappedStack == null) return false;
        return delegate.isFluidEqual(wrappedStack.getDefinition()) && stackSize == wrappedStack.getStackSize();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.delegate.getFluid().hashCode();
        result = 31 * result + (this.delegate.tag == null ? 0 : this.delegate.tag.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format("Wrapped: %s, Stack Size: %d", delegate, stackSize);
    }
}
