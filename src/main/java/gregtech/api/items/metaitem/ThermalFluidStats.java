package gregtech.api.items.metaitem;

/**
 * @deprecated use {@link FilteredFluidStats#FilteredFluidStats(int, int, boolean, boolean, boolean, boolean, boolean)}
 */
@Deprecated
public class ThermalFluidStats extends FilteredFluidStats {
    public ThermalFluidStats(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        super(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
    }
}
