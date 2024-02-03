package gregtech.api.capability;

public interface IParallelMultiblock {

    /**
     *
     * @return whether the multiblock can use parallel recipes
     */
    default boolean isParallel() {
        return false;
    }

    /**
     *
     * @return the maximum amount of parallel recipes the multiblock can use
     */
    default int getMaxParallel() {
        return 1;
    }
}
