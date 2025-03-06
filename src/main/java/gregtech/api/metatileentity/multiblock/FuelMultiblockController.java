package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStallType;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.overclock.RecipeNoOverclockingOperator;
import gregtech.api.recipes.logic.statemachine.running.RecipeCleanupSaveOperation;
import gregtech.api.recipes.logic.statemachine.running.RecipeFinalizingOperator;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class FuelMultiblockController extends RecipeMapMultiblockController {

    protected final long maxVoltage;

    public FuelMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier) {
        super(metaTileEntityId, recipeMap);
        maxVoltage = GTValues.V[tier];
    }

    @Override
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        super.modifyRecipeLogicStandardBuilder(builder);
        builder.setOverclockFactory(RecipeNoOverclockingOperator::create)
                .setDownTransformForParallels(true)
                .setCleanupOperator(RecipeCleanupSaveOperation.STANDARD_INSTANCE)
                .setParallelLimit(() -> Integer.MAX_VALUE)
                .setStallType(RecipeStallType.PAUSE);
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.comprehensive(getEnergyContainer().getInputVoltage(),
                getEnergyContainer().getInputAmperage(), getMaxVoltage(),
                getEnergyContainer().getOutputAmperage());
        return set;
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        List<IEnergyContainer> outputEnergy = new ArrayList<>(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        outputEnergy.addAll(getAbilities(MultiblockAbility.SUBSTATION_OUTPUT_ENERGY));
        outputEnergy.addAll(getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer = new EnergyContainerList(outputEnergy);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        // TODO multiple recipe display
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(isWorkingEnabled(), isActive())
                // .addEnergyProductionLine(getMaxVoltage(), recipeEUt())
                .addFuelNeededLine(getRecipeFluidInputInfo(), estimateRecipeDuration())
                .addWorkingStatusLine();
    }

    protected long getMaxVoltage() {
        return Math.min(maxVoltage, getEnergyContainer().getOutputVoltage());
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addLowDynamoTierLine(isDynamoTierTooLow())
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    protected boolean isDynamoTierTooLow() {]
        // TODO multiple recipe display
        // if (isStructureFormed()) {
        // return getMaxVoltage() < -recipeEUt();
        // }
        return false;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        // TODO multiple recipe display
        // if (maxProgress() > 0) {
        // list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
        // new TextComponentTranslation(TextFormattingUtil.formatNumbers(progress() / 20))
        // .setStyle(new Style().setColor(TextFormatting.GREEN)),
        // new TextComponentTranslation(
        // TextFormattingUtil.formatNumbers(maxProgress() / 20))
        // .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        // }

        list.add(new TextComponentTranslation("behavior.tricorder.energy_container_storage",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyStored()))
                        .setStyle(new Style().setColor(TextFormatting.GREEN)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));

        if (workable.isRecipeSelected()) {
            // list.add(new TextComponentTranslation("behavior.tricorder.workable_production",
            // new TextComponentTranslation(
            // TextFormattingUtil.formatNumbers(Math.abs(recipeEUt())))
            // .setStyle(new Style().setColor(TextFormatting.RED)),
            // new TextComponentTranslation(
            // TextFormattingUtil.formatNumbers(recipeEUt() == 0 ? 0 : 1))
            // .setStyle(new Style().setColor(TextFormatting.RED))));

            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_energy_output",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getOutputVoltage()))
                            .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                    new TextComponentTranslation(
                            GTValues.VN[GTUtility.getTierByVoltage(energyContainer.getOutputVoltage())])
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        }

        if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_maintenance",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(getNumMaintenanceProblems()))
                            .setStyle(new Style().setColor(TextFormatting.RED))));
        }

        return list;
    }

    protected int[] getTotalFluidAmount(FluidStack testStack, IMultipleTankHandler multiTank) {
        int fluidAmount = 0;
        int fluidCapacity = 0;
        for (var tank : multiTank) {
            if (tank != null) {
                FluidStack drainStack = tank.drain(testStack, false);
                if (drainStack != null && drainStack.amount > 0) {
                    fluidAmount += drainStack.amount;
                    fluidCapacity += tank.getCapacity();
                }
            }
        }
        return new int[] { fluidAmount, fluidCapacity };
    }

    protected void addFuelText(List<ITextComponent> textList) {
        // Fuel
        int fuelStored = 0;
        int fuelCapacity = 0;
        FluidStack fuelStack = null;
        if (isStructureFormed() && (fuelStack = getInputFluidStack()) != null && getInputFluidInventory() != null) {
            fuelStack = fuelStack.copy();
            fuelStack.amount = Integer.MAX_VALUE;
            int[] fuelAmount = getTotalFluidAmount(fuelStack, getInputFluidInventory());
            fuelStored = fuelAmount[0];
            fuelCapacity = fuelAmount[1];
        }

        if (fuelStack != null) {
            ITextComponent fuelName = TextComponentUtil.setColor(GTUtility.getFluidTranslation(fuelStack),
                    TextFormatting.GOLD);
            ITextComponent fuelInfo = new TextComponentTranslation("%s / %s L (%s)",
                    TextFormattingUtil.formatNumbers(fuelStored),
                    TextFormattingUtil.formatNumbers(fuelCapacity),
                    fuelName);
            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.large_combustion_engine.fuel_amount",
                    TextComponentUtil.setColor(fuelInfo, TextFormatting.GOLD)));
        } else {
            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.large_combustion_engine.fuel_amount",
                    "0 / 0 L"));
        }
    }

    /**
     * See {@link RecipeFinalizingOperator} for encoding pattern.
     */
    protected @Nullable NBTTagCompound getMostUpToDateRecipe() {
        // TODO multiple recipe display
        // NBTTagCompound worker = activeWorker.logicData();
        // if (worker.hasKey(RecipeFinalizingOperator.STANDARD_RESULTS_KEY)) {
        // return worker.getCompoundTag(RecipeFinalizingOperator.STANDARD_RESULTS_KEY);
        // } else if (worker.hasKey(RecipeCleanupSaveOperation.STANDARD_PREVIOUS_RECIPE_KEY)) {
        // return worker.getCompoundTag(RecipeCleanupSaveOperation.STANDARD_PREVIOUS_RECIPE_KEY);
        // }
        return null;
    }

    protected int estimateRecipeDuration() {
        NBTTagCompound recipe = getMostUpToDateRecipe();
        return recipe == null ? 0 : recipe.getInteger("Duration");
    }

    @Nullable
    protected String getRecipeFluidInputInfo() {
        List<IRotorHolder> abilities = this.getAbilities(MultiblockAbility.ROTOR_HOLDER);
        IRotorHolder rotorHolder = abilities.size() > 0 ? abilities.get(0) : null;

        NBTTagCompound recipe = getMostUpToDateRecipe();
        if (recipe == null) return null;

        NBTTagList list = recipe.getTagList("FluidsIn", Constants.NBT.TAG_COMPOUND);
        if (list.isEmpty()) return null;

        FluidStack requiredFluidInput = FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(0));
        if (requiredFluidInput == null) return null;

        int neededAmount = requiredFluidInput.amount;
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            neededAmount /= (rotorHolder.getTotalEfficiency() / 100.0);
        } else if (rotorHolder != null && !rotorHolder.hasRotor()) {
            return null;
        }
        return TextFormatting.RED + TextFormattingUtil.formatNumbers(neededAmount) + "L";
    }

    protected @Nullable FluidStack getInputFluidStack() {
        NBTTagCompound recipe = getMostUpToDateRecipe();
        if (recipe == null) return null;

        NBTTagList list = recipe.getTagList("FluidsIn", Constants.NBT.TAG_COMPOUND);
        if (list.isEmpty()) return null;

        return FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(0));
    }
}
