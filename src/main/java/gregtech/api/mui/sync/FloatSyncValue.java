package gregtech.api.mui.sync;

import gregtech.api.util.function.FloatConsumer;
import gregtech.api.util.function.FloatSupplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class FloatSyncValue extends ValueSyncHandler<Float> implements IFloatSyncValue<Float>, IStringSyncValue<Float> {

    @NotNull
    private final FloatSupplier getter;
    @Nullable
    private final FloatConsumer setter;
    private float cache;

    public FloatSyncValue(@NotNull FloatSupplier getter, @Nullable FloatConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getAsFloat();
    }

    public FloatSyncValue(@NotNull FloatSupplier getter) {
        this(getter, null);
    }

    public FloatSyncValue(@Nullable FloatSupplier clientGetter, @Nullable FloatConsumer clientSetter,
                          @Nullable FloatSupplier serverGetter, @Nullable FloatConsumer serverSetter) {
        if (clientGetter == null && serverGetter == null) {
            throw new NullPointerException("Client or server getter must not be null!");
        }

        if (NetworkUtils.isClient()) {
            this.getter = clientGetter != null ? clientGetter : serverGetter;
            this.setter = clientSetter != null ? clientSetter : serverSetter;
        } else {
            this.getter = serverGetter != null ? serverGetter : clientGetter;
            this.setter = serverSetter != null ? serverSetter : clientSetter;
        }

        this.cache = this.getter.getAsFloat();
    }

    @Override
    public Float getValue() {
        return cache;
    }

    @Override
    public float getFloatValue() {
        return cache;
    }

    @Override
    public void setValue(@NotNull Float value, boolean setSource, boolean sync) {
        setFloatValue(value, setSource, sync);
    }

    @Override
    public void setFloatValue(float value, boolean setSource, boolean sync) {
        cache = value;

        if (setSource && setter != null) {
            setter.apply(value);
        }

        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || getter.getAsFloat() != cache) {
            setFloatValue(getter.getAsFloat(), false, false);
            return false;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeFloat(getFloatValue());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        setFloatValue(buffer.readFloat(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setFloatValue(Float.parseFloat(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(cache);
    }
}
