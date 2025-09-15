package gregtech.api.capability.impl;

import gregtech.GregTechMod;
import gregtech.api.GTValues;
import gregtech.api.capability.DualHandler;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
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

public class SteamMultiblockRecipeLogic extends AbstractRecipeLogic {

    // EU per mB
    private final double conversionRate;
    private IMultipleTankHandler steamFluidTank;
    private IFluidTank steamFluidTankCombined;
    protected final Set<IItemHandlerModifiable> invalidatedInputList = new HashSet<>();
    // Used for distinct mode
    protected int lastRecipeIndex = 0;
    protected IItemHandlerModifiable currentDistinctInputBus;
    private boolean hasDualInputCache;
    @Override
    public void invalidate() {
        super.invalidate();
        lastRecipeIndex = 0;
        invalidatedInputList.clear();
    }
    public SteamMultiblockRecipeLogic(RecipeMapSteamMultiblockController tileEntity, RecipeMap<?> recipeMap,
                                      IMultipleTankHandler steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap);
        this.steamFluidTank = steamFluidTank;
        this.conversionRate = conversionRate;
        setAllowOverclocking(false);
        combineSteamTanks();
    }
    @Override
    protected boolean canWorkWithInputs() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (!(controller instanceof RecipeMapSteamMultiblockController distinctController) ||
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

        // Distinct buses only apply to some multiblocks, so check the controller against a lower class
        if (controller instanceof RecipeMapSteamMultiblockController distinctController) {

            if (distinctController.canBeDistinct() && distinctController.isDistinct() &&
                    getInputInventory().getSlots() > 0) {
                trySearchNewRecipeDistinct();
                return;
            }
        }
        GregTechMod.LOGGER.warn("Meow !");
        trySearchNewRecipeCombined();
    }

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

    @Override
    public boolean prepareRecipe(Recipe recipe) {
        ((RecipeMapSteamMultiblockController) metaTileEntity).refreshAllBeforeConsumption();
        return super.prepareRecipe(recipe);
    }

    protected boolean prepareRecipeDistinct(Recipe recipe) {
        ((RecipeMapSteamMultiblockController) metaTileEntity).refreshAllBeforeConsumption();
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
        RecipeMapSteamMultiblockController distinctController = (RecipeMapSteamMultiblockController) controller;
        if (distinctController.canBeDistinct() && distinctController.isDistinct() &&
                !(getInputInventory() instanceof DualHandler) &&
                getInputInventory().getSlots() > 0) {
            invalidatedInputList.add(currentDistinctInputBus);
        } else {
            super.invalidateInputs();
        }
    }
    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false)) {
            controller.checkRecipe(recipe, true);
            return super.checkRecipe(recipe);
        }
        return false;
    }
    @Nullable
    @Override
    public RecipeMap<?> getRecipeMap() {
        // if the multiblock has more than one RecipeMap, return the currently selected one
        if (metaTileEntity instanceof IMultipleRecipeMaps)
            return ((IMultipleRecipeMaps) metaTileEntity).getCurrentRecipeMap();
        return super.getRecipeMap();
    }
    private void clearNotificationLists() {
        invalidatedInputList.clear();
        metaTileEntity.getNotifiedFluidInputList().clear();
        metaTileEntity.getNotifiedItemInputList().clear();
    }
    protected List<IItemHandlerModifiable> getInputBuses() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        List<IItemHandlerModifiable> inputItems = new ArrayList<>(
                controller.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        inputItems.addAll(controller.getAbilities(MultiblockAbility.DUAL_IMPORT));
        return inputItems;
    }
    @Override
    protected boolean canProgressRecipe() {
        return super.canProgressRecipe() && !((IMultiblockController) metaTileEntity).isStructureObstructed();
    }
    public void onDistinctChanged() {
        this.lastRecipeIndex = 0;
    }
    public IFluidTank getSteamFluidTankCombined() {
        combineSteamTanks();
        return steamFluidTankCombined;
    }
    @Override
    protected IMultipleTankHandler getInputTank() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getInputFluidInventory();
    }

    protected IMultipleTankHandler getInputTank(IItemHandler items) {
        var tanks = new ArrayList<>(getInputTank().getFluidTanks());
        if (items instanceof IMultipleTankHandler tankHandler) {
            tanks.addAll(tankHandler.getFluidTanks());
        }
        return new FluidTankList(getInputTank().allowSameFluidFill(), tanks);
    }
    @Override
    protected IMultipleTankHandler getOutputTank() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getOutputFluidInventory();
    }
    @Override
    protected IItemHandlerModifiable getInputInventory() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getInputInventory();
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getOutputInventory();
    }

    protected IMultipleTankHandler getSteamFluidTank() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getSteamFluidTank();
    }

    private void combineSteamTanks() {
        steamFluidTank = getSteamFluidTank();
        if (steamFluidTank == null)
            steamFluidTankCombined = new FluidTank(0);
        else if (steamFluidTank.getTanks() == 0) {
            int capacity = steamFluidTank.getTanks() * 64000;
            steamFluidTankCombined = new FluidTank(capacity);
            steamFluidTankCombined.fill(steamFluidTank.drain(capacity, false), true);
        } else if (steamFluidTank.getTanks() == 1) {
            IMultipleTankHandler.ITankEntry tankHatch = steamFluidTank.getTankAt(0);
            int capacity = tankHatch.getCapacity();
            steamFluidTankCombined = new FluidTank(capacity);
            steamFluidTankCombined.fill(steamFluidTank.drain(capacity, false), true);
        }
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
    public void update() {
        // Fixes an annoying GTCE bug in AbstractRecipeLogic
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        if (isActive && !controller.isStructureFormed()) {
            progressTime = 0;
            wasActiveAndNeedsUpdate = true;
        }

        combineSteamTanks();
        super.update();
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        combineSteamTanks();
        return (long) Math.ceil(steamFluidTankCombined.getFluidAmount() * conversionRate);
    }

    @Override
    protected long getEnergyCapacity() {
        combineSteamTanks();
        return (long) Math.floor(steamFluidTankCombined.getCapacity() * conversionRate);
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        combineSteamTanks();
        int resultDraw = GTUtility.safeCastLongToInt((long) Math.ceil(recipeEUt / conversionRate));
        return resultDraw >= 0 && steamFluidTankCombined.getFluidAmount() >= resultDraw &&
                steamFluidTank.drain(resultDraw, !simulate) != null;
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.V[GTValues.LV];
    }

    @Override
    public boolean isAllowOverclocking() {
        return false;
    }

    @Override
    protected @Nullable Recipe setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                           @NotNull IItemHandlerModifiable importInventory) {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false)) {
            recipe = super.setupAndConsumeRecipeInputs(recipe, importInventory);
            if (recipe != null) {
                controller.checkRecipe(recipe, true);
                return recipe;
            }
        }
        return null;
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
        ventSteam();
    }

    private void ventSteam() {
        BlockPos machinePos = metaTileEntity.getPos();
        EnumFacing ventingSide = metaTileEntity.getFrontFacing();
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = metaTileEntity.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(metaTileEntity.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            performVentingAnimation(machinePos, ventingSide);
        } else if (blockOnPos.getBlock() == Blocks.SNOW_LAYER && blockOnPos.getValue(BlockSnow.LAYERS) == 1) {
            performVentingAnimation(machinePos, ventingSide);
            metaTileEntity.getWorld().destroyBlock(ventingBlockPos, false);
        }
    }

    private void performVentingAnimation(BlockPos machinePos, EnumFacing ventingSide) {
        WorldServer world = (WorldServer) metaTileEntity.getWorld();
        double posX = machinePos.getX() + 0.5 + ventingSide.getXOffset() * 0.6;
        double posY = machinePos.getY() + 0.5 + ventingSide.getYOffset() * 0.6;
        double posZ = machinePos.getZ() + 0.5 + ventingSide.getZOffset() * 0.6;

        world.spawnParticle(EnumParticleTypes.CLOUD, posX, posY, posZ,
                7 + GTValues.RNG.nextInt(3),
                ventingSide.getXOffset() / 2.0,
                ventingSide.getYOffset() / 2.0,
                ventingSide.getZOffset() / 2.0, 0.1);
        if (ConfigHolder.machines.machineSounds && !metaTileEntity.isMuffled()) {
            world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f,
                    1.0f);
        }
    }

    @Override
    protected boolean hasEnoughPower(long eut, int duration) {
        long totalSteam = (long) (eut * duration / conversionRate);
        if (totalSteam > 0) {
            long steamStored = getEnergyStored();
            long steamCapacity = getEnergyCapacity();
            // if the required steam is larger than the full buffer, just require the full buffer
            if (steamCapacity < totalSteam) {
                return steamCapacity == steamStored;
            }
            // otherwise require the full amount of steam for the recipe
            return steamStored >= totalSteam;
        }
        // generation case unchanged
        return super.hasEnoughPower(eut, duration);
    }
}
