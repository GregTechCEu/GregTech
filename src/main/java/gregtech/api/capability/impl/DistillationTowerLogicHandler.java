package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.utils.FluidTankHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DistillationTowerLogicHandler {

    protected final AbstractRecipeLogic workable;
    protected final IDistillationTower tower;

    public int layerCount;
    public List<IFluidHandler> orderedFluidOutputs;
    public IMultipleTankHandler fluidTanks;

    public DistillationTowerLogicHandler(IDistillationTower tower, AbstractRecipeLogic workable) {
        this.tower = tower;
        this.workable = workable;
    }

    protected boolean applyFluidToOutputs(List<FluidStack> fluids, boolean doFill) {
        boolean valid = true;
        for (int i = 0; i < fluids.size(); i++) {
            IFluidHandler handler = orderedFluidOutputs.get(i);
            int accepted = handler.fill(fluids.get(i), doFill);
            if (accepted != fluids.get(i).amount) valid = false;
            if (!doFill && !valid) break;
        }
        return valid;
    }

    public void outputRecipeOutputs() {
        GTTransferUtils.addItemsToItemHandler(workable.getOutputInventory(), false, workable.itemOutputs);
        this.applyFluidToOutputs(workable.fluidOutputs, true);
    }

    public boolean setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                               @NotNull IItemHandlerModifiable importInventory,
                                               @NotNull IMultipleTankHandler importFluids) {
        workable.overclockResults = workable.calculateOverclock(recipe);

        workable.modifyOverclockPost(workable.overclockResults, recipe.getRecipePropertyStorage());

        if (!workable.hasEnoughPower(workable.overclockResults)) {
            return false;
        }

        IItemHandlerModifiable exportInventory = workable.getOutputInventory();

        // We have already trimmed outputs and chanced outputs at this time
        // Attempt to merge all outputs + chanced outputs into the output bus, to prevent voiding chanced outputs
        if (!workable.getMetaTileEntity().canVoidRecipeItemOutputs() &&
                !GTTransferUtils.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs())) {
            workable.isOutputsFull = true;
            return false;
        }

        // Perform layerwise fluid checks
        if (!workable.getMetaTileEntity().canVoidRecipeFluidOutputs() &&
                !this.applyFluidToOutputs(recipe.getAllFluidOutputs(), false)) {
            workable.isOutputsFull = true;

            return false;
        }

        workable.isOutputsFull = false;
        if (recipe.matches(true, importInventory, importFluids)) {
            workable.getMetaTileEntity().addNotifiedInput(importInventory);
            return true;
        }
        return false;
    }

    /**
     * Needs to be overriden for multiblocks that have different assemblies than the standard distillation tower.
     *
     * @param structurePattern the structure pattern
     */
    public void determineLayerCount(@NotNull BlockPattern structurePattern) {
        this.layerCount = structurePattern.formedRepetitionCount[1] + 1;
    }

    /**
     * Needs to be overriden for multiblocks that have different assemblies than the standard distillation tower.
     */
    public void determineOrderedFluidOutputs() {
        // noinspection SimplifyStreamApiCallChains
        List<MetaTileEntityMultiblockPart> fluidExportParts = tower.getMultiblockParts().stream()
                .filter(iMultiblockPart -> iMultiblockPart instanceof IMultiblockAbilityPart<?>abilityPart &&
                        abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS &&
                        abilityPart instanceof MetaTileEntityMultiblockPart)
                .map(iMultiblockPart -> (MetaTileEntityMultiblockPart) iMultiblockPart)
                .collect(Collectors.toList());
        // the fluidExportParts should come sorted in smallest Y first, largest Y last.
        List<IFluidHandler> orderedHandlerList = new ObjectArrayList<>();
        List<IFluidTank> tankList = new ObjectArrayList<>();
        int firstY = tower.getPos().getY() + 1;
        int exportIndex = 0;
        for (int y = firstY; y < firstY + this.layerCount; y++) {
            if (fluidExportParts.size() <= exportIndex) {
                orderedHandlerList.add(FakeTank.INSTANCE);
                tankList.add(FakeTank.INSTANCE);
                continue;
            }
            MetaTileEntityMultiblockPart part = fluidExportParts.get(exportIndex);
            if (part.getPos().getY() == y) {
                List<IFluidTank> hatchTanks = new ObjectArrayList<>();
                // noinspection unchecked
                ((IMultiblockAbilityPart<IFluidTank>) part).registerAbilities(hatchTanks);
                if (hatchTanks.size() == 1)
                    orderedHandlerList.add(FluidTankHandler.getTankFluidHandler(hatchTanks.get(0)));
                else orderedHandlerList.add(new FluidTankList(false, hatchTanks));
                tankList.addAll(hatchTanks);
                exportIndex++;
            } else if (part.getPos().getY() > y) {
                orderedHandlerList.add(FakeTank.INSTANCE);
                tankList.add(FakeTank.INSTANCE);
            } else {
                GTLog.logger.error("The Distillation Tower at " + tower.getPos() +
                        " had a fluid export hatch with an unexpected Y position.");
                tower.invalidateStructure();
                this.orderedFluidOutputs = new ObjectArrayList<>();
                this.fluidTanks = new FluidTankList(false);
            }
        }
        this.orderedFluidOutputs = orderedHandlerList;
        this.fluidTanks = new FluidTankList(tower.allowSameFluidFillForOutputs(), tankList);
    }

    public void invalidate() {
        this.layerCount = 0;
        this.orderedFluidOutputs = null;
    }

    public interface IDistillationTower {

        List<IMultiblockPart> getMultiblockParts();

        BlockPos getPos();

        void invalidateStructure();

        boolean allowSameFluidFillForOutputs();
    }

    // an endless void devouring any fluid sent to it
    protected static class FakeTank implements IFluidHandler, IFluidTank {

        protected static final FakeTank INSTANCE = new FakeTank();
        public static final FluidTankInfo FAKE_TANK_INFO = new FluidTankInfo(null, Integer.MAX_VALUE);
        public static final IFluidTankProperties FAKE_TANK_PROPERTIES = new FluidTankProperties(null, Integer.MAX_VALUE,
                true, false);
        public static final IFluidTankProperties[] FAKE_TANK_PROPERTIES_ARRAY = new IFluidTankProperties[] {
                FAKE_TANK_PROPERTIES };

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return FAKE_TANK_PROPERTIES_ARRAY;
        }

        @Override
        public FluidStack getFluid() {
            return null;
        }

        @Override
        public int getFluidAmount() {
            return 0;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public FluidTankInfo getInfo() {
            return FAKE_TANK_INFO;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return resource.amount;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }
    }
}
