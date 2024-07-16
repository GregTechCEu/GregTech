package gregtech.api.graphnet.predicate;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import gregtech.common.covers.CoverShutter;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;

import net.minecraft.nbt.NBTTagString;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ShutterPredicate implements IEdgePredicate<ShutterPredicate, NBTTagString> {

    public static final ShutterPredicate INSTANCE = new ShutterPredicate();

    private ShutterPredicate() {}

    @Override
    @Deprecated
    public ShutterPredicate getNew() {
        return INSTANCE;
    }

    @Override
    public @NotNull String getName() {
        return "Shuttered";
    }

    @Override
    public NBTTagString serializeNBT() {
        return new NBTTagString("");
    }

    @Override
    public void deserializeNBT(NBTTagString nbt) {}

    @Override
    public boolean andy() {
        return true;
    }

    @Override
    public boolean test(IPredicateTestObject object) {
        return false;
    }
}
