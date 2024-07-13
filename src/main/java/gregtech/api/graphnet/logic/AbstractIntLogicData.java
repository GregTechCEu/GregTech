package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;

public abstract class AbstractIntLogicData<T extends AbstractIntLogicData<T>> implements INetLogicEntry<T, NBTTagInt> {

    private int value;

    public T getWith(int value) {
        return getNew().setValue(value);
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
}
