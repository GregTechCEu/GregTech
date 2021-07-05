package gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.ConfigHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

public class MetaTileEntityAirCollector extends TieredMetaTileEntity {
    private static final int PRODUCTION_CYCLE_LENGTH = 20;

    public MetaTileEntityAirCollector(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityAirCollector(metaTileEntityId, getTier());
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new FluidTank(32000));
    }

    @Override
    public void update() {
        super.update();

        if (!getWorld().isRemote) {
            long energyToConsume = GTValues.V[getTier()];
            if (checkDimension() && checkOpenSides() && getOffsetTimer() % PRODUCTION_CYCLE_LENGTH == 0L && energyContainer.getEnergyStored() >= energyToConsume) {
                int fluidAmount = getCollectedFluidAmount();
                FluidStack fluidStack = Materials.Air.getFluid(fluidAmount);
                if (exportFluids.fill(fluidStack, false) == fluidAmount) {
                    exportFluids.fill(fluidStack, true);
                    energyContainer.removeEnergy(energyToConsume);
                }
            }
            if (getOffsetTimer() % 5 == 0) {
                pushFluidsIntoNearbyHandlers(getFrontFacing());
            }
        }
    }

    private boolean checkOpenSides() {
        EnumFacing frontFacing = getFrontFacing();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == frontFacing) continue;
            if (getWorld().isAirBlock(getPos().offset(side)))
                return true;
        }
        return false;
    }

    private boolean checkDimension() {
        int dimensionId = getWorld().provider.getDimension();
        return IntStream.of(ConfigHolder.airCollectorDimensionBlacklist).noneMatch(x -> x == dimensionId);
    }

    private int getCollectedFluidAmount() {
        return 500 * (1 << getTier());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        EnumFacing frontFacing = getFrontFacing();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side.getAxis().isHorizontal()) {
                Textures.AIR_VENT_OVERLAY.renderSided(side, renderState, translation, pipeline);
            } else {
                Textures.FILTER_OVERLAY.renderSided(side, renderState, translation, pipeline);
            }
        }
        Textures.PIPE_OUT_OVERLAY.renderSided(frontFacing, renderState, translation, pipeline);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.air_collector.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.air_collector.collection_speed", getCollectedFluidAmount(), PRODUCTION_CYCLE_LENGTH));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VN[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", exportFluids.getTankAt(0).getCapacity()));
    }
}
