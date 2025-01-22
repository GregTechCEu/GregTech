package gregtech.api.network;

import net.minecraft.network.PacketBuffer;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

public class AdvancedPacketBuffer extends PacketBuffer {

    private final @NotNull Supplier<ByteBuf> subBufferCreator;
    private @Nullable AdvancedPacketBuffer subBuffer;

    public AdvancedPacketBuffer(@NotNull Supplier<ByteBuf> subBufferCreator) {
        super(subBufferCreator.get());
        this.subBufferCreator = subBufferCreator;
    }

    public AdvancedPacketBuffer(ByteBuf wrapped, @NotNull Supplier<ByteBuf> subBufferCreator) {
        super(wrapped);
        this.subBufferCreator = subBufferCreator;
    }

    /**
     * Opens a new sub buffer to write to. Writes any already open sub buffer to this buffer before closing it.
     * 
     * @return the new sub buffer
     */
    public AdvancedPacketBuffer openSubBuffer() {
        return openSubBuffer(true);
    }

    /**
     * Opens a new sub buffer to write to.
     * 
     * @param writeOld whether any already open buffer should be written to this buffer in the process of closing it.
     * @return the new sub buffer
     */
    public AdvancedPacketBuffer openSubBuffer(boolean writeOld) {
        if (writeOld) writeSubBuffer();
        else closeSubBuffer();
        subBuffer = new AdvancedPacketBuffer(subBufferCreator.get(), subBufferCreator);
        return subBuffer;
    }

    /**
     * Writes the open sub buffer to this buffer, then closes it. Does nothing if no sub buffer is open.
     */
    public void writeSubBuffer() {
        if (subBuffer == null) return;
        subBuffer.writeSubBuffer();
        this.writeByteArray(Arrays.copyOfRange(subBuffer.array(), 0, subBuffer.writerIndex()));
        subBuffer = null;
    }

    /**
     * Reads a sub buffer from this buffer, then makes it the currently open sub buffer.
     * Closes any old open sub buffer in the process without writing it.
     * 
     * @return the newly read sub buffer.
     */
    public AdvancedPacketBuffer readSubBuffer() {
        return readSubBuffer(false);
    }

    /**
     * Reads a sub buffer from this buffer, then makes it the currently open sub buffer.
     * 
     * @param writeOld whether any already open buffer should be written to this buffer in the process of closing it.
     * @return the newly read sub buffer.
     */
    public AdvancedPacketBuffer readSubBuffer(boolean writeOld) {
        if (writeOld) writeSubBuffer();
        else closeSubBuffer();
        ByteBuf backer = subBufferCreator.get();
        backer.writeBytes(this.readByteArray());
        subBuffer = new AdvancedPacketBuffer(backer, subBufferCreator);
        return subBuffer;
    }

    public void closeSubBuffer() {
        subBuffer = null;
    }

    /**
     * Gets the currently open sub buffer, or null if none is open.
     */
    public @Nullable AdvancedPacketBuffer getSubBuffer() {
        return subBuffer;
    }
}
