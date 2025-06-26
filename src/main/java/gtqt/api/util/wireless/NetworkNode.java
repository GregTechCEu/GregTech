package gtqt.api.util.wireless;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        this.isOpen=true;
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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
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
