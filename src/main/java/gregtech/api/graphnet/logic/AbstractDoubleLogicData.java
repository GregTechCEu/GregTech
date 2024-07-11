package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;

import org.jetbrains.annotations.Contract;

public abstract class AbstractDoubleLogicData<T extends AbstractDoubleLogicData<T>> implements INetLogicEntry<T, NBTTagDouble> {

    private double value;

    @Contract("_ -> this")
    public T setValue(double value) {
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
}
