package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Collection;

public interface UISyncer {

    IByteBufAdapter<BigInteger> BIG_INT = ByteBufAdapters.makeAdapter(
            buffer -> new BigInteger(buffer.readByteArray()),
            (buffer, value) -> buffer.writeByteArray(value.toByteArray()),
            IEquals.defaultTester());

    boolean syncBoolean(boolean initial);

    int syncInt(int initial);

    long syncLong(long initial);

    default @NotNull String syncString(@NotNull String initial) {
        return syncObject(initial, ByteBufAdapters.STRING);
    }

    byte syncByte(byte initial);

    double syncDouble(double initial);

    float syncFloat(float initial);

    default BigInteger syncBigInt(BigInteger initial) {
        return syncObject(initial, BIG_INT);
    }

    <T> T syncObject(T initial, IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer);

    default <T> T syncObject(T initial, IByteBufAdapter<T> adapter) {
        return syncObject(initial, adapter, adapter);
    }

    <T, C extends Collection<T>> C syncCollection(C initial,
                                                  IByteBufSerializer<T> serializer,
                                                  IByteBufDeserializer<T> deserializer);

    default <T, C extends Collection<T>> C syncCollection(C initial, IByteBufAdapter<T> adapter) {
        return syncCollection(initial, adapter, adapter);
    }

    void readBuffer(ByteBuf buf);

    void writeBuffer(ByteBuf buf);

    boolean hasChanged();
}
