package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractLongLogicData<T extends AbstractLongLogicData<T>> extends NetLogicEntry<T, NBTTagLong> {

    private long value;

    @Contract("_ -> this")
    public T setValue(long value) {
        this.value = value;
        return (T) this;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public NBTTagLong serializeNBT() {
        return new NBTTagLong(this.value);
    }

    @Override
    public void deserializeNBT(NBTTagLong nbt) {
        this.value = nbt.getLong();
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeVarLong(this.value);
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.value = buf.readVarLong();
    }

    @Override
    public abstract @NotNull LongLogicType<T> getType();

    public static class LongLogicType<T extends AbstractLongLogicData<T>> extends NetLogicType<T> {

        public LongLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                             @NotNull T defaultable) {
            super(name, supplier, defaultable);
        }

        public LongLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                             @NotNull T defaultable) {
            super(namespace, name, supplier, defaultable);
        }

        public T getWith(long value) {
            return getNew().setValue(value);
        }
    }
}
