package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
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

    public LargeTurbineWorkableHandler2(RecipeMapMultiblockController metaTileEntity, int tier) {
        super(metaTileEntity);
        this.BASE_EU_OUTPUT = (int) GTValues.V[tier] * 2;
    }

    @Override
    public void updateRecipeProgress() {
        if (shouldRun()) {
            //no safe void for turbines
            drawEnergy(getOutputVoltage(), false);
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        } else
            invalidate();
    }

    @Override
    protected void trySearchNewRecipe() {
        if (shouldRun())
            super.trySearchNewRecipe();
    }

//    public FluidStack getFuelStack() {
//        if (previousRecipe == null)
//            return null;
//        FluidStack fuelStack = previousRecipe.getRecipeFluid();
//        return fluidTank.get().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
//    }

    @Override
    public long getMaxVoltage() {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine2) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return (long) BASE_EU_OUTPUT * rotorHolder.getTotalPower() / 100;
        else return 0;
    }

    public int getOutputVoltage() {
        return getRecipeEUt() * ((MetaTileEntityLargeTurbine2) metaTileEntity).getRotorHolder().getTotalEfficiency() / 100;
    }

    @Override
    public int getParallelLimit() {
        return Integer.MAX_VALUE;
    }

    public boolean shouldRun() {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine2) metaTileEntity).getRotorHolder();
        return workingEnabled && rotorHolder != null && rotorHolder.hasRotor();
    }

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

//    @Override
//    public NBTTagCompound serializeNBT() {
//        NBTTagCompound tagCompound = super.serializeNBT();
//        return tagCompound;
//    }

//    @Override
//    public void deserializeNBT(NBTTagCompound compound) {
//        super.deserializeNBT(compound);
//    }
}
