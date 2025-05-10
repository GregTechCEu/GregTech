package gregtech.common.metatileentities.wirelesscharger;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.IWirelessCharger;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ACTIVE;

public class MetaTileEntityWirelessCharger extends TieredMetaTileEntity implements IWirelessCharger, IDataInfoProvider {

    private boolean lastActiveState = true;
    private boolean locked = false;
    private final int range;
    private final Set<EntityPlayer> playersInRange = new HashSet<>();

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

    protected void detectPlayers() {
        for (EntityPlayer player : getWorld().playerEntities) {
            if (calculateDistance(player, getPos()) <= range) {
                if (!playersInRange.contains(player) && isPlayerValid(player)) {
                    playersInRange.add(player);
                    player.sendMessage(new TextComponentTranslation("gregtech.machine.wireless_charger.in_range",
                            GTValues.VN[getTier()]));
                }
            } else {
                if (playersInRange.contains(player)) {
                    playersInRange.remove(player);
                    player.sendMessage(new TextComponentTranslation("gregtech.machine.wireless_charger.left_range",
                            GTValues.VN[getTier()]));
                }
            }
        }

        boolean hasPlayersInRange = !playersInRange.isEmpty();
        if (lastActiveState != hasPlayersInRange) {
            lastActiveState = hasPlayersInRange;
            writeCustomData(UPDATE_ACTIVE, buf -> buf.writeBoolean(lastActiveState));
        }
    }

    protected static int calculateDistance(@NotNull EntityPlayer player, @NotNull BlockPos pos) {
        return (int) GTUtility.euclidianDistance((int) player.posX, (int) player.posY, (int) player.posZ + 1,
                pos.getX(), pos.getY(), pos.getZ());
    }

    protected boolean isPlayerValid(@NotNull EntityPlayer player) {
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
    public boolean canChargePlayerItems(@NotNull EntityPlayer player) {
        return playersInRange.contains(player);
    }

    @Override
    public void chargePlayerItems(@NotNull List<ItemStack> stacksToCharge) {
        long usedEU = 0;

        for (ItemStack stack : stacksToCharge) {
            long availableEU = Math.min(energyContainer.getEnergyStored(), energyContainer.getInputVoltage() * 20);
            if (availableEU == 0 || GTUtility.isItemChargable(stack)) {
                usedEU += GTUtility.chargeItem(stack, availableEU,
                        GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage()));
            }
        }

        energyContainer.removeEnergy(usedEU);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        super.onRightClick(playerIn, hand, facing, hitResult);

        if (!getWorld().isRemote) {
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.wireless_charger.players"));
            for (EntityPlayer player : playersInRange) {
                playerIn.sendMessage(new TextComponentString("- ").appendSibling(player.getDisplayName()));
            }
        }

        return true;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            if (locked) {
                locked = false;
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.wireless_charger.public"),
                        true);
            } else {
                locked = true;
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.wireless_charger.private"),
                        true);
            }

            playersInRange.clear();
            detectPlayers();
        }

        return true;
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);

        if (dataId == UPDATE_ACTIVE) {
            lastActiveState = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        buf.writeBoolean(lastActiveState);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        lastActiveState = buf.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        Textures.WIRELESS_CHARGER_DISPLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                lastActiveState, true);
    }

    @Override
    public @NotNull List<ITextComponent> getDataInfo() {
        List<ITextComponent> playerList = new ArrayList<>();
        playerList.add(new TextComponentTranslation("gregtech.machine.wireless_charger.players"));
        for (EntityPlayer player : playersInRange) {
            playerList.add(new TextComponentString("- ").appendSibling(player.getDisplayName()));
        }
        return playerList;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.wireless_charger.tooltip.generic"));
        tooltip.add(I18n.format("gregtech.machine.wireless_charger.tooltip.range",
                TextFormattingUtil.formatNumbers(range)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in",
                energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till",
                energyContainer.getInputAmperage()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity",
                energyContainer.getEnergyCapacity()));
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
