package gtqt.common.metatileentities.multi.multiblockpart;

import betterquesting.api.api.QuestingAPI;

import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;

import gregtech.api.capability.IWirelessController;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import gtqt.api.util.wireless.NetworkDatabase;
import gtqt.api.util.wireless.NetworkManager;
import gtqt.api.util.wireless.NetworkNode;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import net.minecraft.world.World;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetaTileEntityWirelessController extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<IWirelessController>,IWirelessController{

    //MetaTileEntityWirelessController是MetaTileEntityPowerSubstation内energyBank的代理

    //优先级 100
    private int priority = 100;

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public void update(){
        super.update();
    }
    @Override
    public void onRemoval() {
        super.onRemoval();
        NetworkNode node = getNetwork();
        if (node != null) {
            node.removeHatch(this);
        }
    }
    public MetaTileEntityWirelessController(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityWirelessController(metaTileEntityId, getTier());
    }
    @Override
    public MultiblockAbility<IWirelessController> getAbility() {
        return  MultiblockAbility.WIRELESS_CONTROLLER;
    }
    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {

    }
    //无线电网操作////////////////////////////////////////////////////////////////////////////
    //对外暴露
    //返回自身即可
    //激活 向NetworkNode网络发送自己
    //多方块成型？UI手动？放置时自动？
    public void sentMTE(){
        NetworkNode node = getNetwork();
        node.addNewHatch(this);
    }


    //
    public NetworkNode getNetwork() {
        var world = this.getWorld();
        if(this.getOwnerGT()!=null) {
            UUID id = this.getOwnerGT();
            // 安全获取网络示例
            NetworkDatabase db = NetworkDatabase.get(world);
            NetworkNode node = db.getNetwork(id);

            if (node == null) {
                NetworkManager.INSTANCE.createNetwork(world,this.getOwnerGT(),"无线网络");
                db = NetworkDatabase.get(world);
                node = db.getNetwork(id);
            }
            return node;
        }
        return null;
    }

    public EntityPlayer getPlayerByGTuuid(UUID GTuuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            return server.getPlayerList().getPlayerByUUID(GTuuid);
        }
        return null;
    }

    public UUID getBQUUID(EntityPlayerMP player){
        return QuestingAPI.getQuestingUUID(player);
    }

    public List<EntityPlayer> getBQGroup(EntityPlayerMP player) {
        UUID BQuuid = getBQUUID(player);
        List<EntityPlayer> group = new ArrayList<>() ;
        DBEntry<IParty> partyEntry = PartyManager.INSTANCE.getParty(BQuuid);
        if (partyEntry != null && player.getServer() != null) {
            for (UUID memID : partyEntry.getValue().getMembers()) {

                EntityPlayerMP memPlayer = player.getServer().getPlayerList()
                        .getPlayerByUsername(NameCache.INSTANCE.getName(memID));
                group.add(memPlayer);
            }
        }
        return group;
    }
    //IO操作////////////////////////////////////////////////////////////////////////////

    //检查容量
    public BigInteger getCapacity() {
        if(getPSS()!=null)
            return getPSS().getEnergyBank().getCapacity();
        return BigInteger.ZERO;
    }

    public BigInteger getStored() {
        if(getPSS()!=null)
            return getPSS().getEnergyBank().getStored();
        return BigInteger.ZERO;
    }

    //fill
    //返回实际填充数值 （如果满了就是溢出的部分）
    public long fill(long amount) {
        return getPSS().getEnergyBank().fill(amount);
    }

    //drain
    //返回实际抽取的量 （如果没那么多就是实际抽取的量）
    public long drain(long amount) {
        return getPSS().getEnergyBank().drain(amount);
    }

    //接口实现////////////////////////////////////////////////////////////////////////////
    @Override
    public MetaTileEntityPowerSubstation.PowerStationEnergyBank getEnergyBank() {
        if(getPSS()!=null)
            return getPSS().getEnergyBank();
        return null;
    }

    public MetaTileEntityPowerSubstation getPSS() {
        if(this.getController() instanceof MetaTileEntityPowerSubstation powerStation)
            return powerStation;
        return null;
    }

    @Override
    public void setEnergyBank(MetaTileEntityPowerSubstation.PowerStationEnergyBank energyBank) {
        if(getPSS()!=null) getPSS().setEnergyBank(energyBank);
    }
    //接口实现////////////////////////////////////////////////////////////////////////////
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        data.setInteger("priority", this.priority);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.priority = data.getInteger("priority");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.priority);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.priority = buf.readInt();
    }
}
