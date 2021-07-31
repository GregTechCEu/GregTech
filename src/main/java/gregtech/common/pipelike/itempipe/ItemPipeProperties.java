package gregtech.common.pipelike.itempipe;

import java.util.Objects;

public class ItemPipeProperties {

    /**
     * Items will try to take the path with the lowest priority
     */
    public final int priority;

    /**
     * rate in stacks per sec
     */
    public final float transferRate;

    public ItemPipeProperties(int priority, float transferRate) {
        this.priority = priority;
        this.transferRate = transferRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPipeProperties that = (ItemPipeProperties) o;
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
