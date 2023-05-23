package gregtech.api.capability;

import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Interface for objects associated with {@link IFilter}.
 */
public interface IFiltered {

    /**
     * Compare logic for filtered instances.
     */
    Comparator<IFiltered> COMPARATOR = Comparator.nullsLast(
            Comparator.comparing(IFiltered::getFilter, IFilter.FILTER_COMPARATOR)
    );

    /**
     * @return instance of {@link IFilter} associated to this object, or {@code null} if there's no filter
     * associated.
     */
    @Nullable
    IFilter<FluidStack> getFilter();
}
