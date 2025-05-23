package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

class IAEStackByteBufAdapter<T extends IAEStack<T>> implements IByteBufAdapter<T> {

    public static final IAEStackByteBufAdapter<IAEItemStack> wrappedItemStackAdapter = specificDeserializer(
            WrappedItemStack::fromPacket);
    public static final IAEStackByteBufAdapter<IAEFluidStack> wrappedFluidStackAdapter = specificDeserializer(
            WrappedFluidStack::fromPacket);

    @NotNull
    private final IByteBufSerializer<T> serializer;
    @NotNull
    private final IByteBufDeserializer<T> deserializer;

    private IAEStackByteBufAdapter(@NotNull IByteBufSerializer<T> serializer,
                                   @NotNull IByteBufDeserializer<T> deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public static <
            T extends IAEStack<T>> IAEStackByteBufAdapter<T> createAdapter(@NotNull IByteBufSerializer<T> serializer,
                                                                           @NotNull IByteBufDeserializer<T> deserializer) {
        return new IAEStackByteBufAdapter<>(serializer, deserializer);
    }

    public static <
            T extends IAEStack<T>> IAEStackByteBufAdapter<T> specificDeserializer(@NotNull IByteBufDeserializer<T> deserializer) {
        return createAdapter((buf, stack) -> stack.writeToPacket(buf), deserializer);
    }

    @Override
    public T deserialize(PacketBuffer buffer) throws IOException {
        return deserializer.deserialize(buffer);
    }

    @Override
    public void serialize(PacketBuffer buffer, T stackToSerialize) throws IOException {
        serializer.serialize(buffer, stackToSerialize);
    }

    @Override
    public boolean areEqual(@NotNull T t1, @NotNull T t2) {
        return Objects.equals(t1, t2);
    }
}
