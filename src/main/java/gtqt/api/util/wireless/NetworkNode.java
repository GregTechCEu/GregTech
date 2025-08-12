package gtqt.api.util.wireless;

import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;

import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityWirelessController;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NetworkNode {
    private final UUID ownerUUID;
    //private final int networkID;
    private String networkName;
    private BigInteger energy;
    private boolean isOpen;

    public NetworkNode(UUID owner,  String name) {
        this.ownerUUID = owner;
        //this.networkID = id;
        this.networkName = name;
        this.energy = BigInteger.ZERO;
        this.isOpen=true;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

//    public int getNetworkID() {
//        return networkID;
//    }

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

    /// ///////////////////////////////////////////////////////////////////////////////////////
    /// 新的能源网络只存储 hatches
    /// 在每次sort时同步刷新有效性与状态（电量 优先级）
    /// ///////////////////////////////////////////////////////////////////////////////////////
    List<MetaTileEntityWirelessController> hatches;

    // 获取按优先级升序排序的hatch列表（优先级数值小的在前）
    // 移除无效仓室（内部方法）
    private synchronized void removeInvalidHatches() {
        //检查多方块情况 尤其是不成形的时候应该移除
        hatches.removeIf(hatch -> hatch == null || !hatch.getController().isStructureFormed());
    }

    // 获取排序后的仓室列表（带有效性检查）
    private synchronized List<MetaTileEntityWirelessController> getSortedHatches() {
        removeInvalidHatches(); // 先清理无效仓室

        List<MetaTileEntityWirelessController> sorted = new ArrayList<>(hatches);
        sorted.sort(Comparator.comparingInt(MetaTileEntityWirelessController::getPriority)); // 升序排序
        return sorted;
    }

    public long fill(long amount) {
        if (amount <= 0) return 0;
        long remaining = amount;
        for (MetaTileEntityWirelessController hatch : getSortedHatches()) {
            if (remaining <= 0) break; // 能量已填满，提前退出
            long filled = hatch.fill(remaining); // 尝试填充
            remaining -= filled; // 减去已填充量
        }
        return amount - remaining; // 返回实际填充总量
    }

    public long drain(long amount) {
        if (amount <= 0) return 0;
        long remaining = amount;
        for (MetaTileEntityWirelessController hatch : getSortedHatches()) {
            if (remaining <= 0) break; // 能量已抽够，提前退出
            long drained = hatch.drain(remaining); // 尝试抽取
            remaining -= drained; // 减去已抽取量
        }
        return amount - remaining; // 返回实际抽取总量
    }

    public BigInteger getTotalCapacity() {
        BigInteger total = BigInteger.ZERO;
        for (MetaTileEntityWirelessController hatch : hatches) {
            total = total.add(hatch.getCapacity()); // 累加每个hatch的容量
        }
        return total;
    }

    public BigInteger getTotalStored() {
        BigInteger total = BigInteger.ZERO;
        for (MetaTileEntityWirelessController hatch : hatches) {
            total = total.add(hatch.getStored()); // 累加每个hatch的存储量
        }
        return total;
    }

    public synchronized void addNewHatch(MetaTileEntityWirelessController metaTileEntityWirelessController) {
        if (hatches.contains(metaTileEntityWirelessController))return;
        hatches.add(metaTileEntityWirelessController);
    }

    public synchronized void removeHatch(MetaTileEntityWirelessController metaTileEntityWirelessController) {
        hatches.remove(metaTileEntityWirelessController);
    }
}
