package gtqt.api.util.wireless;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkInfo {

    public boolean isGloba;
    public String NetworkName = "null";
    public String NetworkPassword = "";
    public UUID ownerid = null;
    public List<BlockPos> machinepos = new ArrayList<>();
    public BigInteger NetWorkEnergy = BigInteger.valueOf(Integer.MIN_VALUE);

    public NetworkInfo(UUID uid) {
        this.isGloba = false;
        this.ownerid = uid;
    }

    public NetworkInfo() {

    }
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setBoolean("isGloba", this.isGloba);
        tag.setString("NetworkName", this.NetworkName);
        tag.setString("NetworkPassword", this.NetworkPassword);

        if (this.ownerid != null) {
            tag.setString("ownerid", this.ownerid.toString());
        } else {
            tag.setString("ownerid", "");
        }
        NBTTagList posList = new NBTTagList();
        for (BlockPos pos : machinepos) {
            NBTTagCompound posTag = new NBTTagCompound();
            posTag.setInteger("x", pos.getX());
            posTag.setInteger("y", pos.getY());
            posTag.setInteger("z", pos.getZ());
            posList.appendTag(posTag);
        }
        tag.setTag("machinepos", posList);
        tag.setString("NetWorkEnergy", NetWorkEnergy.toString());

        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        this.isGloba = tag.getBoolean("isGloba");
        this.NetworkName = tag.getString("NetworkName");
        this.NetworkPassword = tag.getString("NetworkPassword");

        String ownerStr = tag.getString("ownerid");
        if (!ownerStr.isEmpty()) {
            this.ownerid = UUID.fromString(ownerStr);
        } else {
            this.ownerid = null;
        }
        this.machinepos.clear();
        NBTTagList posList = tag.getTagList("machinepos", 10);
        for (int i = 0; i < posList.tagCount(); i++) {
            NBTTagCompound posTag = posList.getCompoundTagAt(i);
            int x = posTag.getInteger("x");
            int y = posTag.getInteger("y");
            int z = posTag.getInteger("z");
            this.machinepos.add(new BlockPos(x, y, z));
        }
        String energyStr = tag.getString("NetWorkEnergy");
        try {
            this.NetWorkEnergy = new BigInteger(energyStr);
        } catch (Exception e) {
            this.NetWorkEnergy = BigInteger.valueOf(Integer.MIN_VALUE);
        }
    }
}
