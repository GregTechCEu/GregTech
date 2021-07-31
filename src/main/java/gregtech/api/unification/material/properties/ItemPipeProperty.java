package gregtech.api.unification.material.properties;

import java.util.Objects;

public class ItemPipeProperty implements IMaterialProperty {

    /**
     * Items will try to take the path with the lowest priority
     */
    public final int priority;

    /**
     * rate in stacks per sec
     */
    public final float transferRate;

    public ItemPipeProperty(int priority, float transferRate) {
        this.priority = priority;
        this.transferRate = transferRate;
    }

    /**
     * Default property constructor.
     */
    public ItemPipeProperty() {
        this(1, 0.25f);
    }

    @Override
    public void verifyProperty(Properties properties) {
        if (properties.getIngotProperty() == null) {
            properties.setIngotProperty(new IngotProperty());
            properties.verify();
        }
    }

    @Override
    public boolean doesMatch(IMaterialProperty otherProp) {
        return otherProp instanceof ItemPipeProperty;
    }

    @Override
    public String getName() {
        return "item_pipe_property";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPipeProperty that = (ItemPipeProperty) o;
        return priority == that.priority && Float.compare(that.transferRate, transferRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, transferRate);
    }

    @Override
    public String toString() {
        return "ItemPipeProperties{" +
                "priority=" + priority +
                ", transferRate=" + transferRate +
                '}';
    }
}
