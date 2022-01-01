package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidFuelInfo;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SteamLavaBoiler extends SteamBoiler implements IFuelable {

    private FluidTank fuelFluidTank;

    private final List<FluidStack> boilerFuels = new ArrayList<FluidStack>() {{
        boilerFuels.add(Materials.Lava.getFluid(100));
        boilerFuels.add(Materials.Creosote.getFluid(250));
    }};

    public SteamLavaBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.LAVA_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SteamLavaBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 600 : 240;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        FluidTankList superHandler = super.createImportFluidHandler();
        this.fuelFluidTank = new FilteredFluidHandler(16000)
                .setFillPredicate(boilerFuels::contains);
        return new FluidTankList(false, superHandler, fuelFluidTank);

    }

    @Override
    protected void tryConsumeNewFuel() {
        for(FluidStack fuel : boilerFuels) {
            if(fuel.containsFluid(fuel) && fuelFluidTank.getFluidAmount() >= fuel.amount) {
                fuelFluidTank.drain(fuel.amount, true);
                setFuelMaxBurnTime((1000 / fuel.amount) * 10);
            }
        }
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 40 : 45;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T result = super.getCapability(capability, side);
        if (result != null)
            return result;
        if (capability == GregtechCapabilities.CAPABILITY_FUELABLE) {
            return GregtechCapabilities.CAPABILITY_FUELABLE.cast(this);
        }
        return null;
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        FluidStack fuel = fuelFluidTank.drain(Integer.MAX_VALUE, false);
        if (fuel == null || fuel.amount == 0)
            return Collections.emptySet();
        final int fuelRemaining = fuel.amount;
        final int fuelCapacity = fuelFluidTank.getCapacity();
        final long burnTime = (long) fuelRemaining * (this.isHighPressure ? 6 : 12); // 100 mb lasts 600 or 1200 ticks
        return Collections.singleton(new FluidFuelInfo(fuel, fuelRemaining, fuelCapacity, boilerFuels.get(boilerFuels.indexOf(fuel)).amount, burnTime));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer)
                .widget(new TankWidget(fuelFluidTank, 119, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))
                .build(getHolder(), entityPlayer);
    }
}
