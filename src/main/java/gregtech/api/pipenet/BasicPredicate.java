package gregtech.api.pipenet;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class BasicPredicate extends AbstractEdgePredicate<BasicPredicate> {

    static {
        PREDICATES.put("Basic", new BasicPredicate());
    }

    protected boolean shutteredSource;
    protected boolean shutteredTarget;

    public void setShutteredSource(boolean shutteredSource) {
        this.shutteredSource = shutteredSource;
    }

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
    public @NotNull BasicPredicate createPredicate() {
        return new BasicPredicate();
    }

    @Override
    protected String predicateType() {
        return "Basic";
    }
}
