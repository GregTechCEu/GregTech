package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IHasRecipeMap;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.running.RecipeProgressOperation;
import gregtech.api.recipes.logic.workable.OutputBufferTrait;
import gregtech.api.recipes.logic.workable.RecipeWorkable;
import gregtech.api.recipes.lookup.AbstractRecipeLookup;
import gregtech.api.recipes.lookup.property.CleanroomFulfilmentProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class RecipeMapMultiblockController extends MultiblockWithDisplayBase implements IDataInfoProvider,
                                                    ICleanroomReceiver,
                                                    IControllable,
                                                    IHasRecipeMap,
                                                    RecipeWorkable.ISupportsRecipeWorkable,
                                                    OutputBufferTrait.IBufferingMTE {

    public final RecipeMap<?> recipeMap;
    protected RecipeWorkable workable;
    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;

    protected final OutputBufferTrait outputBuffer;

    @Nullable
    private ICleanroomProvider cleanroom;

    public RecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.outputBuffer = new OutputBufferTrait(this);
        RecipeStandardStateMachineBuilder std = new RecipeStandardStateMachineBuilder(this::getLookup);
        modifyRecipeLogicStandardBuilder(std);
        this.workable = new RecipeWorkable(this, std);
        resetTileAbilities();
    }

    /**
     * Called during initialization. Should not require any fields to be populated for the state machine to be
     * constructed.
     * Override this if you merely need to set up the standard state machine builder.
     */
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        builder.setOffthreadSearchAndSetup(true)
                .setParallelLimit(this::getBaseParallelLimit)
                .setUpTransformForOverclocks(true)
                .setDownTransformForParallels(true)
                .setMaintenance(this::getMaintenanceValues)
                .setItemInput(this::getInputInventory)
                .setFluidInput(this::getInputFluidInventory)
                .setProperties(this::computePropertySet)
                .setFluidOutput(outputBuffer::bufferFluids)
                .setFluidTrim(this::getFluidOutputLimit)
                .setItemOutput(outputBuffer::bufferItems)
                .setItemTrim(this::getItemOutputLimit)
                .setNotifiedFluidInputs(this::getNotifiedFluidInputList)
                .setNotifiedItemInputs(this::getNotifiedItemInputList)
                .setItemOutAmountLimit(() -> getOutputInventory().getSlots() * 64)
                .setItemOutStackLimit(() -> getOutputInventory().getSlots())
                .setFluidOutAmountLimit(() -> {
                    int sum = 0;
                    for (var e : getOutputFluidInventory().getFluidTanks()) {
                        int capacity = e.getCapacity();
                        sum += capacity;
                    }
                    return sum;
                })
                .setFluidOutStackLimit(() -> getOutputFluidInventory().getTanks())
                .setPerTickRecipeCheck((recipe) -> {
                    double progress = recipe.getInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY);
                    double maxProgress = recipe.getDouble("Duration");
                    long voltage = recipe.getLong("Voltage");
                    long amperage = recipe.getLong("Amperage");
                    long eut = (long) (Math.min(1, maxProgress - progress) * voltage * amperage);
                    boolean generating = recipe.getBoolean("Generating");
                    if (!generating) {
                        return Math.abs(getEnergyContainer().removeEnergy(eut)) >= eut;
                    } else {
                        getEnergyContainer().addEnergy(eut);
                        return true;
                    }
                });
    }

    @NotNull
    @Override
    public RecipeMap<?> getRecipeMap() {
        return recipeMap;
    }

    protected @NotNull AbstractRecipeLookup getLookup() {
        return getRecipeMap().getLookup();
    }

    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.comprehensive(getEnergyContainer().getInputVoltage(),
                getEnergyContainer().getInputAmperage(), getEnergyContainer().getOutputVoltage(),
                getEnergyContainer().getOutputAmperage());

        if (ConfigHolder.machines.cleanMultiblocks) {
            set.add(new CleanroomFulfilmentProperty(c -> true));
        } else {
            ICleanroomProvider prov = getCleanroom();
            if (prov != null) {
                set.add(new CleanroomFulfilmentProperty(c -> prov.isClean() && prov.checkCleanroomType(c)));
            }
        }
        return set;
    }

    protected int getBaseParallelLimit() {
        return 1;
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }

    public IMultipleTankHandler getInputFluidInventory() {
        return inputFluidInventory;
    }

    public IMultipleTankHandler getOutputFluidInventory() {
        return outputFluidInventory;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    @Override
    protected void updateFormedValid() {
        outputBuffer.updateBufferedOutputs();
    }

    @Override
    public boolean shouldRecipeWorkableUpdate() {
        return isStructureFormed() && !isStructureObstructed();
    }

    @Override
    public boolean areOutputsClogged() {
        outputBuffer.updateBufferedOutputs();
        return outputBuffer.awaitingSpace();
    }

    @Override
    public boolean shouldBeActive() {
        return super.shouldBeActive() && workable.isRunning();
    }

    @Override
    public @NotNull ItemStack outputFromBuffer(@NotNull ItemStack stack) {
        return GTTransferUtils.insertItem(getOutputInventory(), stack, false);
    }

    @Override
    public int outputFromBuffer(@NotNull FluidStack stack) {
        return getOutputFluidInventory().fill(stack, true);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            setWorkingEnabled(buf.readBoolean());
        }
    }

    protected boolean insufficientEnergy() {
        // if the energy container has less than a tenth of its capacity, we're probably low on energy.
        return isActive() && getEnergyContainer().getEnergyStored() <= getEnergyContainer().getEnergyCapacity() * 0.1;
    }

    protected void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(),
                getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(),
                getAbilities(MultiblockAbility.EXPORT_FLUIDS));

        List<IEnergyContainer> inputEnergy = new ArrayList<>(getAbilities(MultiblockAbility.INPUT_ENERGY));
        inputEnergy.addAll(getAbilities(MultiblockAbility.SUBSTATION_INPUT_ENERGY));
        inputEnergy.addAll(getAbilities(MultiblockAbility.INPUT_LASER));
        this.energyContainer = new EnergyContainerList(inputEnergy);
    }

    protected void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    protected boolean allowSameFluidFillForOutputs() {
        return true;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        // TODO multiple recipe display
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(isWorkingEnabled(), isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(getEnergyContainer().getInputVoltage()))
                .addParallelsLine(getBaseParallelLimit())
                .addWorkingStatusLine();
        // .addProgressLine(recipeProgressPercent());
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addLowPowerLine(insufficientEnergy())
                .addNoSpaceLine(areOutputsClogged())
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isWorkingEnabled() {
        return workable.getProgressAndComplete().isLogicEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (isWorkingAllowed != workable.getProgressAndComplete().isLogicEnabled()) {
            workable.getProgressAndComplete().setLogicEnabled(isWorkingAllowed);
            workable.getLookupAndSetup().setLogicEnabled(isWorkingAllowed);
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, b -> b.writeBoolean(isWorkingAllowed));
        }
    }

    @Override
    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, true, true, true, true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn,
                                               boolean checkMaintenance,
                                               boolean checkItemIn,
                                               boolean checkItemOut,
                                               boolean checkFluidIn,
                                               boolean checkFluidOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler);

        if (checkEnergyIn) {
            predicate = predicate.or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                    .setMaxGlobalLimited(2)
                    .setPreviewCount(1));
        }

        if (checkItemIn) {
            if (recipeMap.getMaxInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeMap.getMaxOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkFluidIn) {
            if (recipeMap.getMaxFluidInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
            }
        }
        if (checkFluidOut) {
            if (recipeMap.getMaxFluidOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
            }
        }
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isActive(), isWorkingEnabled());
    }

    @Override
    public SoundEvent getSound() {
        return getRecipeMap().getSound();
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        // if (maxProgress() > 0) {
        // list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
        // new TextComponentTranslation(TextFormattingUtil.formatNumbers(progress() / 20))
        // .setStyle(new Style().setColor(TextFormatting.GREEN)),
        // new TextComponentTranslation(
        // TextFormattingUtil.formatNumbers((long) Math.ceil(maxProgress() / 20)))
        // .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        // }

        list.add(new TextComponentTranslation("behavior.tricorder.energy_container_storage",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyStored()))
                        .setStyle(new Style().setColor(TextFormatting.GREEN)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));

        // if (recipeEUt() > 0) {
        // list.add(new TextComponentTranslation("behavior.tricorder.workable_consumption",
        // new TextComponentTranslation(TextFormattingUtil.formatNumbers(recipeEUt()))
        // .setStyle(new Style().setColor(TextFormatting.RED)),
        // new TextComponentTranslation(
        // TextFormattingUtil.formatNumbers(recipeEUt() == 0 ? 0 : 1))
        // .setStyle(new Style().setColor(TextFormatting.RED))));
        // }

        list.add(new TextComponentTranslation("behavior.tricorder.multiblock_energy_input",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getInputVoltage()))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                new TextComponentTranslation(GTValues.VN[GTUtility.getTierByVoltage(energyContainer.getInputVoltage())])
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));

        if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_maintenance",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(getNumMaintenanceProblems()))
                            .setStyle(new Style().setColor(TextFormatting.RED))));
        }

        if (getBaseParallelLimit() > 1) {
            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_parallel",
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(getBaseParallelLimit()))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN))));
        }

        return list;
    }

    @Nullable
    @Override
    public ICleanroomProvider getCleanroom() {
        return this.cleanroom;
    }

    @Override
    public void setCleanroom(@NotNull ICleanroomProvider provider) {
        if (cleanroom == null || provider.getPriority() > cleanroom.getPriority()) {
            this.cleanroom = provider;
        }
    }

    @Override
    public void unsetCleanroom() {
        this.cleanroom = null;
    }
}
