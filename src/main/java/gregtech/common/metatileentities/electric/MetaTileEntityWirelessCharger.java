package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.IWirelessCharger;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.registry.WirelessChargerManger;
import gregtech.api.util.GTUtility;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MetaTileEntityWirelessCharger extends TieredMetaTileEntity implements IWirelessCharger {

    private boolean locked = false;
    private final int range;
    private final Set<UUID> playersInRange = new HashSet<>();

    public MetaTileEntityWirelessCharger(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        range = (int) Math.pow(2, 4 + tier);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = EnergyContainerHandler.receiverContainer(this, tierVoltage * 128L, tierVoltage, 8L);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityWirelessCharger(metaTileEntityId, getTier());
    }

    @Override
    public void update() {
        super.update();

        if (!getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            detectPlayers();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getWorld().isRemote) {
            WirelessChargerManger.addCharger(this);
        }
    }

    private void detectPlayers() {
        for (EntityPlayer player : getWorld().playerEntities) {
            if (calculateDistance(player, getPos()) <= range) {
                if (!playersInRange.contains(player.getUniqueID()) && isPlayerValid(player)) {
                    playersInRange.add(player.getUniqueID());
                    // TODO: proper lang
                    player.sendMessage(new TextComponentString("in range of wireless charger"));
                }
            } else {
                playersInRange.remove(player.getUniqueID());
                // TODO: proper lang
                player.sendMessage(new TextComponentString("left range of wireless charger"));
            }
        }
    }

    private static int calculateDistance(EntityPlayer player, BlockPos pos) {
        return (int) GTUtility.euclidianDistance((int) player.posX, (int) player.posY, (int) player.posZ, pos.getX(),
                pos.getY(), pos.getZ());
    }

    private boolean isPlayerValid(EntityPlayer player) {
        return !locked || player.getUniqueID().equals(getOwner());
    }

    @Override
    public void onPlacement(@Nullable EntityLivingBase placer) {
        super.onPlacement(placer);
        if (!getWorld().isRemote) {
            WirelessChargerManger.addCharger(this);
            detectPlayers();
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote) {
            WirelessChargerManger.removeCharger(this);
        }
    }

    @Override
    public boolean canChargePlayerItems(EntityPlayer player) {
        return playersInRange.contains(player.getUniqueID());
    }

    @Override
    public void chargePlayerItems(List<ItemStack> stacksToCharge) {
        long usedEU = 0;
        for (ItemStack stack : stacksToCharge) {
            long availableEU = Math.min(energyContainer.getEnergyStored(), energyContainer.getInputVoltage() * 20);
            if (availableEU == 0) break;
            usedEU += GTUtility.chargeItem(stack, availableEU,
                    GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage()));
        }
        energyContainer.removeEnergy(usedEU);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setBoolean("Locked", locked);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        locked = data.getBoolean("Locked");
    }
}
