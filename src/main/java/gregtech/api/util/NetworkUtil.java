package gregtech.api.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class NetworkUtil {

    /**
     * Write the NBT of a {@link FluidStack} to a {@link PacketBuffer}.
     * 
     * @param to    the buffer to write to
     * @param stack the stack to write
     */
    public static void writeFluidStack(@NotNull PacketBuffer to, @NotNull FluidStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        stack.writeToNBT(tag);
        to.writeCompoundTag(tag);
    }

    /**
     * {@link #writeFluidStack(PacketBuffer, FluidStack)} but with a netty {@link ByteBuf}
     * 
     * @param to    the buffer to write to
     * @param stack the stack to write
     */
    public static void writeFluidStack(@NotNull ByteBuf to, @NotNull FluidStack stack) {
        writeFluidStack(new PacketBuffer(to), stack);
    }

    /**
     * Read a {@link FluidStack} from a {@link PacketBuffer}
     * 
     * @param from the packet buffer to read from
     * @return the decoded fluid stack
     */
    public static @Nullable FluidStack readFluidStack(@NotNull PacketBuffer from) {
        NBTTagCompound tag;
        try {
            tag = from.readCompoundTag();
        } catch (IOException e) {
            GTLog.logger.error("Exception reading the tag compound from the packet buffer!", e);
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(tag);
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
}
