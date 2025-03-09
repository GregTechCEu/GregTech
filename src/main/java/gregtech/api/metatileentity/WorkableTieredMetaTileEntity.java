package gregtech.api.metatileentity;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IHasRecipeMap;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.running.RecipeFinalizer;
import gregtech.api.recipes.logic.statemachine.running.RecipeProgressOperation;
import gregtech.api.recipes.logic.workable.RecipeWorkable;
import gregtech.api.recipes.lookup.property.CleanroomFulfilmentProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

public abstract class WorkableTieredMetaTileEntity extends TieredMetaTileEntity
                                                   implements IDataInfoProvider, ICleanroomReceiver, IHasRecipeMap,
                                                   IControllable, RecipeWorkable.ISupportsRecipeWorkable {

    protected RecipeWorkable workable;

    protected final RecipeMap<?> recipeMap;
    protected final ICubeRenderer renderer;

    private final Function<Integer, Integer> tankScalingFunction;

    public final boolean handlesRecipeOutputs;

    protected Deque<ItemStack> bufferedItemOutputs;
    protected Deque<FluidStack> bufferedFluidOutputs;

    @Nullable
    private ICleanroomProvider cleanroom;

    public WorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                        ICubeRenderer renderer, int tier,
                                        Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, true);
    }

    public WorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                        ICubeRenderer renderer, int tier,
                                        Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
        this.handlesRecipeOutputs = handlesRecipeOutputs;
        this.recipeMap = recipeMap;
        this.bufferedItemOutputs = new ArrayDeque<>();
        this.bufferedFluidOutputs = new ArrayDeque<>();
        RecipeStandardStateMachineBuilder std = new RecipeStandardStateMachineBuilder(recipeMap::getLookup);
        modifyRecipeLogicStandardBuilder(std);
        this.workable = new RecipeWorkable(this, std);
        this.tankScalingFunction = tankScalingFunction;
        initializeInventory();
        reinitializeEnergyContainer();
    }

    @Override
    protected void initializeInventory() {
        if (recipeMap != null) super.initializeInventory();
    }

    /**
     * Called during initialization. Should not require any fields to be populated for the state machine to be
     * constructed.
     * Override this if you merely need to set up the standard state machine builder.
     */
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        builder.setOffthreadSearchAndSetup(true)
                .setItemInput(this::getImportItems)
                .setFluidInput(this::getImportFluids)
                .setProperties(this::computePropertySet)
                .setFluidOutput(bufferedFluidOutputs::addAll)
                .setFluidTrim(this::getFluidOutputLimit)
                .setItemOutput(bufferedItemOutputs::addAll)
                .setItemTrim(this::getItemOutputLimit)
                .setNotifiedFluidInputs(this::getNotifiedFluidInputList)
                .setNotifiedItemInputs(this::getNotifiedItemInputList)
                .setItemOutAmountLimit(() -> getExportItems().getSlots() * 64)
                .setItemOutStackLimit(() -> getExportItems().getSlots())
                .setFluidOutAmountLimit(() -> {
                    int sum = 0;
                    for (var e : getExportFluids().getFluidTanks()) {
                        int capacity = e.getCapacity();
                        sum += capacity;
                    }
                    return sum;
                })
                .setFluidOutStackLimit(() -> getExportFluids().getTanks())
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

    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.comprehensive(getEnergyContainer().getInputVoltage(), 1, getEnergyContainer().getOutputVoltage(),
                getEnergyContainer().getOutputAmperage());

        ICleanroomProvider prov = getCleanroom();
        if (prov != null) {
            set.add(new CleanroomFulfilmentProperty(c -> prov.isClean() && prov.checkCleanroomType(c)));
        }
        return set;
    }

    protected IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Nullable
    @Override
    public RecipeMap<?> getRecipeMap() {
        return recipeMap;
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 64L, tierVoltage, 2, 0L, 0L) {

            @Override
            public long getInputAmperage() {
                if (getEnergyCapacity() / 2 > getEnergyStored() && isWorkingEnabled()) {
                    return 2;
                }
                return 1;
            }
        };
    }

    @Override
    public void update() {
        super.update();
        updateBufferedOutputs();
    }

    @Override
    public boolean shouldRecipeWorkableUpdate() {
        return true;
    }

    @Override
    public boolean areOutputsClogged() {
        updateBufferedOutputs();
        return !bufferedItemOutputs.isEmpty() || !bufferedFluidOutputs.isEmpty();
    }

    protected void updateBufferedOutputs() {
        while (!bufferedItemOutputs.isEmpty()) {
            ItemStack first = bufferedItemOutputs.removeFirst();
            ItemStack remainder = GTTransferUtils.insertItem(getExportItems(), first, false);
            if (!remainder.isEmpty() && !canVoidRecipeItemOutputs()) {
                bufferedItemOutputs.addFirst(remainder);
                break;
            }
        }
        while (!bufferedFluidOutputs.isEmpty()) {
            FluidStack first = bufferedFluidOutputs.peekFirst();
            first.amount -= getExportFluids().fill(first, true);
            if (first.amount <= 0 || canVoidRecipeFluidOutputs()) {
                bufferedFluidOutputs.removeFirst();
            } else {
                break;
            }
        }
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
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            setWorkingEnabled(buf.readBoolean());
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                isWorkingEnabled());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, recipeMap.getMaxInputs(), this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, recipeMap.getMaxOutputs(), this, true);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        NotifiableFluidTank[] fluidImports = new NotifiableFluidTank[recipeMap.getMaxFluidInputs()];
        for (int i = 0; i < fluidImports.length; i++) {
            NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank(
                    this.tankScalingFunction.apply(this.getTier()), this, false);
            fluidImports[i] = filteredFluidHandler;
        }
        return new FluidTankList(false, fluidImports);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        FluidTank[] fluidExports = new FluidTank[recipeMap.getMaxFluidOutputs()];
        for (int i = 0; i < fluidExports.length; i++) {
            fluidExports[i] = new NotifiableFluidTank(this.tankScalingFunction.apply(this.getTier()), this, true);
        }
        return new FluidTankList(false, fluidExports);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        if (recipeMap.getMaxFluidInputs() != 0)
            tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity",
                    this.tankScalingFunction.apply(getTier())));
    }

    public Function<Integer, Integer> getTankScalingFunction() {
        return tankScalingFunction;
    }

    protected boolean insufficientEnergy() {
        // if the energy container has less than a tenth of its capacity, we're probably low on energy.
        return isActive() && getEnergyContainer().getEnergyStored() <= getEnergyContainer().getEnergyCapacity() * 0.1;
    }

    public boolean isActive() {
        return workable.isRunning();
    }

    @Override
    public SoundEvent getSound() {
        return recipeMap.getSound();
    }

    protected double recipeProgressPercent() {
        NBTTagCompound recipe = RecipeFinalizer.getFirstActiveRecipe(workable.getProgressAndComplete().logicData());
        if (recipe == null) return 0;
        return RecipeFinalizer.progress(recipe) / RecipeFinalizer.duration(recipe);
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();

        NBTTagCompound recipe = RecipeFinalizer.getFirstActiveRecipe(workable.getProgressAndComplete().logicData());
        if (recipe != null) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(RecipeFinalizer.progress(recipe) / 20))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(RecipeFinalizer.duration(recipe) / 20))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        }

        if (energyContainer != null) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_stored_energy",
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(energyContainer.getEnergyStored()))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        }
        if (recipe != null) {
            if (RecipeFinalizer.isGenerating(recipe)) {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_production",
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(RecipeFinalizer.voltage(recipe)))
                                        .setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(RecipeFinalizer.amperage(recipe)))
                                        .setStyle(new Style().setColor(TextFormatting.RED))));
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_consumption",
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(RecipeFinalizer.voltage(recipe)))
                                        .setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(RecipeFinalizer.amperage(recipe)))
                                        .setStyle(new Style().setColor(TextFormatting.RED))));
            }
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
        this.cleanroom = provider;
    }

    @Override
    public void unsetCleanroom() {
        this.cleanroom = null;
    }
}
