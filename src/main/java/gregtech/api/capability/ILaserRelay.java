package gregtech.api.capability;

public interface ILaserRelay {

    /**
     * Receive a laser pulse.
     * 
     * @param laserVoltage  the voltage of the laser.
     * @param laserAmperage the amperage of the laser.
     * @return how much amperage was received.
     */
    long receiveLaser(long laserVoltage, long laserAmperage);
}
