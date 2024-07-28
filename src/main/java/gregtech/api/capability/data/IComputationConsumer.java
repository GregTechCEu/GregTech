package gregtech.api.capability.data;

/**
 * Used for {@link gregtech.api.capability.impl.ComputationRecipeLogic}
 */
public interface IComputationConsumer {

    /**
     * Called to supply CWU.
     * 
     * @param requested the requested CWU
     * @param simulate  whether to simulate the request
     * @return the amount of CWU supplied.
     */
    long supplyCWU(long requested, boolean simulate);
}
