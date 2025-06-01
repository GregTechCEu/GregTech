package gtqt.api.util.wireless;

import java.math.BigInteger;
import java.util.UUID;

public class NetworkNode {
    private final UUID ownerUUID;
    private final int networkID;
    private String networkName;
    private BigInteger energy;
    private boolean isOpen;

    public NetworkNode(UUID owner, int id, String name) {
        this.ownerUUID = owner;
        this.networkID = id;
        this.networkName = name;
        this.energy = BigInteger.ZERO;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public int getNetworkID() {
        return networkID;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public BigInteger getEnergy() {
        return energy;
    }

    public void setEnergy(BigInteger energy) {
        this.energy = energy;
    }
    public BigInteger modifyEnergy(BigInteger delta) {
        BigInteger original = this.energy;
        BigInteger newValue = original.add(delta);
        if (newValue.compareTo(BigInteger.ZERO) < 0) {
            this.energy = BigInteger.ZERO;
            return original.negate();
        }
        this.energy = newValue;
        return delta;
    }
}
