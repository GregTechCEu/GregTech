package gregtech.api.graphnet.pipenet.predicate;

import gregtech.api.graphnet.predicate.IEdgePredicate;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.NBTTagString;

import org.jetbrains.annotations.NotNull;

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
        return new NBTTagString();
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
