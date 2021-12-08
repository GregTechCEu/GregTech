package gregtech.api.capability;

public interface IVentable {

    /**
     *
     * @return whether the machine needs to be vented
     */
    boolean isNeedsVenting();

    /**
     * used to perform venting
     */
    void tryDoVenting();

    /**
     *
     * @return whether venting is stuck
     */
    boolean isVentingStuck();

    void setNeedsVenting(boolean needsVenting);
}
