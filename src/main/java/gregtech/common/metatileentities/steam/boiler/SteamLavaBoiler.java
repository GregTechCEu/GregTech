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
import gregtech.api.recipes.ModHandler;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.Collection;
import java.util.Collections;

public class SteamLavaBoiler extends SteamBoiler implements IFuelable {

    private FluidTank lavaFluidTank;

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
        this.lavaFluidTank = new FilteredFluidHandler(16000)
                .setFillPredicate(ModHandler::isLava);
        return new FluidTankList(false, superHandler, lavaFluidTank);

    }

    public static final int LAVA_PER_OPERATION = 100; // todo this may be too good?

    @Override
    protected void tryConsumeNewFuel() {
        if (lavaFluidTank.getFluidAmount() >= LAVA_PER_OPERATION) {
            lavaFluidTank.drain(LAVA_PER_OPERATION, true);
            setFuelMaxBurnTime(LAVA_PER_OPERATION);
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
        FluidStack lava = lavaFluidTank.drain(Integer.MAX_VALUE, false);
        if (lava == null || lava.amount == 0)
            return Collections.emptySet();
        final int fuelRemaining = lava.amount;
        final int fuelCapacity = lavaFluidTank.getCapacity();
        final long burnTime = (long) fuelRemaining * (this.isHighPressure ? 6 : 12); // 100 mb lasts 600 or 1200 ticks
        return Collections.singleton(new FluidFuelInfo(lava, fuelRemaining, fuelCapacity, LAVA_PER_OPERATION, burnTime));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer)
                .widget(new TankWidget(lavaFluidTank, 119, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))
                .build(getHolder(), entityPlayer);
    }
}
