package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.*;
import gregtech.api.recipes.logic.ParallelLogic;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.OverlayedItemHandler;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.BlockWireCoil2.CoilType2;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.*;

public class MetaTileEntityMultiSmelter extends RecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {
            MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS,
            MultiblockAbility.INPUT_ENERGY, MultiblockAbility.MAINTENANCE_HATCH
    };

    protected int heatingCoilLevel;
    protected int heatingCoilDiscount;

    public MetaTileEntityMultiSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES);
        this.recipeMapWorkable = new MultiSmelterWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMultiSmelter(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.multi_furnace.heating_coil_level", heatingCoilLevel));
            textList.add(new TextComponentTranslation("gregtech.multiblock.multi_furnace.heating_coil_discount", heatingCoilDiscount));
        }
        super.addDisplayText(textList);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object coilType = context.get("CoilType");
        if (coilType instanceof CoilType) {
            this.heatingCoilLevel = ((CoilType) coilType).getLevel();
            this.heatingCoilDiscount = ((CoilType) coilType).getEnergyDiscount();
        } else if(coilType instanceof CoilType2) {
            this.heatingCoilLevel = ((CoilType2) coilType).getLevel();
            this.heatingCoilDiscount = ((CoilType2) coilType).getEnergyDiscount();
        } else {
            this.heatingCoilLevel = CoilType.CUPRONICKEL.getLevel();
            this.heatingCoilDiscount = CoilType.CUPRONICKEL.getEnergyDiscount();
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.heatingCoilLevel = 0;
        this.heatingCoilDiscount = 0;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "XXX")
                .aisle("XXX", "C#C", "XMX")
                .aisle("XSX", "CCC", "XXX")
                .setAmountAtLeast('L', 9)
                .where('S', selfPredicate())
                .where('L', statePredicate(getCasingState()))
                .where('X', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('M', abilityPartPredicate(MultiblockAbility.MUFFLER_HATCH))
                .where('C', MetaTileEntityElectricBlastFurnace.heatingCoilPredicate())
                .where('#', isAirPredicate())
                .build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.MULTI_FURNACE_OVERLAY;
    }

    @Override
    public int getParallelLimit() {
        return this.heatingCoilLevel * 32;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    protected class MultiSmelterWorkable extends MultiblockRecipeLogic {

        public MultiSmelterWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        protected void trySearchNewRecipeCombined() {

            long maxVoltage = getMaxVoltage();
            Recipe currentRecipe;
            IItemHandlerModifiable importInventory = getInputInventory();
            IMultipleTankHandler importFluids = getInputTank();

            //inverse of logic in normal AbstractRecipeLogic
            //for MultiSmelter, we can reuse previous recipe if inputs didn't change
            //otherwise, we need to recompute it for new ingredients
            //but technically, it means we can cache multi smelter recipe, but changing inputs have more priority
            if (hasNotifiedInputs() ||
                    previousRecipe == null ||
                    !previousRecipe.matches(false, importInventory, importFluids)) {
                //Inputs changed, try searching new recipe for given inputs
                currentRecipe = findAndAppendRecipes(maxVoltage, importInventory);
            } else {
                //if previous recipe still matches inputs, try to use it
                currentRecipe = previousRecipe;
            }
            if (currentRecipe != null)
                // replace old recipe with new one
                this.previousRecipe = currentRecipe;
            // proceed if we have a usable recipe.
            if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe, importInventory)) {
                setupRecipe(currentRecipe);
            }

            // Inputs have been inspected.
            metaTileEntity.getNotifiedItemInputList().clear();
        }

        protected Recipe findAndAppendRecipes(long maxVoltage,
                                              IItemHandlerModifiable inputs) {
            final int maxItemsLimit = 32 * heatingCoilLevel;
            RecipeBuilder<?> recipeBuilder = recipeMap.recipeBuilder();

            boolean matchedRecipe = false;

            OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(outputInventory);

            // Iterate over the input items looking for more things to add until we run either out of input items
            // or we have exceeded the number of items permissible from the smelting bonus
            int engagedItems = 0;

            for (int index = 0; index < inputs.getSlots(); index++) {
                // Skip this slot if it is empty.
                final ItemStack currentInputItem = inputs.getStackInSlot(index);
                if (currentInputItem.isEmpty())
                    continue;

                // Determine if there is a valid recipe for this item. If not, skip it.
                Recipe matchingRecipe = findRecipe(maxVoltage, currentInputItem);

                CountableIngredient inputIngredient;
                if (matchingRecipe != null) {
                    inputIngredient = matchingRecipe.getInputs().get(0);
                    matchedRecipe = true;
                } else
                    continue;

                // There's something not right with this recipe if the ingredient is null.
                if (inputIngredient == null)
                    throw new IllegalStateException(
                            String.format("Got recipe with null ingredient %s", matchingRecipe));

                //equivalent of getting the max ratio from the inputs from Parallel logic
                int amountOfCurrentItem = Math.min(maxItemsLimit - engagedItems, currentInputItem.getCount());

                //how much we can add to the output inventory
                int limitByOutput = ParallelLogic.limitParallelByItems(matchingRecipe, overlayedItemHandler, amountOfCurrentItem);

                //amount to actually multiply the recipe by
                int multiplierRecipeAmount = Math.min(amountOfCurrentItem, limitByOutput);

                if (multiplierRecipeAmount > 0) {
                    recipeBuilder.append(matchingRecipe, multiplierRecipeAmount);
                    engagedItems += multiplierRecipeAmount;
                }

                if (engagedItems == maxItemsLimit) {
                    break;
                }
            }

            this.invalidInputsForRecipes = !matchedRecipe;
            this.isOutputsFull = (matchedRecipe && engagedItems == 0);

            if (recipeBuilder.getInputs().isEmpty()) {
                return null;
            }

            this.parallelRecipesPerformed = engagedItems;

            return recipeBuilder
                    .EUt(Math.max(1, 16 / heatingCoilDiscount))
                    .duration((int) Math.max(1.0, 256 * engagedItems / (maxItemsLimit * 1.0)))
                    .build().getResult();
        }

        protected Recipe findRecipe(long maxVoltage, ItemStack itemStack) {
            return recipeMap.findRecipe(maxVoltage,
                    Collections.singletonList(itemStack),
                    Collections.emptyList(), 0, MatchingMode.IGNORE_FLUIDS);
        }
    }
}
