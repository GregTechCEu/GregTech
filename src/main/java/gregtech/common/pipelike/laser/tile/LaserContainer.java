package gregtech.common.pipelike.laser.tile;
import net.minecraft.util.EnumFacing;
public interface LaserContainer {

    /**
     * @return amount of used amperes. 0 if not accepted anything.
     */
    long acceptLaserFromNetwork(EnumFacing side, long voltage, long parallel);

    boolean inputsLaser(EnumFacing side);

    default boolean outputsLaser(EnumFacing side) {
        return false;
    }

    /**
     * @param differenceAmount amount of energy to add (>0) or remove (<0)
     * @return amount of energy added or removed
     */
    long changeLaser(long differenceAmount);

    /**
     * Adds specified amount of energy to this energy container
     *
     * @param LaserToAdd amount of energy to add
     * @return amount of energy added
     */
    default long addLaser(long LaserToAdd) {
        return changeLaser(LaserToAdd);
    }

    /**
     * Removes specified amount of energy from this energy container
     *
     * @param LaserToRemove amount of energy to remove
     * @return amount of energy removed
     */
    default long removeLaser(long LaserToRemove) {
        return changeLaser(-LaserToRemove);
    }

    default long getLaserCanBeInserted() {
        return getLaserCapacity() - getLaserStored();
    }

    /**
     * Gets the stored electric energy
     */
    long getLaserCapacity();

    /**
     * Gets the largest electric energy capacity
     */
    long getLaserStored();

    /**
     * Gets the amount of energy packets per tick.
     */
    default long getOutputLaser() {
        return 0L;
    }

    /**
     * Gets the output in energy units per energy packet.
     */
    default long getOutputParallel() {
        return 0L;
    }

    /**
     * Gets the amount of energy packets this machine can receive
     */
    long getInputParallel();

    /**
     * Gets the maximum voltage this machine can receive in one energy packet.
     * Overflowing this value will explode machine.
     */
    long getInputLaser();

    default boolean isOneProbeHidden() {
        return false;
    }

}
