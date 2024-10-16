package gregtech.api.mui.sync;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NBTSyncValue extends ValueSyncHandler<NBTTagCompound> {

    private final Supplier<NBTTagCompound> getter;
    private final Consumer<NBTTagCompound> setter;
    private NBTTagCompound cache;

    public NBTSyncValue(Supplier<NBTTagCompound> getter, Consumer<NBTTagCompound> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public void setValue(NBTTagCompound value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || !Objects.equals(this.getter.get(), this.cache)) {
            setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeCompoundTag(getValue());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        setValue(buffer.readCompoundTag());
    }

    @Override
    public NBTTagCompound getValue() {
        return this.cache;
    }
}
