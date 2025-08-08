package gregtech.api.mui;

import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.util.NetworkUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.utils.serialization.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

public class GTByteBufAdapters {

    public static final IByteBufAdapter<ChancedItemOutput> CHANCED_ITEM_OUTPUT = makeAdapter(
            ChancedItemOutput::fromBuffer, ChancedItemOutput::toBuffer);

    public static final IByteBufAdapter<ChancedFluidOutput> CHANCED_FLUID_OUTPUT = makeAdapter(
            ChancedFluidOutput::fromBuffer, ChancedFluidOutput::toBuffer);

    public static final IByteBufAdapter<BigInteger> BIG_INT = makeAdapter(
            buffer -> new BigInteger(buffer.readByteArray()),
            (buffer, value) -> buffer.writeByteArray(value.toByteArray()));

    public static final IByteBufAdapter<Fluid> FLUID = makeAdapter(NetworkUtil::readFluid, NetworkUtil::writeFluid);

    public static final IByteBufAdapter<IAEItemStack> WRAPPED_ITEM_STACK = makeAdapter(WrappedItemStack::fromPacket,
            (buffer, value) -> value.writeToPacket(buffer));

    public static final IByteBufAdapter<IAEFluidStack> WRAPPED_FLUID_STACK = makeAdapter(
            WrappedFluidStack::fromPacket, (buffer, value) -> value.writeToPacket(buffer));

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                     @NotNull IByteBufSerializer<T> serializer) {
        return makeAdapter(deserializer, serializer, IEquals.defaultTester());
    }

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer,
                                                     @NotNull IByteBufSerializer<T> serializer,
                                                     @NotNull IEquals<T> equals) {
        final IEquals<T> tester = Objects.requireNonNull(equals);
        return new IByteBufAdapter<>() {

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
