package gregtech.api.capability;

public interface IGhostSlotConfigurable {

    /**
     * @return if there is a ghost circuit inventory
     */
    boolean hasGhostCircuitInventory();

    /**
     * Set ghost circuit config to given value. If the provided config value is outside of valid config range
     * (0~32), then the circuit is set to empty.
     * <p>
     * If the machine does not have circuit inventory, this method does nothing.
     *
     * @param config New config value
     */
    void setGhostCircuitConfig(int config);
}
