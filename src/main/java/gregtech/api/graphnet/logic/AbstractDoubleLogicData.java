package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractDoubleLogicData<T extends AbstractDoubleLogicData<T>>
                                             extends NetLogicEntry<T, NBTTagDouble> {

    private double value;

    protected AbstractDoubleLogicData() {}

    protected AbstractDoubleLogicData(double init) {
        this.value = init;
    }

    protected T setValue(double value) {
        this.value = value;
        return (T) this;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public NBTTagDouble serializeNBT() {
        return new NBTTagDouble(this.value);
    }

    @Override
    public void deserializeNBT(NBTTagDouble nbt) {
        this.value = nbt.getDouble();
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeDouble(value);
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.value = buf.readDouble();
    }

    @Override
    public abstract @NotNull DoubleLogicType<T> getType();

    public static class DoubleLogicType<T extends AbstractDoubleLogicData<T>> extends NetLogicType<T> {

        public DoubleLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                               @NotNull T defaultable) {
            super(name, supplier, defaultable);
        }

        public DoubleLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                               @NotNull T defaultable) {
            super(namespace, name, supplier, defaultable);
        }

        public T getWith(double value) {
            return getNew().setValue(value);
        }
    }
}
