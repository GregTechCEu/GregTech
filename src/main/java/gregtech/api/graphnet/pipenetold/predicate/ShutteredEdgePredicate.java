package gregtech.api.graphnet.pipenetold.predicate;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class ShutteredEdgePredicate extends IEdgePredicateOld<ShutteredEdgePredicate>
        implements IShutteredEdgePredicate {

    public final static String KEY = "Basic";

    protected boolean shutteredSource;
    protected boolean shutteredTarget;

    @Override
    public void setShutteredSource(boolean shutteredSource) {
        this.shutteredSource = shutteredSource;
    }

    @Override
    public void setShutteredTarget(boolean shutteredTarget) {
        this.shutteredTarget = shutteredTarget;
    }

    @Override
    public boolean test(IPredicateTestObject o) {
        return !(shutteredSource || shutteredTarget);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (shutteredSource) tag.setBoolean("ShutteredSource", true);
        if (shutteredTarget) tag.setBoolean("ShutteredTarget", true);
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound nbt) {
        shutteredSource = nbt.getBoolean("ShutteredSource");
        shutteredTarget = nbt.getBoolean("ShutteredTarget");
    }

    @Override
    protected String predicateName() {
        return KEY;
    }
}
