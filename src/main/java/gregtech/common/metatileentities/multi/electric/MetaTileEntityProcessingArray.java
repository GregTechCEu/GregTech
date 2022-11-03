package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.IMachineHatchMultiblock;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.sound.GTSounds;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.recipes.logic.OverclockingLogic.unlockedVoltageOverclockingLogic;

public class MetaTileEntityProcessingArray extends RecipeMapMultiblockController implements IMachineHatchMultiblock {

    private final int tier;
    private boolean machineChanged;

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, null);
        this.tier = tier;
        this.recipeMapWorkable = new ProcessingArrayWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityProcessingArray(metaTileEntityId, tier);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        ((ProcessingArrayWorkable) this.recipeMapWorkable).findMachineStack();
    }

    @Override
    public int getMachineLimit() {
        return tier == 0 ? 16 : 64;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('L', states(getCasingState()))
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(tier == 0 ? 11 : 4).or(autoAbilities())
                        .or(abilities(MultiblockAbility.MACHINE_HATCH).setExactLimit(1)))
                .where('#', air())
                .build();
    }

    public IBlockState getCasingState() {
        return tier == 0
                ? MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST)
                : MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.HSSE_STURDY);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return tier == 0
                ? Textures.ROBUST_TUNGSTENSTEEL_CASING
                : Textures.ROBUST_HSSE_CASING;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if(this.isActive()) {
            textList.add(new TextComponentTranslation("gregtech.machine.machine_hatch.locked").setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return tier == 0
                ? Textures.PROCESSING_ARRAY_OVERLAY
                : Textures.ADVANCED_PROCESSING_ARRAY_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public void notifyMachineChanged() {
        machineChanged = true;
    }

    @Override
    public String[] getBlacklist() {
        return ConfigHolder.machines.processingArrayBlacklist;
    }

    @Override
    public SoundEvent getSound() {
        return GTSounds.ARC;
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(4).setPreviewCount(1) : new TraceabilityPredicate());

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));

        return predicate;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.parallel_limit", getMachineLimit()));
    }

    @Override
    public int getItemOutputLimit() {
        ItemStack machineStack = ((ProcessingArrayWorkable) this.recipeMapWorkable).getMachineStack();
        MetaTileEntity mte = GTUtility.getMetaTileEntity(machineStack);
        return mte == null ? 0 : mte.getItemOutputLimit();

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    protected class ProcessingArrayWorkable extends MultiblockRecipeLogic {

        ItemStack currentMachineStack = ItemStack.EMPTY;
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
            currentMachineStack = ItemStack.EMPTY;
            machineChanged = true;
            machineTier = 0;
            machineVoltage = 0L;
            activeRecipeMap = null;
        }

        /**
         * Checks if a provided Recipe Map is valid to be used in the processing array
         * Will filter out anything in the config blacklist, and also any non-singleblock machines
         *
         * @param recipeMap The recipeMap to check
         * @return {@code true} if the provided recipeMap is valid for use
         */
        @Override
        public boolean isRecipeMapValid(RecipeMap<?> recipeMap) {
            if (recipeMap == null || GTUtility.findMachineInBlacklist(recipeMap.getUnlocalizedName(), ((IMachineHatchMultiblock) metaTileEntity).getBlacklist()))
                return false;

            return GTUtility.isMachineValidForMachineHatch(currentMachineStack, ((IMachineHatchMultiblock) metaTileEntity).getBlacklist());
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return canWorkWithMachines() && super.shouldSearchForRecipes();
        }

        public boolean canWorkWithMachines() {
            if (machineChanged) {
                findMachineStack();
                machineChanged = false;
                previousRecipe = null;
                if (isDistinct()) {
                    invalidatedInputList.clear();
                } else {
                    invalidInputsForRecipes = false;
                }
            }
            return (!currentMachineStack.isEmpty() && this.activeRecipeMap != null);
        }

        @Override
        public RecipeMap<?> getRecipeMap() {
            return activeRecipeMap;
        }

        public void findMachineStack() {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) this.metaTileEntity;

            //The Processing Array is limited to 1 Machine Interface per multiblock, and only has 1 slot
            ItemStack machine = controller.getAbilities(MultiblockAbility.MACHINE_HATCH).get(0).getStackInSlot(0);


            MetaTileEntity mte = GTUtility.getMetaTileEntity(machine);

            if (mte == null)
                this.activeRecipeMap = null;
            else
                this.activeRecipeMap = mte.getRecipeMap();


            //Find the voltage tier of the machine.
            this.machineTier = mte instanceof ITieredMetaTileEntity ? ((ITieredMetaTileEntity) mte).getTier() : 0;

            this.machineVoltage = GTValues.V[this.machineTier];

            this.currentMachineStack = machine;
        }

        @Override
        public int getParallelLimit() {
            return (currentMachineStack == null || currentMachineStack.isEmpty()) ? getMachineLimit() : Math.min(currentMachineStack.getCount(), getMachineLimit());
        }

        @Override
        protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
            return super.findRecipe(Math.min(super.getMaxVoltage(), this.machineVoltage), inputs, fluidInputs);
        }

        @Override
        protected int[] calculateOverclock(@Nonnull Recipe recipe) {
            int recipeEUt = recipe.getEUt();
            int recipeDuration = recipe.getDuration();
            if (!isAllowOverclocking()) {
                return new int[]{recipeEUt, recipeDuration};
            }

            // apply maintenance penalties
            Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

            int originalTier = Math.max(1, GTUtility.getTierByVoltage(recipeEUt / Math.max(1, this.parallelRecipesPerformed)));
            int numOverclocks = Math.min(this.machineTier, GTUtility.getTierByVoltage(getMaxVoltage())) - originalTier;
            return unlockedVoltageOverclockingLogic(
                    recipeEUt, getMaxVoltage(),
                    (int) Math.round(recipeDuration * maintenanceValues.getSecond()),
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier(),
                    numOverclocks
            );
        }

        private ItemStack getMachineStack() {
            return currentMachineStack;
        }
    }
}
