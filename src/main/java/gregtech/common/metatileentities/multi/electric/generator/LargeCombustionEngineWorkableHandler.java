package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.unification.material.Materials;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Supplier;

public class LargeCombustionEngineWorkableHandler extends FuelRecipeLogic {

    private final int maxCycleLength = 20;
    private int currentCycle = 0;
    private boolean isUsingOxygen = false;

    public LargeCombustionEngineWorkableHandler(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap,
                                                Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank, long maxVoltage) {
        super(metaTileEntity, recipeMap, energyContainer, fluidTank, maxVoltage);
    }

    public FluidStack getFuelStack() {
        if (previousRecipe == null)
            return null;
        FluidStack fuelStack = previousRecipe.getRecipeFluid();
        return fluidTank.get().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }

    @Override
    protected boolean checkRecipe(FuelRecipe recipe) {
        FluidStack lubricantStack = Materials.Lubricant.getFluid(2);
        FluidStack drainStack = fluidTank.get().drain(lubricantStack, false);
        //TODO, shouldn't this be &&?
        return (drainStack != null && drainStack.amount >= 2) || currentCycle < maxCycleLength;
    }

    @Override
    protected int calculateFuelAmount(FuelRecipe currentRecipe) {
        FluidStack oxygenStack = Materials.Oxygen.getFluid(2);
        FluidStack drainOxygenStack = fluidTank.get().drain(oxygenStack, false);
        this.isUsingOxygen = drainOxygenStack != null && drainOxygenStack.amount >= 2;
        return super.calculateFuelAmount(currentRecipe) * (isUsingOxygen ? 2 : 1);
    }

    @Override
    protected long startRecipe(FuelRecipe currentRecipe, int fuelAmountUsed, int recipeDuration) {
        if (this.currentCycle >= maxCycleLength) {
            FluidStack lubricantStack = Materials.Lubricant.getFluid(2);
            fluidTank.get().drain(lubricantStack, true);
            this.currentCycle = 0;
        } else this.currentCycle++;
        if (isUsingOxygen) {
            FluidStack oxygenStack = Materials.Oxygen.getFluid(2);
            fluidTank.get().drain(oxygenStack, true);
        }
        return maxVoltage * (isUsingOxygen ? 3 : 1);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("Cycle", currentCycle);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.currentCycle = compound.getInteger("Cycle");
    }

    @Override
    protected boolean isObstructed() {
        return checkIntakesObstructed();
    }

    private boolean checkIntakesObstructed() {
        if(this.metaTileEntity instanceof MultiblockWithDisplayBase) {
            MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) this.metaTileEntity;

            EnumFacing facing = controller.getFrontFacing();
            //TODO, this also needs to check Y for freedom wrench implementation
            boolean permuteXZ = facing.getAxis() == EnumFacing.Axis.Z;
            BlockPos centerPos = controller.getPos().offset(facing);
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    //Skip the controller block itself
                    if(x == 0 && y == 0) {
                        continue;
                    }
                    BlockPos blockPos = centerPos.add(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                    IBlockState blockState = controller.getWorld().getBlockState(blockPos);
                    if (!blockState.getBlock().isAir(blockState, controller.getWorld(), blockPos)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
