package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.interfaces.IRefreshBeforeConsumption;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gtqt.api.util.GTQTUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AdvanceRecipeMapMultiblockController extends RecipeMapMultiblockController
        implements IDataInfoProvider,
                   ICleanroomReceiver,
                   IDistinctBusController {

    public final RecipeMap<?> recipeMap;
    protected ArrayList<MultiblockRecipeLogic> recipeMapWorkable = new ArrayList<>();
    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;
    protected List<IRefreshBeforeConsumption> refreshBeforeConsumptions;

    protected int thread = 1;

    private boolean isDistinct = false;

    @Nullable
    private ICleanroomProvider cleanroom;

    public AdvanceRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
        this.recipeMap = recipeMap;
        recipeMapWorkable.add(new MultiblockRecipeLogic(this));
        this.refreshBeforeConsumptions = new ArrayList<>();
        resetTileAbilities();
    }

    public void refreshAllBeforeConsumption() {
        for (IRefreshBeforeConsumption refresh : refreshBeforeConsumptions) {
            refresh.refreshBeforeConsumption();
        }
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

    public ArrayList<MultiblockRecipeLogic> getRecipeMapWorkableList() {
        return recipeMapWorkable;
    }

    /**
     * Performs extra checks for validity of given recipe before multiblock will start it's processing.
     */
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();

        thread = this.getAbilities(MultiblockAbility.THREAD_HATCH).isEmpty() ? 1 :
                this.getAbilities(MultiblockAbility.THREAD_HATCH).get(0).getCurrentThread();

        recipeMapWorkable = new ArrayList<>();
        for (int i = 0; i < thread; i++)
            recipeMapWorkable.add(new MultiblockRecipeLogic(this));
    }

    public void refreshThread(int thread) {
        if (!checkWorkingEnable()) {
            recipeMapWorkable = new ArrayList<>();
            for (int i = 0; i < thread; i++)
                recipeMapWorkable.add(new MultiblockRecipeLogic(this));
        }
    }

    public int getThread() {
        thread = this.getAbilities(MultiblockAbility.THREAD_HATCH).isEmpty() ? 1 :
                this.getAbilities(MultiblockAbility.THREAD_HATCH).get(0).getCurrentThread();
        return thread;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable)
            multiblockRecipeLogic.invalidate();
    }

    @Override
    protected void updateFormedValid() {
        if (!hasMufflerMechanics() || isMufflerFaceFree()) {
            for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable)
                multiblockRecipeLogic.updateWorkable();
        }
    }

    public boolean checkActive() {
        for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable)
            if (multiblockRecipeLogic.isActive()) return true;
        return false;
    }

    public boolean checkWorkingEnable() {
        for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable)
            if (multiblockRecipeLogic.isActive()) return true;
        return false;
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() &&
                checkActive() &&
                checkWorkingEnable();
    }

    protected void initializeAbilities() {
        List<IItemHandler> inputItems = new ArrayList<>(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        inputItems.addAll(getAbilities(MultiblockAbility.DUAL_IMPORT));
        this.inputInventory = new ItemHandlerList(inputItems);

        List<IMultipleTankHandler> inputFluids = new ArrayList<>(getAbilities(MultiblockAbility.DUAL_IMPORT));
        inputFluids.add(new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS)));
        this.inputFluidInventory = GTQTUtility.mergeTankHandlers(inputFluids, true);

        List<IItemHandler> outputItems = new ArrayList<>(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        outputItems.addAll(getAbilities(MultiblockAbility.DUAL_EXPORT));
        this.outputInventory = new ItemHandlerList(outputItems);
        List<IMultipleTankHandler> outputFluids = new ArrayList<>(getAbilities(MultiblockAbility.DUAL_EXPORT));
        outputFluids.add(new FluidTankList(false, getAbilities(MultiblockAbility.EXPORT_FLUIDS)));
        this.outputFluidInventory = GTQTUtility.mergeTankHandlers(outputFluids, false);

        List<IEnergyContainer> inputEnergy = new ArrayList<>(getAbilities(MultiblockAbility.INPUT_ENERGY));
        inputEnergy.addAll(getAbilities(MultiblockAbility.SUBSTATION_INPUT_ENERGY));
        inputEnergy.addAll(getAbilities(MultiblockAbility.INPUT_LASER));
        this.energyContainer = new EnergyContainerList(inputEnergy);

        for (IMultiblockPart part : getMultiblockParts()) {
            if (part instanceof IRefreshBeforeConsumption refresh) {
                refreshBeforeConsumptions.add(refresh);
            }
        }
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 288, 208);
        ///////////////////////////多线程组件
        builder.image(192, 4, 90, 200, GuiTextures.DISPLAY);
        var scroll = new ScrollableListWidget(196, 8, 86, 196);
        AdvancedTextWidget textWidget = new AdvancedTextWidget(0, 0, this::addInfo, 0xFFFFFF);
        scroll.addWidget(textWidget);
        builder.widget(scroll);
        ///////////////////////////Main GUI
        // Display
        if (this instanceof IProgressBarMultiblock progressMulti && progressMulti.showProgressBar()) {
            builder.image(4, 4, 190, 109, GuiTextures.DISPLAY);

            if (progressMulti.getNumProgressBars() == 3) {
                // triple bar
                ProgressWidget progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(0),
                        4, 115, 62, 7,
                        progressMulti.getProgressBarTexture(0), ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
                builder.widget(progressBar);

                progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(1),
                        68, 115, 62, 7,
                        progressMulti.getProgressBarTexture(1), ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 1));
                builder.widget(progressBar);

                progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(2),
                        132, 115, 62, 7,
                        progressMulti.getProgressBarTexture(2), ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 2));
                builder.widget(progressBar);
            } else if (progressMulti.getNumProgressBars() == 2) {
                // double bar
                ProgressWidget progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(0),
                        4, 115, 94, 7,
                        progressMulti.getProgressBarTexture(0), ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
                builder.widget(progressBar);

                progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(1),
                        100, 115, 94, 7,
                        progressMulti.getProgressBarTexture(1), ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 1));
                builder.widget(progressBar);
            } else {
                // single bar
                ProgressWidget progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(0),
                        4, 115, 190, 7,
                        progressMulti.getProgressBarTexture(0), ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
                builder.widget(progressBar);
            }
            builder.widget(new IndicatorImageWidget(174, 93, 17, 17, getLogo())
                    .setWarningStatus(getWarningLogo(), this::addWarningText)
                    .setErrorStatus(getErrorLogo(), this::addErrorText));
        } else {
            builder.image(4, 4, 190, 117, GuiTextures.DISPLAY);
            builder.widget(new IndicatorImageWidget(174, 101, 17, 17, getLogo())
                    .setWarningStatus(getWarningLogo(), this::addWarningText)
                    .setErrorStatus(getErrorLogo(), this::addErrorText));
        }

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(181)
                .setClickHandler(this::handleDisplayClick));

        // Power Button
        // todo in the future, refactor so that this class is instanceof IControllable.
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(new ImageCycleButtonWidget(173, 183, 18, 18, GuiTextures.BUTTON_POWER,
                    controllable::isWorkingEnabled, controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }

        // Voiding Mode Button
        if (shouldShowVoidingModeButton()) {
            builder.widget(new ImageCycleButtonWidget(173, 161, 18, 18, GuiTextures.BUTTON_VOID_MULTIBLOCK,
                    4, this::getVoidingMode, this::setVoidingMode)
                    .setTooltipHoverString(MultiblockWithDisplayBase::getVoidingModeTooltip));
        } else {
            builder.widget(new ImageWidget(173, 161, 18, 18, GuiTextures.BUTTON_VOID_NONE)
                    .setTooltip("gregtech.gui.multiblock_voiding_not_supported"));
        }

        // Distinct Buses Button
        if (this.canBeDistinct()) {
            builder.widget(new ImageCycleButtonWidget(173, 143, 18, 18, GuiTextures.BUTTON_DISTINCT_BUSES,
                    this::isDistinct, this::setDistinct)
                    .setTooltipHoverString(i -> "gregtech.multiblock.universal.distinct_" +
                            (i == 0 ? "disabled" : "enabled")));
        }

        // Flex Button
        builder.widget(getFlexButton(173, 125, 18, 18));

        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }

    protected void addInfo(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("总线程数：%s", recipeMapWorkable.size()));
        int i = 1;
        for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable) {
            textList.add(new TextComponentTranslation(">>线程：%s", i++));

            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.parallel",
                    multiblockRecipeLogic.getParallelLimit()));

            int currentProgress = (int) (multiblockRecipeLogic.getProgressPercent() * 100);
            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.progress",
                    currentProgress));
        }
    }

    private void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
        this.refreshBeforeConsumptions.clear();
    }

    protected IMultipleTankHandler extendedImportFluidList(IMultipleTankHandler fluids) {
        List<IFluidTank> tanks = new ArrayList<>(fluids.getFluidTanks());
        // iterate import items to look for and tanks that we might have missed
        // honestly this might not be worth checking because
        // it might already be handled in ARL/MRL
        for (var handler : getAbilities(MultiblockAbility.IMPORT_ITEMS)) {
            if (handler instanceof IFluidTank tank) {
                if (!tanks.contains(tank)) tanks.add(tank);
            } else if (handler instanceof IMultipleTankHandler multipleTankHandler) {
                for (var tank : multipleTankHandler.getFluidTanks()) {
                    if (!tanks.contains(tank)) tanks.add(tank);
                }
            }
        }
        for (var handler : getAbilities(MultiblockAbility.DUAL_IMPORT)) {
            for (var tank : handler.getFluidTanks()) {
                if (!tanks.contains(tank)) tanks.add(tank);
            }
        }

        return new FluidTankList(allowSameFluidFillForOutputs(), tanks);
    }

    public boolean allowSameFluidFillForOutputs() {
        return true;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.Builder builder = MultiblockDisplayText.builder(textList, isStructureFormed());

        builder.setWorkingStatus(checkWorkingEnable(), checkActive());
        builder.addWorkingStatusLine()
                .addEnergyUsageLine(recipeMapWorkable.get(0).getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.get(0).getMaxVoltage()));

    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.Builder builder = MultiblockDisplayText.builder(textList, isStructureFormed(), false);

        for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable)
            builder.addLowPowerLine(multiblockRecipeLogic.isHasNotEnoughEnergy());

        builder.addMaintenanceProblemLines(getMaintenanceProblems());

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
        if (checkItemIn || checkFluidIn) {
            if (recipeMap.getMaxInputs() > 0 || recipeMap.getMaxFluidInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.DUAL_IMPORT).setPreviewCount(1));
            }
        }
        if (checkItemOut || checkFluidOut) {
            if (recipeMap.getMaxOutputs() > 0 || recipeMap.getMaxFluidOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.DUAL_EXPORT).setPreviewCount(1));
            }
        }

        predicate = predicate
                .or(abilities(MultiblockAbility.THREAD_HATCH).setMaxGlobalLimited(1).setPreviewCount(1));
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                checkActive(), checkWorkingEnable());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isDistinct", isDistinct);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isDistinct = data.getBoolean("isDistinct");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isDistinct);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isDistinct = buf.readBoolean();
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
        for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable)
            multiblockRecipeLogic.onDistinctChanged();
        getMultiblockParts().forEach(part -> part.onDistinctChange(isDistinct));
        // mark buses as changed on distinct toggle
        if (this.isDistinct) {
            this.notifiedItemInputList
                    .addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
            this.notifiedItemInputList
                    .addAll(this.getAbilities(MultiblockAbility.DUAL_IMPORT));
        } else {
            this.notifiedItemInputList.add(this.inputInventory);
        }
    }

    @Override
    public SoundEvent getSound() {
        return recipeMap.getSound();
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        for (MultiblockRecipeLogic multiblockRecipeLogic : recipeMapWorkable) {
            if (multiblockRecipeLogic.getMaxProgress() > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(multiblockRecipeLogic.getProgress() / 20))
                                .setStyle(new Style().setColor(TextFormatting.GREEN)),
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(multiblockRecipeLogic.getMaxProgress() / 20))
                                .setStyle(new Style().setColor(TextFormatting.YELLOW))));
            }

            list.add(new TextComponentTranslation("behavior.tricorder.energy_container_storage",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyStored()))
                            .setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()))
                            .setStyle(new Style().setColor(TextFormatting.YELLOW))));

            if (multiblockRecipeLogic.getRecipeEUt() > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_consumption",
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(multiblockRecipeLogic.getRecipeEUt()))
                                .setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(multiblockRecipeLogic.getRecipeEUt() == 0 ? 0 : 1))
                                .setStyle(new Style().setColor(TextFormatting.RED))));
            }

            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_energy_input",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getInputVoltage()))
                            .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                    new TextComponentTranslation(
                            GTValues.VN[GTUtility.getTierByVoltage(energyContainer.getInputVoltage())])
                            .setStyle(new Style().setColor(TextFormatting.YELLOW))));

            if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
                list.add(new TextComponentTranslation("behavior.tricorder.multiblock_maintenance",
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(getNumMaintenanceProblems()))
                                .setStyle(new Style().setColor(TextFormatting.RED))));
            }

            if (multiblockRecipeLogic.getParallelLimit() > 1) {
                list.add(new TextComponentTranslation("behavior.tricorder.multiblock_parallel",
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(multiblockRecipeLogic.getParallelLimit()))
                                .setStyle(new Style().setColor(TextFormatting.GREEN))));
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
        if (cleanroom == null || provider.getPriority() > cleanroom.getPriority()) {
            this.cleanroom = provider;
        }
    }

    @Override
    public void unsetCleanroom() {
        this.cleanroom = null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.thread_multiblock.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.thread_multiblock.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.thread_multiblock.tooltip.3"));
    }
}
