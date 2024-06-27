package gregtech.api.pipenet.predicate;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class BasicEdgePredicate extends AbstractEdgePredicate<BasicEdgePredicate> implements IShutteredEdgePredicate {

    private final static String KEY = "Basic";

    static {
        PREDICATE_SUPPLIERS.put(KEY, BasicEdgePredicate::new);
    }

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
