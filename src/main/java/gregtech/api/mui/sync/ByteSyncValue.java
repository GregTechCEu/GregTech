package gregtech.api.mui.sync;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ByteSyncValue extends ValueSyncHandler<Byte> implements IStringSyncValue<Byte> {

    private byte cache;
    private final ByteSupplier getter;
    private final ByteSetter setter;

    public ByteSyncValue(@NotNull ByteSupplier getter, @Nullable ByteSetter setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public ByteSyncValue(@NotNull ByteSupplier getter) {
        this(getter, null);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setByteValue(Byte.parseByte(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(cache);
    }

    @Override
    public void setValue(Byte value, boolean setSource, boolean sync) {
        setByteValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getByte() != this.cache) {
            setByteValue(this.getter.getByte(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeByte(this.cache);
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        setByteValue(buffer.readByte(), true, false);
    }

    public Byte getValue() {
        return cache;
    }

    public byte getByteValue() {
        return cache;
    }

    public void setByteValue(byte value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.set(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    public interface ByteSupplier {

        byte getByte();
    }

    public interface ByteSetter {

        void set(byte b);
    }
}
