package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractIntLogicData<T extends AbstractIntLogicData<T>> extends NetLogicEntry<T, NBTTagInt> {

    private int value;

    protected AbstractIntLogicData() {}

    protected AbstractIntLogicData(int init) {
        this.value = init;
    }

    protected T setValue(int value) {
        this.value = value;
        return (T) this;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public NBTTagInt serializeNBT() {
        return new NBTTagInt(this.value);
    }

    @Override
    public void deserializeNBT(NBTTagInt nbt) {
        this.value = nbt.getInt();
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeVarInt(this.value);
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.value = buf.readVarInt();
    }

    @Override
    public abstract @NotNull IntLogicType<T> getType();

    public static class IntLogicType<T extends AbstractIntLogicData<T>> extends NetLogicType<T> {

        public IntLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                            @NotNull T defaultable) {
            super(name, supplier, defaultable);
        }

        public IntLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                            @NotNull T defaultable) {
            super(namespace, name, supplier, defaultable);
        }

        public T getWith(int value) {
            return getNew().setValue(value);
        }
    }
}
