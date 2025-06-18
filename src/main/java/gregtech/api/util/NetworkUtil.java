package gregtech.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

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
     * Write an {@link ItemStack} to a {@link PacketBuffer}.
     *
     * @param to    the buffer to write to
     * @param stack the stack to write
     */
    public static void writeItemStack(@NotNull PacketBuffer to, @NotNull ItemStack stack) {
        to.writeItemStack(stack);
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
}
