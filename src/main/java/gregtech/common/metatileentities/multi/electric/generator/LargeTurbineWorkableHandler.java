package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

public class LargeTurbineWorkableHandler extends MultiblockFuelRecipeLogic {

    private final int BASE_EU_OUTPUT;

    private int excessVoltage;

    public LargeTurbineWorkableHandler(RecipeMapMultiblockController metaTileEntity, int tier) {
        super(metaTileEntity);
        this.BASE_EU_OUTPUT = (int) GTValues.V[tier] * 2;
    }

    @Override
    protected void updateRecipeProgress() {
        if (canRecipeProgress) {
            // turbines can void energy
            drawEnergy(recipeEUt, false);
            //as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        }
    }

    public FluidStack getInputFluidStack() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        if (previousRecipe == null) {
            Recipe recipe = findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());

            return recipe == null ? null : getInputTank().drain(new FluidStack(recipe.getFluidInputs().get(0).getInputFluidStack().getFluid(), Integer.MAX_VALUE), false);
        }
        FluidStack fuelStack = previousRecipe.getFluidInputs().get(0).getInputFluidStack();
        return getInputTank().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }

    @Override
    public long getMaxVoltage() {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return (long) BASE_EU_OUTPUT * rotorHolder.getTotalPower() / 100;
        return 0;
    }

    @Override
    protected long boostProduction(long production) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed)
                return production;
            return (long) (production * Math.pow(1.0 * currentSpeed / maxSpeed, 2));
        }
        return 0;
    }

    @Override
    protected boolean prepareRecipe(Recipe recipe) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor())
            return false;

        int turbineMaxVoltage = (int) getMaxVoltage();
        FluidStack recipeFluidStack = recipe.getFluidInputs().get(0).getInputFluidStack();
        int parallel = 0;

        if (excessVoltage >= turbineMaxVoltage) {
            excessVoltage -= turbineMaxVoltage;
        } else {
            double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;
            //get the amount of parallel required to match the desired output voltage
            parallel = MathHelper.ceil((turbineMaxVoltage - excessVoltage) /
                            (Math.abs(recipe.getEUt()) * holderEfficiency));

            // Null check fluid here, since it can return null on first join into world or first form
            FluidStack inputFluid = getInputFluidStack();
            if(inputFluid == null || getInputFluidStack().amount < recipeFluidStack.amount * parallel) {
                return false;
            }

            //this is necessary to prevent over-consumption of fuel
            excessVoltage += (int) (parallel * Math.abs(recipe.getEUt()) * holderEfficiency - turbineMaxVoltage);
        }

        //rebuild the recipe and adjust voltage to match the turbine
        RecipeBuilder<?> recipeBuilder = getRecipeMap().recipeBuilder();
        recipeBuilder.append(recipe, parallel, false)
                .EUt(-turbineMaxVoltage);
        applyParallelBonus(recipeBuilder);
        recipe = recipeBuilder.build().getResult();

        if (recipe != null && setupAndConsumeRecipeInputs(recipe, getInputInventory())) {
            setupRecipe(recipe);
            return true;
        }
        return false;
    }

    @Override
    public void invalidate() {
        excessVoltage = 0;
        super.invalidate();
    }

    public void updateTanks() {
        FuelMultiblockController controller = (FuelMultiblockController) this.metaTileEntity;
        List<IFluidHandler> tanks = controller.getNotifiedFluidInputList();
        for (IFluidTank tank : controller.getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
            tanks.add((FluidTank) tank);
        }
    }
}
