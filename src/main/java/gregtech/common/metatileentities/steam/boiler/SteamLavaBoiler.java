package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.GTValues;
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
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SteamLavaBoiler extends SteamBoiler implements IFuelable {

    private FluidTank fuelFluidTank;

    private final Map<Fluid, Integer> boilerFuels;

    public SteamLavaBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.LAVA_BOILER_OVERLAY);
        this.boilerFuels = getBoilerFuels();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamLavaBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 600 : 240;
    }

    private Map<Fluid, Integer> getBoilerFuels() {
        Map<Fluid, Integer> fuels = new HashMap<>();
        fuels.put(Materials.Lava.getFluid(), 100);
        fuels.put(Materials.Creosote.getFluid(), 250);

        return fuels;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        FluidTankList superHandler = super.createImportFluidHandler();
        this.fuelFluidTank = new FilteredFluidHandler(16000)
                .setFillPredicate(fs -> boilerFuels.containsKey(fs.getFluid()));
        return new FluidTankList(false, superHandler, fuelFluidTank);

    }

    @Override
    protected void tryConsumeNewFuel() {
        for(Map.Entry<Fluid, Integer> fuels : boilerFuels.entrySet()) {
            if(fuelFluidTank.getFluid() != null && fuelFluidTank.getFluid().isFluidEqual(new FluidStack(fuels.getKey(), fuels.getValue())) && fuelFluidTank.getFluidAmount() >= fuels.getValue()) {
                fuelFluidTank.drain(fuels.getValue(), true);
                setFuelMaxBurnTime(100);
            }
        }
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 40 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 1;
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
        return Collections.singleton(new FluidFuelInfo(fuel, fuelRemaining, fuelCapacity, boilerFuels.get(fuel.getFluid()), burnTime));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer)
                .widget(new TankWidget(fuelFluidTank, 119, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))
                .build(getHolder(), entityPlayer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(float x, float y, float z) {
        super.randomDisplayTick(x, y, z);
        if (GTValues.RNG.nextFloat() < 0.3F) {
            getWorld().spawnParticle(EnumParticleTypes.LAVA, x + GTValues.RNG.nextFloat(), y, z + GTValues.RNG.nextFloat(), 0.0F, 0.0F, 0.0F);
        }
    }
}
