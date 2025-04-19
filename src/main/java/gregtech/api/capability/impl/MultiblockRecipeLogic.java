package gregtech.api.capability.impl;


import gregtech.api.GTValues;
import gregtech.api.capability.DualHandler;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gregtech.api.recipes.logic.OverclockingLogic.subTickParallelOC;

public class MultiblockRecipeLogic extends AbstractRecipeLogic {

    protected final Set<IItemHandlerModifiable> invalidatedInputList = new HashSet<>();
    // Used for distinct mode
    protected int lastRecipeIndex = 0;
    protected IItemHandlerModifiable currentDistinctInputBus;
    private boolean hasDualInputCache;

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity, tileEntity.recipeMap);
    }

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, tileEntity.recipeMap, hasPerfectOC);
    }

    @Override
    public void update() {}

    public void updateWorkable() {
        super.update();
    }

    @Override
    protected boolean canProgressRecipe() {
        return super.canProgressRecipe() && !((IMultiblockController) metaTileEntity).isStructureObstructed();
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    @Override
    public void invalidate() {
        super.invalidate();
        lastRecipeIndex = 0;
        invalidatedInputList.clear();
    }

    public void onDistinctChanged() {
        this.lastRecipeIndex = 0;
    }

    public IEnergyContainer getEnergyContainer() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getEnergyContainer();
    }

    @Override
    protected IItemHandlerModifiable getInputInventory() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getInputInventory();
    }

    // Used for distinct bus recipe checking
    protected List<IItemHandlerModifiable> getInputBuses() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        List<IItemHandlerModifiable> inputItems = new ArrayList<>(
                controller.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        inputItems.addAll(controller.getAbilities(MultiblockAbility.DUAL_IMPORT));
        return inputItems;
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getOutputInventory();
    }

    @Override
    protected IMultipleTankHandler getInputTank() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getInputFluidInventory();
    }

    /**
     * Overload of {@link #getInputTank()} to gather extra fluid tanks that could exist in a distinct item handler (such
     * as a {@link DualHandler})
     *
     * @param items Handler to gather fluid tanks from
     * @return a new FluidTankList with extra fluid tanks on top of the existing fluid tanks
     */
    protected IMultipleTankHandler getInputTank(IItemHandler items) {
        var tanks = new ArrayList<>(getInputTank().getFluidTanks());
        if (items instanceof IMultipleTankHandler tankHandler) {
            tanks.addAll(tankHandler.getFluidTanks());
        }
        return new FluidTankList(getInputTank().allowSameFluidFill(), tanks);
    }

    protected IMultipleTankHandler getDistinctInputTank(IItemHandler items) {
        var tanks = new ArrayList<>(getInputTank().getFluidTanks());
        tanks.clear();
        if (items instanceof IMultipleTankHandler tankHandler) {
            tanks.addAll(tankHandler.getFluidTanks());
        }
        return new FluidTankList(getInputTank().allowSameFluidFill(), tanks);
    }

    @Override
    protected IMultipleTankHandler getOutputTank() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getOutputFluidInventory();
    }

    @Override
    protected boolean canWorkWithInputs() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (!(controller instanceof RecipeMapMultiblockController distinctController) ||
                !distinctController.canBeDistinct() ||
                !distinctController.isDistinct() ||
                getInputInventory().getSlots() == 0) {
            return super.canWorkWithInputs();
        }

        // 当无效列表为空时直接通过检查
        if (invalidatedInputList.isEmpty()) {
            return true;
        }

        boolean canWork;

        // 处理流体输入通知
        if (!metaTileEntity.getNotifiedFluidInputList().isEmpty()) {
            canWork = true;
            clearNotificationLists();
        } else {
            // 处理物品输入通知
            canWork = processItemNotifications();
        }

        // 扁平化输入总线并清理无效列表中的DualHandler
        List<IItemHandler> flattenedHandlers = flattenInputBuses();
        removeDualHandlersFromInvalidated();

        // 检查无效列表是否包含所有必要处理器
        if (!new HashSet<>(invalidatedInputList).containsAll(flattenedHandlers)) {
            canWork = true;
        }

        return canWork;
    }

    // 提取方法：清空通知列表
    private void clearNotificationLists() {
        invalidatedInputList.clear();
        metaTileEntity.getNotifiedFluidInputList().clear();
        metaTileEntity.getNotifiedItemInputList().clear();
    }

    // 提取方法：处理物品输入通知
    private boolean processItemNotifications() {
        boolean updated = false;
        Iterator<IItemHandlerModifiable> notifiedIter = metaTileEntity.getNotifiedItemInputList().iterator();

        while (notifiedIter.hasNext()) {
            IItemHandlerModifiable bus = notifiedIter.next();
            Iterator<IItemHandlerModifiable> invalidatedIter = invalidatedInputList.iterator();

            while (invalidatedIter.hasNext()) {
                IItemHandler handler = invalidatedIter.next();
                if (isHandlerMatch(handler, bus)) {
                    invalidatedIter.remove();
                    updated = true;
                }
            }
            notifiedIter.remove();
        }
        return updated;
    }

    // 辅助方法：处理器匹配检查
    private boolean isHandlerMatch(IItemHandler handler, IItemHandlerModifiable bus) {
        if (handler instanceof ItemHandlerList) {
            return ((ItemHandlerList) handler).getBackingHandlers().contains(bus);
        }
        return handler == bus;
    }

    // 提取方法：扁平化输入总线
    private List<IItemHandler> flattenInputBuses() {
        return getInputBuses().stream()
                .flatMap(ih -> {
                    if (ih instanceof ItemHandlerList) {
                        return ((ItemHandlerList) ih).getBackingHandlers().stream();
                    }
                    return Stream.of(ih);
                })
                .collect(Collectors.toList());
    }

    // 提取方法：清理DualHandler
    private void removeDualHandlersFromInvalidated() {
        invalidatedInputList.removeIf(handler -> handler instanceof DualHandler);
    }

    @Override
    protected void trySearchNewRecipe() {
        // do not run recipes when there are more than 5 maintenance problems
        // Maintenance can apply to all multiblocks, so cast to a base multiblock class
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (ConfigHolder.machines.enableMaintenance && controller.hasMaintenanceMechanics() &&
                controller.getNumMaintenanceProblems() > 5) {
            return;
        }

        // Distinct buses only apply to some multiblocks, so check the controller against a lower class
        if (controller instanceof RecipeMapMultiblockController distinctController) {

            if (distinctController.canBeDistinct() && distinctController.isDistinct() &&
                    getInputInventory().getSlots() > 0) {
                trySearchNewRecipeDistinct();
                return;
            }
        }

        trySearchNewRecipeCombined();
    }

    /**
     * Put into place so multiblocks can override {@link AbstractRecipeLogic#trySearchNewRecipe()} without having to
     * deal with the maintenance and distinct logic in {@link MultiblockRecipeLogic#trySearchNewRecipe()}
     */
    protected void trySearchNewRecipeCombined() {
        super.trySearchNewRecipe();
    }

    protected void trySearchNewRecipeDistinct() {
        long maxVoltage = getMaxVoltage();
        List<IItemHandlerModifiable> importInventory = getInputBuses();

        // 优先尝试缓存命中
        if (attemptCacheHit(importInventory)) {
            return;
        }

        // 更新双输入缓存状态
        updateHasDualInputCache();

        // 遍历总线寻找配方，优先检查上次成功总线
        findRecipeInBuses(importInventory, maxVoltage);
    }

    // 其他方法保持不变，以下为修改后的方法

    private void updateHasDualInputCache() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        hasDualInputCache = !controller.getAbilities(MultiblockAbility.DUAL_IMPORT).isEmpty();
    }

    private boolean hasDualInput() {
        return hasDualInputCache;
    }

    private void findRecipeInBuses(List<IItemHandlerModifiable> importInventory, long maxVoltage) {
        // 优先检查上次成功的总线
        if (lastRecipeIndex >= 0 && lastRecipeIndex < importInventory.size()) {
            IItemHandlerModifiable bus = importInventory.get(lastRecipeIndex);
            if (!isBusInvalid(bus)) {
                Recipe recipe = findRecipeForBus(bus, maxVoltage);
                if (handleFoundRecipe(recipe, bus, lastRecipeIndex)) {
                    return;
                }
            }
        }

        // 遍历剩余总线
        for (int i = 0; i < importInventory.size(); i++) {
            if (i == lastRecipeIndex) continue; // 跳过已检查的总线

            IItemHandlerModifiable bus = importInventory.get(i);
            if (isBusInvalid(bus)) continue;

            Recipe recipe = findRecipeForBus(bus, maxVoltage);
            if (handleFoundRecipe(recipe, bus, i)) return;
        }
    }

    private boolean checkPreviousRecipeDistinct(IItemHandlerModifiable previousBus) {
        boolean dualInput = hasDualInput();
        IMultipleTankHandler tank = dualInput ? getDistinctInputTank(previousBus) : getInputTank(previousBus);
        return previousRecipe != null && previousRecipe.matches(false, previousBus, tank);
    }

    protected boolean checkLatestRecipeDistinct(IItemHandlerModifiable previousBus) {
        boolean dualInput = hasDualInput();
        IMultipleTankHandler tank = dualInput ? getDistinctInputTank(previousBus) : getInputTank(previousBus);

        // 逆序遍历最新配方（跳过最后一个）
        for (int i = latestRecipes.size() - 2; i >= 0; i--) { // 从倒数第二个开始
            Recipe recipe = latestRecipes.get(i);
            if (recipe != null && recipe.matches(false, previousBus, tank)) {
                return checkRecipe(recipe) && prepareRecipeDistinct(recipe);
            }
        }
        return false;
    }

    protected boolean prepareRecipeDistinct(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, getRecipeMap(), metaTileEntity.getItemOutputLimit(),
                metaTileEntity.getFluidOutputLimit());
        boolean dualInput = hasDualInput();
        IMultipleTankHandler inputTank =
                dualInput ? getDistinctInputTank(currentDistinctInputBus) : getInputTank(currentDistinctInputBus);

        recipe = findParallelRecipe(
                recipe,
                currentDistinctInputBus,
                inputTank,
                getOutputInventory(),
                getOutputTank(),
                getMaxParallelVoltage(),
                getParallelLimit());

        if (recipe != null) {
            recipe = setupAndConsumeRecipeInputs(recipe, currentDistinctInputBus, inputTank);
            if (recipe != null) {
                setupRecipe(recipe);
                return true;
            }
        }
        return false;
    }

    // 提取方法：尝试缓存命中
    private boolean attemptCacheHit(List<IItemHandlerModifiable> importInventory) {
        //原先的方法
        if (canUseCachedRecipe(importInventory) && prepareRecipeDistinct(previousRecipe))
            return true;

        //临近搜索系统
        return canUseLatestRecipe(importInventory);
    }

    // 提取方法：检查缓存有效性
    private boolean canUseCachedRecipe(List<IItemHandlerModifiable> importInventory) {
        return previousRecipe != null
                && lastRecipeIndex < importInventory.size()
                && checkPreviousRecipeDistinct(importInventory.get(lastRecipeIndex))
                && checkRecipe(previousRecipe);
    }

    private boolean canUseLatestRecipe(List<IItemHandlerModifiable> importInventory) {
        return previousRecipe != null
                && lastRecipeIndex < importInventory.size()
                && checkLatestRecipeDistinct(importInventory.get(lastRecipeIndex));
    }

    // 提取方法：检查总线有效性
    private boolean isBusInvalid(IItemHandlerModifiable bus) {
        return invalidatedInputList.contains(bus);
    }

    // 提取方法：获取对应总线配方
    private Recipe findRecipeForBus(IItemHandlerModifiable bus, long maxVoltage) {
        return hasDualInput()
                ? findRecipe(maxVoltage, bus, getDistinctInputTank(bus))
                : findRecipe(maxVoltage, bus, getInputTank(bus));
    }

    // 提取方法：处理找到的配方
    private boolean handleFoundRecipe(Recipe recipe, IItemHandlerModifiable bus, int index) {
        if (recipe != null && checkRecipe(recipe)) {
            updateRecipeCache(recipe, bus, index);
            return prepareRecipeDistinct(recipe);
        } else {
            markBusAsInvalid(bus);
            return false;
        }
    }

    // 提取方法：更新配方缓存
    private void updateRecipeCache(Recipe recipe, IItemHandlerModifiable bus, int index) {
        previousRecipe = recipe;
        addToPreviousRecipes(recipe);
        currentDistinctInputBus = bus;
        lastRecipeIndex = index;
    }

    // 提取方法：标记无效总线
    private void markBusAsInvalid(IItemHandlerModifiable bus) {
        invalidatedInputList.add(bus);
    }

    @Override
    public void invalidateInputs() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;
        if (distinctController.canBeDistinct() && distinctController.isDistinct() &&
                !(getInputInventory() instanceof DualHandler) &&
                getInputInventory().getSlots() > 0) {
            invalidatedInputList.add(currentDistinctInputBus);
        } else {
            super.invalidateInputs();
        }
    }

    @Override
    protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull RecipePropertyStorage storage) {
        super.modifyOverclockPre(ocParams, storage);

        // apply maintenance bonuses
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration bonus
        if (maintenanceValues.getSecond() != 1.0) {
            ocParams.setDuration((int) Math.round(ocParams.duration() * maintenanceValues.getSecond()));
        }
    }

    @Override
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
        subTickParallelOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(),
                getOverclockingVoltageFactor());
    }

    @Override
    protected void modifyOverclockPost(@NotNull OCResult ocResult, @NotNull RecipePropertyStorage storage) {
        super.modifyOverclockPost(ocResult, storage);
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration penalty
        if (maintenanceValues.getFirst() > 0) {
            ocResult.setDuration((int) (ocResult.duration() * (1 + 0.1 * maintenanceValues.getFirst())));
        }
    }

    @Override
    public long getMaximumOverclockVoltage() {
        IEnergyContainer energyContainer = getEnergyContainer();
        if (energyContainer instanceof EnergyContainerList) {
            long voltage;
            long amperage;
            if (energyContainer.getInputVoltage() > energyContainer.getOutputVoltage()) {
                voltage = energyContainer.getInputVoltage();
                amperage = energyContainer.getInputAmperage();
            } else {
                voltage = energyContainer.getOutputVoltage();
                amperage = energyContainer.getOutputAmperage();
            }

            if (amperage == 1) {
                // amperage is 1 when the energy is not exactly on a tier

                // the voltage for recipe search is always on tier, so take the closest lower tier
                return GTValues.VOC[GTUtility.getFloorTierByVoltage(voltage)];
            } else {
                // amperage != 1 means the voltage is exactly on a tier
                // ignore amperage, since only the voltage is relevant for recipe search
                // amps are never > 3 in an EnergyContainerList
                return voltage;
            }
        }
        return Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
    }

    @NotNull
    protected Tuple<Integer, Double> getMaintenanceValues() {
        MultiblockWithDisplayBase displayBase = this.metaTileEntity instanceof MultiblockWithDisplayBase ?
                (MultiblockWithDisplayBase) metaTileEntity : null;
        int numMaintenanceProblems = displayBase == null || !displayBase.hasMaintenanceMechanics() ||
                !ConfigHolder.machines.enableMaintenance ? 0 : displayBase.getNumMaintenanceProblems();
        double durationMultiplier = 1.0D;
        if (displayBase != null && displayBase.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) {
            durationMultiplier = displayBase.getMaintenanceDurationMultiplier();
        }
        return new Tuple<>(numMaintenanceProblems, durationMultiplier);
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false)) {
            controller.checkRecipe(recipe, true);
            return super.checkRecipe(recipe);
        }
        return false;
    }

    @Override
    protected void completeRecipe() {
        performMufflerOperations();
        super.completeRecipe();
    }

    protected void performMufflerOperations() {
        if (metaTileEntity instanceof MultiblockWithDisplayBase controller) {
            // output muffler items
            if (controller.hasMufflerMechanics()) {
                if (parallelRecipesPerformed > 1) {
                    controller.outputRecoveryItems(parallelRecipesPerformed);
                } else {
                    controller.outputRecoveryItems();
                }
            }
        }
    }

    @Override
    public long getMaxVoltage() {
        IEnergyContainer energyContainer = getEnergyContainer();
        if (!consumesEnergy()) {
            // Generator Multiblocks
            long voltage = energyContainer.getOutputVoltage();
            long amperage = energyContainer.getOutputAmperage();
            if (energyContainer instanceof EnergyContainerList && amperage == 1) {
                // Amperage is 1 when the energy is not exactly on a tier.
                // The voltage for recipe search is always on tier, so take the closest lower tier.
                // List check is done because single hatches will always be a "clean voltage," no need
                // for any additional checks.
                return GTValues.VOC[GTUtility.getFloorTierByVoltage(voltage)];
            }
            return voltage;
        } else {
            // Machine Multiblocks
            if (energyContainer instanceof EnergyContainerList energyList) {
                long highestVoltage = energyList.getHighestInputVoltage();
                if (energyList.getNumHighestInputContainers() > 1) {
                    // allow tier + 1 if there are multiple hatches present at the highest tier
                    int tier = GTUtility.getTierByVoltage(highestVoltage);
                    return GTValues.V[Math.min(tier + 1, GTValues.MAX)];
                } else {
                    return highestVoltage;
                }
            } else {
                return energyContainer.getInputVoltage();
            }
        }
    }

    @Override
    protected long getMaxParallelVoltage() {
        return getMaximumOverclockVoltage();
    }

    @Nullable
    @Override
    public RecipeMap<?> getRecipeMap() {
        // if the multiblock has more than one RecipeMap, return the currently selected one
        if (metaTileEntity instanceof IMultipleRecipeMaps)
            return ((IMultipleRecipeMaps) metaTileEntity).getCurrentRecipeMap();
        return super.getRecipeMap();
    }
}
