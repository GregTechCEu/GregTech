package gregtech.api.pipenet;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class BasicEdgePredicate extends AbstractEdgePredicate<BasicEdgePredicate> implements IShutteredEdgePredicate {

    static {
        PREDICATES.put("Basic", new BasicEdgePredicate());
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
    public boolean test(Object o) {
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
    public void deserializeNBT(NBTTagCompound nbt) {
        shutteredSource = nbt.getBoolean("ShutteredSource");
        shutteredTarget = nbt.getBoolean("ShutteredTarget");
    }

    @Override
    public @NotNull BasicEdgePredicate createPredicate() {
        return new BasicEdgePredicate();
    }

    @Override
    protected String predicateType() {
        return "Basic";
    }
}
