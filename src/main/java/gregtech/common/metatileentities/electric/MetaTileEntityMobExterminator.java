package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.unification.material.Materials.NitrousOxide;

public class MetaTileEntityMobExterminator extends TieredMetaTileEntity {

    private static final int BASE_EU_CONSUMPTION_PER_KILL = 2;
    private static final int RADIUS = 4;
    private boolean isWorking;
    private AxisAlignedBB areaBoundingBox;
    private BlockPos areaCenterPos;
    public static int LOOTING_USED;
    private final FluidTank fluidTank;

    public MetaTileEntityMobExterminator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.fluidTank = new FluidTank(tier * 4000);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMobExterminator(metaTileEntityId, getTier());
    }

    @Override
    public void update() {
        super.update();

        if (getWorld().isRemote || getOffsetTimer() % 20 != 0) {
            return;
        }

        boolean isWorkingNow = energyContainer.getEnergyStored() >= getEnergyConsumedPerKill() && isBlockRedstonePowered();
        if (isWorkingNow) {
            BlockPos selfPos = getPos();
            if (areaCenterPos == null || areaBoundingBox == null) {
                this.areaCenterPos = selfPos.offset(this.getFrontFacing(), 5);
                this.areaBoundingBox = new AxisAlignedBB(areaCenterPos).grow(4, 0, 4);
            }
            List<EntityLivingBase> mobs = this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, areaBoundingBox);

            if (!mobs.isEmpty()) {
                int loopLength = Math.min(this.getMobsPerCycle(), mobs.size());
                for (int i = 0; i < loopLength; i++) {
                    LOOTING_USED = this.getTier() - 1;
                    // When the following function is called, ForgeHooks.getLootingLevel, by activating EventHandlers.onLootingLevel, will get LOOTING_USED.
                    // Race conditions probably won't happen, fortunately.
                    mobs.get(i).attackEntityFrom(DamageSources.getExterminationDamage(this.getWorld()), mobs.get(i).getHealth());
                    if (i > 3) {
                        fluidTank.drain(1, true);
                    }
                    energyContainer.removeEnergy(getEnergyConsumedPerKill());
                }
            }
        }
    }

    // Must only be called inside update().
    private int getMobsPerCycle() {
        int maximumKills = Math.toIntExact(energyContainer.getEnergyStored() / getEnergyConsumedPerKill());
        FluidStack currentGas = fluidTank.getFluid();
        if (currentGas != null && currentGas.isFluidEqual(NitrousOxide.getFluid(1))) {
            int amountToExtract = Math.min(currentGas.amount, 12);
            return Math.min(amountToExtract + 4, maximumKills);
        }
        return Math.min(4, maximumKills);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = new TankWidget(fluidTank, 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);
        return builder.label(6, 6, metaTileEntityId.toString()).build(getHolder(), entityPlayer);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isWorking);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorking = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isWorking = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    protected int getEnergyConsumedPerKill() {
        return BASE_EU_CONSUMPTION_PER_KILL * (1 << (getTier() - 1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.mob_exterminator.tooltip", getTier() - 1));
        tooltip.add(I18n.format("gregtech.universal.tooltip.max_voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.item_controller.tooltip.redstone"));
        tooltip.add(I18n.format("gregtech.machine.mob_exterminator.tooltip.consumption", getEnergyConsumedPerKill()));
        tooltip.add(I18n.format("gregtech.machine.mob_exterminator.tooltip.nitrous"));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, fluidTank);
    }
}
