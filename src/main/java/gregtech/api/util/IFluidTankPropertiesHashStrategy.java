package gregtech.api.util;

import it.unimi.dsi.fastutil.Hash;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.Objects;

public interface IFluidTankPropertiesHashStrategy extends Hash.Strategy<IFluidTankProperties> {

    @Nonnull
    static IFluidTankPropertiesHashStrategy create() {
        return new IFluidTankPropertiesHashStrategy() {
            @Override
            public int hashCode(IFluidTankProperties o) {
                int result = 17;
                result = 31 * result + (o.getContents() == null ? 0 : o.getContents().hashCode());
                result = 31 * result + o.getCapacity();
                result = 31 * result + (o.canFill() ? 1 : 0);
                result = 31 * result + (o.canDrain() ? 1 : 0);
                return result;
            }

            @Override
            public boolean equals(IFluidTankProperties a, IFluidTankProperties b) {
                if (a == b) return true;
                if (b == null || a.getClass() != b.getClass()) return false;
                return a.getCapacity() == b.getCapacity() && a.canFill() == b.canFill() &&
                        a.canDrain() == b.canDrain() && Objects.equals(a.getContents(), b.getContents());

            }
        };
    }
}
