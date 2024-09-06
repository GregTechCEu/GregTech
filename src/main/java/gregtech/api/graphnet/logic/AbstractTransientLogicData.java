package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTransientLogicData<T extends AbstractTransientLogicData<T>>
                                                extends NetLogicEntry<T, NBTBase> {

    protected AbstractTransientLogicData(@NotNull String name) {
        super(name);
    }

    @Override
    public final void deserializeNBT(NBTBase nbt) {}

    @Override
    public final @Nullable NBTBase serializeNBT() {
        return null;
    }

    @Override
    public boolean shouldEncode() {
        return false;
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {}

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {}
}
