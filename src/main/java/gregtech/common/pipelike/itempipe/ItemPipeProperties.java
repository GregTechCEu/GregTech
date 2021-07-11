package gregtech.common.pipelike.itempipe;

import java.util.Objects;

public class ItemPipeProperties {

    /**
     * range in blocks
     */
    public final int maxRange;
    /**
     * rate in stacks per sec
     */
    public final float transferRate;

    public ItemPipeProperties(int maxRange, float transferRate) {
        this.maxRange = maxRange;
        this.transferRate = transferRate;
    }

    public static ItemPipeProperties min(ItemPipeProperties a, ItemPipeProperties b) {
        if(a == null) {
            if(b == null)
                throw new NullPointerException("Properties are null");
            return b;
        }
        if(b == null)
            return a;
        return new ItemPipeProperties(Math.min(a.maxRange, b.maxRange), Math.min(a.transferRate, b.transferRate));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPipeProperties that = (ItemPipeProperties) o;
        return maxRange == that.maxRange && Float.compare(that.transferRate, transferRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxRange, transferRate);
    }

    @Override
    public String toString() {
        return "ItemPipeProperties{" +
                "maxRange=" + maxRange +
                ", transferRate=" + transferRate +
                '}';
    }
}
