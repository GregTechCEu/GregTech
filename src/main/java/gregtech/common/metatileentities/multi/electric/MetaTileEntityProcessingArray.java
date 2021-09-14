package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.*;
import gregtech.api.recipes.builders.*;
import gregtech.api.recipes.logic.ParallelLogic;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;

public class MetaTileEntityProcessingArray extends RecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {
            MultiblockAbility.IMPORT_ITEMS,
            MultiblockAbility.EXPORT_ITEMS,
            MultiblockAbility.IMPORT_FLUIDS,
            MultiblockAbility.EXPORT_FLUIDS,
            MultiblockAbility.INPUT_ENERGY,
            MultiblockAbility.MACHINE_HATCH,
            MultiblockAbility.MAINTENANCE_HATCH
    };

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.PROCESSING_ARRAY_RECIPES);
        this.recipeMapWorkable = new ProcessingArrayWorkable(this);
    }

    @Override
    protected BlockPattern createStructurePattern() {

        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .setAmountAtLeast('L', 11)
                .setAmountLimit('M', 1, 1)
                .where('M', abilityPartPredicate(MultiblockAbility.MACHINE_HATCH))
                .where('L', statePredicate(getCasingState()))
                .where('S', selfPredicate())
                .where('X',
                        statePredicate(getCasingState())
                                .or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('#', isAirPredicate()).build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart arg0) {
        return Textures.ROBUST_TUNGSTENSTEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityProcessingArray(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);

        if(this.recipeMapWorkable.isActive()) {
            textList.add(new TextComponentTranslation("gregtech.machine.machine_hatch.locked"));
        }
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    protected static class ProcessingArrayWorkable extends MultiblockRecipeLogic {
        long voltageTier;
        int numberOfMachines = 0;
        ItemStack machineItemStack = null;
        ItemStack oldMachineStack = null;
        RecipeMap<?> recipeMap = null;

        public ProcessingArrayWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        /*
        Overridden solely to update the machine stack and the recipe map at an early point.
        Recipe multiplication will come at a later time.
        */
        @Override
        protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode mode) {

            //Update the machine stack and recipe map
            findMachineStack();

            // Avoid crashing during load, when GTCE initializes its multiblock previews
            if (this.machineItemStack.isEmpty() || this.recipeMap == null) {
                return null;
            }

            return this.recipeMap.findRecipe(this.voltageTier, inputs, fluidInputs, this.getMinTankCapacity(this.getOutputTank()), MatchingMode.DEFAULT);
        }

        @Override
        public boolean isRecipeMapValid(RecipeMap<?> recipeMap) {
            if (findMachineInBlacklist(recipeMap.getUnlocalizedName())) {
                return false;
            }

            RecipeBuilder<?> recipeBuilder = recipeMap.recipeBuilder();

            return recipeBuilder instanceof SimpleRecipeBuilder ||
                    recipeBuilder instanceof IntCircuitRecipeBuilder ||
                    recipeBuilder instanceof ArcFurnaceRecipeBuilder ||
                    recipeBuilder instanceof CutterRecipeBuilder ||
                    recipeBuilder instanceof UniversalDistillationRecipeBuilder ||
                    recipeBuilder instanceof CircuitAssemblerRecipeBuilder;

        }

        private static boolean findMachineInBlacklist(String unlocalizedName) {

            String[] blacklist = ConfigHolder.multiblockConfiguration.processingArrayBlacklist;

            return Arrays.asList(blacklist).contains(unlocalizedName);
        }

        /**
         * Finds the machine stack in the specialized bus and updates all the cached information related to the machine stack
         */
        protected void findMachineStack() {

            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) this.metaTileEntity;

            //The Processing Array is limited to 1 Machine Interface per multiblock, and only has 1 slot
            ItemStack machine = controller.getAbilities(MultiblockAbility.MACHINE_HATCH).get(0).getImportItems().getStackInSlot(0);

            RecipeMap<?> rmap = RecipeMap.findRecipeMapByItemStack(machine);

            this.machineItemStack = machine;
            this.recipeMap = rmap;

            this.numberOfMachines = machine.getCount();

            MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(machine);

            if(mte == null) {
                this.voltageTier = 0;
            }
            else {
                //Find the voltage tier of the machine.
                this.voltageTier = GTValues.V[((ITieredMetaTileEntity) mte).getTier()];
            }
        }


        /**
         * Will check if the previous machine stack and the current machine stack are different
         *
         * @param newMachineStack - The current machine stack
         * @return - true if the machine stacks are not equal, false if they are equal
         */
        protected boolean didMachinesChange(ItemStack newMachineStack) {
            if (newMachineStack == null || this.oldMachineStack == null)
                return newMachineStack != this.oldMachineStack;

            return !ItemStack.areItemStacksEqual(this.oldMachineStack, newMachineStack);
        }

        @Override
        protected void trySearchNewRecipeCombined() {

            long maxVoltage = getMaxVoltage();
            Recipe currentRecipe;
            IItemHandlerModifiable importInventory = getInputInventory();
            IMultipleTankHandler importFluids = getInputTank();

            //Update the stored machine stack and recipe map variables
            findMachineStack();

            if(this.machineItemStack == null || this.machineItemStack.isEmpty()) {
                return;
            }

            if(didMachinesChange(machineItemStack)) {
                previousRecipe = null;
                oldMachineStack = null;
            }

            // see if the last recipe we used still works
            if (this.previousRecipe != null && this.previousRecipe.matches(false, importInventory, importFluids))
                currentRecipe = this.previousRecipe;
                // If there is no active recipe, then we need to find one.
            else {
                currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, MatchingMode.DEFAULT);
            }
            // If a recipe was found, then inputs were valid. Cache found recipe.
            if (currentRecipe != null) {
                this.previousRecipe = currentRecipe;
            }
            this.invalidInputsForRecipes = (currentRecipe == null);

            // proceed if we have a usable recipe.
            if (currentRecipe != null) {

                //Check if the recipe needs to be multiplied due to parallel logic
                Tuple<RecipeBuilder<?>, Integer> multipliedRecipe = ParallelLogic.multiplyRecipe(currentRecipe, this.recipeMap, importInventory, importFluids, this.numberOfMachines);

                // Multiply the recipe if we can
                if(multipliedRecipe != null) {
                    currentRecipe = multipliedRecipe.getFirst().build().getResult();
                    this.parallelRecipesPerformed = multipliedRecipe.getSecond();
                }

                if(setupAndConsumeRecipeInputs(currentRecipe, importInventory)) {
                    oldMachineStack = machineItemStack;
                    setupRecipe(currentRecipe);
                }

            }
            // Inputs have been inspected.
            metaTileEntity.getNotifiedItemInputList().clear();
            metaTileEntity.getNotifiedFluidInputList().clear();

        }

        /**
         * A very near copy of {@link MultiblockRecipeLogic#trySearchNewRecipeDistinct()} however with some changes to both
         * invalidate the stored machine stack, and cache the stored machine stack
         */
        @Override
        protected void trySearchNewRecipeDistinct() {

            long maxVoltage = getMaxVoltage();
            Recipe currentRecipe;
            List<IItemHandlerModifiable> importInventory = getInputBuses();
            IMultipleTankHandler importFluids = getInputTank();

            //Update the stored machine stack and recipe map variables
            findMachineStack();

            if(this.machineItemStack == null || this.machineItemStack.isEmpty()) {
                return;
            }

            //if fluids changed, iterate all input busses again
            if (metaTileEntity.getNotifiedFluidInputList().size() > 0) {
                for (IItemHandlerModifiable ihm : importInventory){
                    if (!metaTileEntity.getNotifiedItemInputList().contains(ihm)){
                        metaTileEntity.getNotifiedItemInputList().add(ihm);
                    }
                }
                metaTileEntity.getNotifiedFluidInputList().clear();
            }

            // Check if the machine stack has changed
            if (didMachinesChange(machineItemStack)) {
                previousRecipe = null;
                oldMachineStack = null;
            }


            // Our caching implementation
            // This guarantees that if we get a recipe cache hit, our efficiency is no different from other machines
            if (previousRecipe != null && previousRecipe.matches(false, importInventory.get(lastRecipeIndex), importFluids)) {
                currentRecipe = previousRecipe;

                // Perform Parallel Logic
                if(this.metaTileEntity instanceof MultiblockWithDisplayBase) {
                    Tuple<RecipeBuilder<?>, Integer> multipliedRecipe = ParallelLogic.multiplyRecipe(currentRecipe, this.recipeMap, importInventory.get(lastRecipeIndex), importFluids, this.numberOfMachines);

                    // Multiply the recipe if we can
                    if(multipliedRecipe != null) {
                        currentRecipe = multipliedRecipe.getFirst().build().getResult();
                        this.parallelRecipesPerformed = multipliedRecipe.getSecond();
                    }
                }

                // If a valid recipe is found, immediately attempt to return it to prevent inventory scanning
                if (setupAndConsumeRecipeInputs(currentRecipe, importInventory.get(lastRecipeIndex))) {
                    setupRecipe(currentRecipe);
                    metaTileEntity.getNotifiedItemInputList().remove(importInventory.get(lastRecipeIndex));
                    oldMachineStack = machineItemStack;

                    // No need to cache the previous recipe here, as it is not null and matched by the current recipe,
                    // so it will always be the same
                    return;
                }
            }

            // On a cache miss, our efficiency is much worse, as it will check
            // each bus individually instead of the combined inventory all at once.
            for (int i = 0; i < importInventory.size(); i++) {
                IItemHandlerModifiable bus = importInventory.get(i);
                // Skip this bus if no recipe was found last time and the inventory did not change
                if (invalidatedInputList.contains(bus) && !metaTileEntity.getNotifiedItemInputList().contains(bus)) {
                    continue;
                } else {
                    invalidatedInputList.remove(bus);
                }
                // Look for a new recipe after a cache miss
                currentRecipe = findRecipe(maxVoltage, bus, importFluids, MatchingMode.DEFAULT);
                // Cache the current recipe, if one is found
                if (currentRecipe != null) {
                    this.previousRecipe = currentRecipe;

                    // Perform Parallel Logic
                    if(this.metaTileEntity instanceof MultiblockWithDisplayBase) {
                        Tuple<RecipeBuilder<?>, Integer> multipliedRecipe = ParallelLogic.multiplyRecipe(currentRecipe, this.recipeMap, bus, importFluids, this.numberOfMachines);

                        // Multiply the recipe if we can
                        if(multipliedRecipe != null) {
                            currentRecipe = multipliedRecipe.getFirst().build().getResult();
                            this.parallelRecipesPerformed = multipliedRecipe.getSecond();
                        }

                    }

                    if (setupAndConsumeRecipeInputs(currentRecipe, importInventory.get(i))) {
                        lastRecipeIndex = i;
                        setupRecipe(currentRecipe);
                        metaTileEntity.getNotifiedItemInputList().remove(bus);
                        oldMachineStack = machineItemStack;
                        return;
                    }
                } else {
                    invalidatedInputList.add(bus);
                }
            }

            //If no matching recipes are found, clear the notified inputs so we know when new items are given
            metaTileEntity.getNotifiedItemInputList().clear();

        }
    }
}

