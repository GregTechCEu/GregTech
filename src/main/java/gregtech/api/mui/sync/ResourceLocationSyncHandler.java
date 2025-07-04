package gregtech.api.mui.sync;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ResourceLocationSyncHandler extends ValueSyncHandler<ResourceLocation>
                                         implements IStringSyncValue<ResourceLocation> {

    @NotNull
    private final Supplier<@NotNull ResourceLocation> getter;
    @Nullable
    private final Consumer<@NotNull ResourceLocation> setter;
    @NotNull
    private ResourceLocation cache;

    public ResourceLocationSyncHandler(@NotNull Supplier<@NotNull ResourceLocation> getter,
                                       @Nullable Consumer<@NotNull ResourceLocation> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public void setStringValue(@NotNull String value, boolean setSource, boolean sync) {
        setValue(new ResourceLocation(value), setSource, sync);
    }

    @Override
    public @NotNull String getStringValue() {
        return cache.toString();
    }

    @Override
    public void setValue(@NotNull ResourceLocation value, boolean setSource, boolean sync) {
        cache = value;

        if (setSource && setter != null) {
            setter.accept(value);
        }

        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public @NotNull ResourceLocation getValue() {
        return cache;
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        ResourceLocation test = getter.get();
        if (isFirstSync || !Objects.equals(test, cache)) {
            setValue(test, false, false);
            return true;
        }

        return false;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeResourceLocation(cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readResourceLocation(), true, false);
    }
}
