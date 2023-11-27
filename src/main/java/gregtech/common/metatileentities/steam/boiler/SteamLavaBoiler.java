package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.GTValues;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SteamLavaBoiler extends SteamBoiler {

    private static final Object2IntMap<Fluid> BOILER_FUEL_TO_CONSUMPTION = new Object2IntOpenHashMap<>();
    private static boolean initialized;

    private static final IFilter<FluidStack> FUEL_FILTER = new IFilter<>() {

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            for (Fluid fluid : getBoilerFuelToConsumption().keySet()) {
                if (CommonFluidFilters.matchesFluid(fluidStack, fluid)) return true;
            }
            return false;
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistPriority(getBoilerFuelToConsumption().size());
        }
    };

    private static void init() {
        setBoilerFuelToConsumption(Materials.Lava.getFluid(), 100);
        setBoilerFuelToConsumption(Materials.Creosote.getFluid(), 250);
    }

    @NotNull
    public static Object2IntMap<Fluid> getBoilerFuelToConsumption() {
        if (!initialized) {
            initialized = true;
            init();
        }
        return Object2IntMaps.unmodifiable(BOILER_FUEL_TO_CONSUMPTION);
    }

    public static void setBoilerFuelToConsumption(@NotNull Fluid fluid, int amount) {
        Objects.requireNonNull(fluid, "fluid == null");
        if (amount <= 0) throw new IllegalArgumentException("amount <= 0");
        BOILER_FUEL_TO_CONSUMPTION.put(fluid, amount);
    }

    private FluidTank fuelFluidTank;

    public SteamLavaBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.LAVA_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamLavaBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 600 : 240;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        FluidTankList superHandler = super.createImportFluidHandler();
        this.fuelFluidTank = new FilteredFluidHandler(16000).setFilter(FUEL_FILTER);
        return new FluidTankList(false, superHandler, fuelFluidTank);
    }

    @Override
    protected void tryConsumeNewFuel() {
        FluidStack fluid = fuelFluidTank.getFluid();
        if (fluid == null || fluid.tag != null) { // fluid with nbt tag cannot match normal fluids
            return;
        }
        int consumption = getBoilerFuelToConsumption().getInt(fluid.getFluid());
        if (consumption > 0 && fuelFluidTank.getFluidAmount() >= consumption) {
            fuelFluidTank.drain(consumption, true);
            setFuelMaxBurnTime(100);
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
            getWorld().spawnParticle(EnumParticleTypes.LAVA, x + GTValues.RNG.nextFloat(), y,
                    z + GTValues.RNG.nextFloat(), 0.0F, 0.0F, 0.0F);
        }
    }
}
