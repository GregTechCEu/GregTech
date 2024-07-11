package gregtech.api.graphnet;

public abstract class AbstractGroupData {

    protected NetGroup<?, ?, ?> group;

    public void withGroup(NetGroup<?, ?, ?> group) {
        this.group = group;
    }
}
