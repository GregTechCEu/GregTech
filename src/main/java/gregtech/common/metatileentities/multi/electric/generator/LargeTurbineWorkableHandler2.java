package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.FluidKey;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.ParallelLogic;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.MetaFluids;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine.TurbineType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LargeTurbineWorkableHandler2 extends MultiblockFuelRecipeLogic {

    private final int BASE_EU_OUTPUT;

    private int excessVoltage;

    private boolean shouldRun = true;

    public LargeTurbineWorkableHandler2(RecipeMapMultiblockController metaTileEntity, int tier) {
        super(metaTileEntity);
        this.BASE_EU_OUTPUT = (int) GTValues.V[tier] * 2;
    }

    @Override
    public void update() {
        super.update();
        //update the status of the workable twice per second to reduce the density of checks
        if (!metaTileEntity.getWorld().isRemote && metaTileEntity.getOffsetTimer() % 10 == 0) {
            shouldRun = shouldRunUpdate();
        }
    }

    public FluidStack getInputFluidStack() {
        if (previousRecipe == null)
            return null;
        FluidStack fuelStack = previousRecipe.getFluidInputs().get(0);
        return getInputTank().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }

    public boolean shouldRun() {
        return shouldRun;
    }

    public boolean shouldRunUpdate() {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine2) metaTileEntity).getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor() && rotorHolder.isFrontFaceFree();
    }

    @Override
    public void updateRecipeProgress() {
        //no safe void for turbines
        drawEnergy(recipeEUt, false);
        if (++progressTime > maxProgressTime) {
            completeRecipe();
        }
    }

    @Override
    protected void trySearchNewRecipe() {
        if (shouldRun())
            super.trySearchNewRecipe();
    }

    @Override
    public long getMaxVoltage() {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine2) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return (long) BASE_EU_OUTPUT * rotorHolder.getTotalPower() / 100;
        return 0;
    }

    @Override
    protected long boostProduction(int production) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine2) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed)
                return production;
            return (int) (production * Math.pow(1.0 * currentSpeed / maxSpeed, 2));
        }
        return 0;
    }

    @Override
    protected boolean prepareRecipe(Recipe recipe) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine2) metaTileEntity).getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor())
            return false;

        int turbineMaxVoltage = (int) getMaxVoltage();
        FluidStack recipeFluidStack = recipe.getFluidInputs().get(0);
        int inputAmount = 0;
        int parallel = 0;

        if (excessVoltage >= turbineMaxVoltage) {
            excessVoltage -= turbineMaxVoltage;
        } else {
            double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;
            //get the amount of parallel required to match the desired output voltage
            parallel = MathHelper.ceil((turbineMaxVoltage - excessVoltage) /
                            (Math.abs(recipe.getEUt()) * holderEfficiency));

            inputAmount = recipeFluidStack.amount * parallel;
            if (getInputFluidStack().amount < inputAmount)
                return false;

            excessVoltage += (int) (parallel * Math.abs(recipe.getEUt()) * holderEfficiency - turbineMaxVoltage);
        }

        //rebuild the recipe and adjust voltage to match the turbine, with adjusted input
        RecipeBuilder<?> recipeBuilder = getRecipeMap().recipeBuilder();
        recipeBuilder.append(recipe, parallel, false)
                .EUt(-turbineMaxVoltage);
        if (recipeFluidStack.amount != inputAmount) {
            recipeBuilder.clearFluidInputs()
                    .fluidInputs(new FluidStack(recipeFluidStack.getFluid(), inputAmount));
        }
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

    //TODO re-implement large turbine fluid output using recipe voiding
//    private void addOutputFluids(FuelRecipe currentRecipe, int fuelAmountUsed) {
//        if (largeTurbine.turbineType == TurbineType.STEAM) {
//            int waterFluidAmount = fuelAmountUsed / 15;
//            if (waterFluidAmount > 0) {
//                FluidStack waterStack = Materials.Water.getFluid(waterFluidAmount);
//                largeTurbine.exportFluidHandler.fill(waterStack, true);
//            }
//        } else if (largeTurbine.turbineType == TurbineType.PLASMA) {
//            Material material = MetaFluids.getMaterialFromFluid(currentRecipe.getRecipeFluid().getFluid());
//            if (material != null) {
//                largeTurbine.exportFluidHandler.fill(material.getFluid(fuelAmountUsed), true);
//            }
//        }
//    }
}
