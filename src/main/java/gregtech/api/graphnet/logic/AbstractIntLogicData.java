package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;

import org.jetbrains.annotations.Contract;

public abstract class AbstractIntLogicData<T extends AbstractIntLogicData<T>> implements INetLogicEntry<T, NBTTagInt> {

    private int value;

    @Contract("_ -> this")
    public T setValue(int value) {
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
}
