package gregtech.api.graphnet.pipenet.predicate;

import gregtech.api.GTValues;
import gregtech.api.graphnet.predicate.EdgePredicate;
import gregtech.api.graphnet.predicate.NetPredicateType;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.NBTTagByte;

import org.jetbrains.annotations.NotNull;

public final class BlockedPredicate extends EdgePredicate<BlockedPredicate, NBTTagByte> {

    private static final BlockedPredicate INSTANCE = new BlockedPredicate();

    public static final NetPredicateType<BlockedPredicate> TYPE = new NetPredicateType<>(GTValues.MODID, "Blocked",
            () -> INSTANCE, INSTANCE);

    @Override
    public @NotNull NetPredicateType<BlockedPredicate> getType() {
        return TYPE;
    }

    @Override
    public NBTTagByte serializeNBT() {
        return new NBTTagByte((byte) 0);
    }

    @Override
    public void deserializeNBT(NBTTagByte nbt) {}

    @Override
    public boolean andy() {
        return true;
    }

    @Override
    public boolean test(IPredicateTestObject object) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockedPredicate;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
