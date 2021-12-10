package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.*;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.electric.MetaTileEntityMacerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class MetaTileEntityProcessingArray extends RecipeMapMultiblockController {

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.PROCESSING_ARRAY_RECIPES);
        this.recipeMapWorkable = new ProcessingArrayWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityProcessingArray(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('L', states(getCasingState()))
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(11).or(autoAbilities())
                        .or(abilities(MultiblockAbility.MACHINE_HATCH).setExactLimit(1)))
                .where('#', air())
                .build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.ROBUST_TUNGSTENSTEEL_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PROCESSING_ARRAY_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }

    /**
     *
     * @param machineStack the ItemStack containing the machine to check the validity of
     * @return whether the machine is valid or not
     */
    public static boolean isMachineValid(ItemStack machineStack) {
        MetaTileEntity machine = MachineItemBlock.getMetaTileEntity(machineStack);
        if (machine instanceof WorkableTieredMetaTileEntity)
            return !findMachineInBlacklist(machine.getRecipeMap().getUnlocalizedName());

        return false;
    }

    /**
     * Attempts to find a passed in RecipeMap unlocalized name in a list of names
     * @param unlocalizedName The unlocalized name of a RecipeMap
     * @return {@code true} If the RecipeMap is in the config blacklist
     */
    private static boolean findMachineInBlacklist(String unlocalizedName) {
        return Arrays.asList(ConfigHolder.machines.processingArrayBlacklist).contains(unlocalizedName);
    }

    protected static class ProcessingArrayWorkable extends MultiblockRecipeLogic {

        ItemStack currentMachineStack = null;
        ItemStack oldMachineStack = null;
        //The Voltage Tier of the machines the PA is operating upon, from GTValues.V
        private int machineTier;
        //The maximum Voltage of the machines the PA is operating upon
        private long machineVoltage;
        //The Recipe Map of the machines the PA is operating upon
        private RecipeMap<?> activeRecipeMap;

        public ProcessingArrayWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public void invalidate() {
            super.invalidate();
            // Reset locally cached variables upon invalidation
            currentMachineStack = null;
            oldMachineStack = null;
            machineTier = 0;
            machineVoltage = 0L;
            activeRecipeMap = null;
        }

        /**
         * Checks if a provided Recipe Map is valid to be used in the processing array
         * Will filter out anything in the config blacklist, and also any non-singleblock machines
         * @param recipeMap The recipeMap to check
         * @return {@code true} if the provided recipeMap is valid for use
         */
        @Override
        public boolean isRecipeMapValid(RecipeMap<?> recipeMap) {
            if (findMachineInBlacklist(recipeMap.getUnlocalizedName()))
                return false;

            return isMachineValid(currentMachineStack);
        }

        @Override
        protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode mode) {

            findMachineStack();

            if (currentMachineStack.isEmpty() || this.activeRecipeMap == null)
                return null;

            //Find available recipes based on the tier of the machines the processing array is operating upon and the voltage provided to the Processing Array
            return super.findRecipe(maxVoltage, inputs, fluidInputs, mode);
        }

        @Override
        public RecipeMap<?> getRecipeMap() {
            return activeRecipeMap;
        }

        public void findMachineStack() {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) this.metaTileEntity;

            //The Processing Array is limited to 1 Machine Interface per multiblock, and only has 1 slot
            ItemStack machine = controller.getAbilities(MultiblockAbility.MACHINE_HATCH).get(0).getStackInSlot(0);


            MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(machine);

            if (mte == null)
                this.activeRecipeMap = null;
            else
                this.activeRecipeMap = mte.getRecipeMap();


            //Find the voltage tier of the machine.
            this.machineTier = mte instanceof ITieredMetaTileEntity ? ((ITieredMetaTileEntity) mte).getTier() : 0;

            this.machineVoltage = GTValues.V[this.machineTier];

            this.currentMachineStack = machine;
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

            findMachineStack();

            if (this.currentMachineStack == null || this.currentMachineStack.isEmpty())
                return;

            super.trySearchNewRecipeCombined();
        }

        @Override
        protected boolean checkPreviousRecipe() {
            boolean result = super.checkPreviousRecipe();

            if(didMachinesChange(currentMachineStack)) {
                oldMachineStack = null;
                return false;
            }

            return result;
        }

        @Override
        protected boolean checkPreviousRecipeDistinct(IItemHandlerModifiable previousBus) {
            boolean result = super.checkPreviousRecipeDistinct(previousBus);

            if(didMachinesChange(currentMachineStack)) {
                oldMachineStack = null;
                return false;
            }

            return result;
        }

        @Override
        protected boolean prepareRecipe(Recipe recipe) {
            boolean result = super.prepareRecipe(recipe);
            if(result) {
                this.oldMachineStack = currentMachineStack;
            }

            return result;
        }

        @Override
        protected boolean prepareRecipeDistinct(Recipe recipe) {
            boolean result = super.prepareRecipe(recipe);
            if(result) {
                this.oldMachineStack = currentMachineStack;
            }

            return result;
        }

        @Override
        protected int getOverclockingTier(long voltage) {
            return this.machineTier;
        }

        @Override
        public int getParallelLimit() {
            return (currentMachineStack == null || currentMachineStack.isEmpty()) ? 64 : Math.min(currentMachineStack.getCount(), 64);
        }

        @Override
        protected long getMaxVoltage() {
            return Math.min(super.getMaxVoltage(), this.machineVoltage);
        }

        @Override
        public void applyParallelBonus(@Nonnull RecipeBuilder<?> builder) {

            MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(currentMachineStack);

            //Clear the chanced outputs of LV and MV macerators, as they do not have the slots to get byproducts
            if(mte instanceof MetaTileEntityMacerator && machineTier < GTValues.HV) {
                builder.clearChancedOutput();
            }
        }
    }
}
