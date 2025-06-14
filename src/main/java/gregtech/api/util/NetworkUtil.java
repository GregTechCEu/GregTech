package gregtech.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class NetworkUtil {

    /**
     * Write a {@link FluidStack} to a {@link PacketBuffer}.
     *
     * @param to    the buffer to write to
     * @param stack the stack to write
     */
    public static void writeFluidStack(@NotNull PacketBuffer to, @Nullable FluidStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack == null) {
            to.writeBoolean(false);
        } else {
            to.writeBoolean(true);
            stack.writeToNBT(tag);
        }
        to.writeCompoundTag(tag);
    }

    /**
     * {@link #writeFluidStack(PacketBuffer, FluidStack)} but with a netty {@link ByteBuf}
     *
     * @param to    the buffer to write to
     * @param stack the stack to write
     */
    public static void writeFluidStack(@NotNull ByteBuf to, @Nullable FluidStack stack) {
        writeFluidStack(new PacketBuffer(to), stack);
    }

    /**
     * Read a {@link FluidStack} from a {@link PacketBuffer}
     *
     * @param from the packet buffer to read from
     * @return the decoded fluid stack
     */
    public static @Nullable FluidStack readFluidStack(@NotNull PacketBuffer from) {
        if (from.readBoolean()) {
            NBTTagCompound tag;
            try {
                tag = from.readCompoundTag();
            } catch (IOException e) {
                GTLog.logger.error("Exception reading a FluidStack from a PacketBuffer!", e);
                return null;
            }
            return FluidStack.loadFluidStackFromNBT(tag);
        } else {
            return null;
        }
    }

    /**
     * {@link #readFluidStack(PacketBuffer)} but with a netty {@link ByteBuf}
     *
     * @param from the packet buffer to read from
     * @return the decoded fluid stack
     */
    public static @Nullable FluidStack readFluidStack(@NotNull ByteBuf from) {
        return readFluidStack(new PacketBuffer(from));
    }

    /**
     * Write a {@link Fluid} to a {@link PacketBuffer}.
     *
     * @param to    the buffer to write to
     * @param fluid the fluid to write
     */
    public static void writeFluid(@NotNull PacketBuffer to, @Nullable Fluid fluid) {
        if (fluid == null) {
            to.writeBoolean(false);
        } else {
            to.writeBoolean(true);
            to.writeString(fluid.getName());
        }
    }

    /**
     * {@link #writeFluid(PacketBuffer, Fluid)} but with a netty {@link ByteBuf}
     *
     * @param to    the buffer to write to
     * @param fluid the fluid to write
     */
    public static void writeFluid(@NotNull ByteBuf to, @Nullable Fluid fluid) {
        writeFluid(new PacketBuffer(to), fluid);
    }

    /**
     * Read a {@link Fluid} from a {@link PacketBuffer}
     *
     * @param from the packet buffer to read from
     * @return the decoded fluid
     */
    public static @Nullable Fluid readFluid(@NotNull PacketBuffer from) {
        if (from.readBoolean()) {
            return FluidRegistry.getFluid(from.readString(Short.MAX_VALUE));
        } else {
            return null;
        }
    }

    /**
     * {@link #readFluid(PacketBuffer)} but with a netty {@link ByteBuf}
     *
     * @param from the packet buffer to read from
     * @return the decoded fluid
     */
    public static @Nullable Fluid readFluid(@NotNull ByteBuf from) {
        return readFluid(new PacketBuffer(from));
    }

    /**
     * Write an {@link ItemStack} to a {@link PacketBuffer}.
     *
     * @param to    the buffer to write to
     * @param stack the stack to write
     */
    public static void writeItemStack(@NotNull PacketBuffer to, @NotNull ItemStack stack) {
        to.writeItemStack(stack);
    }

    /**
     * {@link #writeItemStack(PacketBuffer, ItemStack)} but with a netty {@link ByteBuf}
     *
     * @param to    the buffer to write to
     * @param stack the stack to write
     */
    public static void writeItemStack(@NotNull ByteBuf to, @NotNull ItemStack stack) {
        writeItemStack(new PacketBuffer(to), stack);
    }

    /**
     * Read an {@link ItemStack} from a {@link PacketBuffer}
     *
     * @param from the packet buffer to read from
     * @return the decoded item stack
     */
    public static @NotNull ItemStack readItemStack(@NotNull PacketBuffer from) {
        try {
            return from.readItemStack();
        } catch (IOException e) {
            GTLog.logger.error("Exception reading an ItemStack from a PacketBuffer!", e);
            return ItemStack.EMPTY;
        }
    }

    /**
     * {@link #readItemStack(PacketBuffer)} but with a netty {@link ByteBuf}
     *
     * @param from the buffer to read from
     * @return the decoded item stack
     */
    public static @NotNull ItemStack readItemStack(@NotNull ByteBuf from) {
        return readItemStack(new PacketBuffer(from));
    }
}
