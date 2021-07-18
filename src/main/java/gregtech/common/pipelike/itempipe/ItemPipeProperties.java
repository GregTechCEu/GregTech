package gregtech.common.pipelike.itempipe;

import java.util.Objects;

public class ItemPipeProperties {

    /**
     * Items will try to take the path with the lowest resistance
     */
    public final int resistance;

    /**
     * rate in stacks per sec
     */
    public final float transferRate;

    public ItemPipeProperties(int resistance, float transferRate) {
        this.resistance = resistance;
        this.transferRate = transferRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPipeProperties that = (ItemPipeProperties) o;
        return resistance == that.resistance && Float.compare(that.transferRate, transferRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resistance, transferRate);
    }

    @Override
    public String toString() {
        return "ItemPipeProperties{" +
                "resistance=" + resistance +
                ", transferRate=" + transferRate +
                '}';
    }
}
