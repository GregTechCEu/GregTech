package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.RecipeView;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

public class LargeTurbineWorkableHandler extends MultiblockFuelRecipeLogic {

    private final long baseEuOutput;

    public LargeTurbineWorkableHandler(MetaTileEntityLargeTurbine metaTileEntity, int tier) {
        super(metaTileEntity);
        this.baseEuOutput = GTValues.V[tier];
    }

    @Override
    public @NotNull MetaTileEntityLargeTurbine getMetaTileEntity() {
        return (MetaTileEntityLargeTurbine) metaTileEntity;
    }

    @Override
    protected boolean produceEnergy(long eu, boolean simulate) {
        eu = boostProduction(eu);
        // turbines can void energy
        if (!simulate) getEnergyContainer()
                .changeEnergy(Math.max(-getEnergyStored(), Math.min(eu, getEnergyCapacity() - getEnergyStored())));
        return true;
    }

    @Override
    public long getMaxVoltageOut() {
        IRotorHolder rotorHolder = getMetaTileEntity().getRotorHolder();
        // both power and efficiency increase production
        if (rotorHolder != null && rotorHolder.hasRotor())
            return baseEuOutput * rotorHolder.getTotalPower() / 100 * rotorHolder.getTotalEfficiency() / 100;
        return 0;
    }

    @Override
    public long getMaxAmperageOut() {
        return 2;
    }

    @Override
    public double getEUtDiscount() {
        IRotorHolder rotorHolder = getMetaTileEntity().getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return rotorHolder.getTotalEfficiency() / 100d * rotorHolder.getTotalPower() / 100d;
        return 1;
    }

    @Override
    protected float computeDurationMultiplier(RecipeView recipe,
                                              @Range(from = 0, to = Integer.MAX_VALUE) int overclocks) {
        IRotorHolder rotorHolder = getMetaTileEntity().getRotorHolder();
        // power increases production and reduces duration in equal measure
        if (rotorHolder != null && rotorHolder.hasRotor())
            return (float) (super.computeDurationMultiplier(recipe, overclocks) * 100d / rotorHolder.getTotalPower());
        return super.computeDurationMultiplier(recipe, overclocks);
    }

    @Override
    protected @Nullable RecipeView applyParallel(@NotNull Recipe recipe, @NotNull MatchCalculation<ItemStack> itemMatch,
                                                 @NotNull MatchCalculation<FluidStack> fluidMatch) {
        RecipeView view = super.applyParallel(recipe, itemMatch, fluidMatch);
        if (view == null ||
                view.getActualAmperage() * view.getActualVoltage() < getMaxAmperageOut() * getMaxVoltageOut())
            return null;
        else return view;
    }

    @Override
    protected long boostProduction(long production) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed)
                return production;
            return (long) (production * Math.pow(1d * currentSpeed / maxSpeed, 2));
        }
        return 0;
    }

    public void updateTanks() {
        FuelMultiblockController controller = (FuelMultiblockController) this.metaTileEntity;
        List<IFluidHandler> tanks = controller.getNotifiedFluidInputList();
        for (IFluidTank tank : controller.getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
            tanks.add((IFluidHandler) tank);
        }
    }
}
