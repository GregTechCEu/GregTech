package gregtech.api.mui;

import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class GTByteBufAdapters {

    public static final IByteBufAdapter<ChancedItemOutput> CHANCED_ITEM_OUTPUT = makeAdapter(
            ChancedItemOutput::fromBuffer, ChancedItemOutput::toBuffer);

    public static final IByteBufAdapter<ChancedFluidOutput> CHANCED_FLUID_OUTPUT = makeAdapter(
            ChancedFluidOutput::fromBuffer, ChancedFluidOutput::toBuffer);

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                     @NotNull IByteBufSerializer<T> serializer) {
        return makeAdapter(deserializer, serializer, Objects::equals);
    }

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                     @NotNull IByteBufSerializer<T> serializer,
                                                     @Nullable IEquals<T> equals) {
        final IEquals<T> tester = equals != null ? equals : IEquals.defaultTester();
        return new IByteBufAdapter<T>() {

            @Override
            public T deserialize(PacketBuffer buffer) throws IOException {
                return deserializer.deserialize(buffer);
            }

            @Override
            public void serialize(PacketBuffer buffer, T u) throws IOException {
                serializer.serialize(buffer, u);
            }

            @Override
            public boolean areEqual(@NotNull T t1, @NotNull T t2) {
                return tester.areEqual(t1, t2);
            }
        };
    }
}
