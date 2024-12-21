package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractByteLogicData<T extends AbstractByteLogicData<T>> extends NetLogicEntry<T, NBTTagByte> {

    private byte value;

    protected AbstractByteLogicData() {}

    protected AbstractByteLogicData(byte init) {
        this.value = init;
    }

    protected T setValue(byte value) {
        this.value = value;
        return (T) this;
    }

    public byte getValue() {
        return this.value;
    }

    @Override
    public NBTTagByte serializeNBT() {
        return new NBTTagByte(this.value);
    }

    @Override
    public void deserializeNBT(NBTTagByte nbt) {
        this.value = nbt.getByte();
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeByte(this.value);
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.value = buf.readByte();
    }

    @Override
    public abstract @NotNull AbstractByteLogicData.ByteLogicType<T> getType();

    public static class ByteLogicType<T extends AbstractByteLogicData<T>> extends NetLogicType<T> {

        public ByteLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                             @NotNull T defaultable) {
            super(name, supplier, defaultable);
        }

        public ByteLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                             @NotNull T defaultable) {
            super(namespace, name, supplier, defaultable);
        }

        public T getWith(byte value) {
            return getNew().setValue(value);
        }
    }
}
