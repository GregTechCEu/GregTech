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
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackKey;
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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
                currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, MatchingMode.IGNORE_FLUIDS);
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

        @Override
        protected void setupRecipe(Recipe recipe) {
            int[] resultOverclock = calculateOverclock(recipe.getEUt(), getMaxVoltage(), recipe.getDuration());
            this.progressTime = 1;
            setMaxProgress(resultOverclock[1]);
            //override the recipeEUt here, so that the parallel implementation does not mess with the Multismelter energy reduction
            this.recipeEUt = resultOverclock[0];
            this.fluidOutputs = GTUtility.copyFluidList(recipe.getFluidOutputs());
            int tier = getMachineTierForRecipe(recipe);
            this.itemOutputs = GTUtility.copyStackList(recipe.getResultItemOutputs(getOutputInventory().getSlots(), random, tier));
            if (this.wasActiveAndNeedsUpdate) {
                this.wasActiveAndNeedsUpdate = false;
            } else {
                this.setActive(true);
            }

        }

        @Override
        protected Recipe findRecipe(long maxVoltage,
                                    IItemHandlerModifiable inputs,
                                    IMultipleTankHandler fluidInputs, MatchingMode mode) {

            Map<Integer, Triple<ItemStackKey, Integer, Integer>> outputInvMap = ParallelLogic.mapInvHandler(this.getOutputInventory());

            final int maxItemsLimit = 32 * heatingCoilLevel;
            final ArrayList<CountableIngredient> recipeInputs = new ArrayList<>();
            final ArrayList<ItemStack> recipeOutputs = new ArrayList<>();

            boolean matchedRecipe = false;

            // Iterate over the input items looking for more things to add until we run either out of input items
            // or we have exceeded the number of items permissible from the smelting bonus
            int itemsLeftUntilMax = maxItemsLimit;

            for (int index = 0; index < inputs.getSlots(); index++) {

                if (itemsLeftUntilMax == 0) {
                    break;
                }

                // Skip this slot if it is empty.
                final ItemStack currentInputItem = inputs.getStackInSlot(index);
                if (currentInputItem.isEmpty())
                    continue;

                // Determine if there is a valid recipe for this item. If not, skip it.
                Recipe matchingRecipe = recipeMap.findRecipe(maxVoltage,
                        Collections.singletonList(currentInputItem),
                        Collections.emptyList(), 0, MatchingMode.DEFAULT);
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


                int amountOfCurrentItem = Math.min(itemsLeftUntilMax, currentInputItem.getCount());

                int amountToInsert = matchingRecipe.getOutputs().get(0).getCount() * amountOfCurrentItem;

                //smelting recipes are limited to one output
                ItemStackKey stackKey = KeySharedStack.getRegisteredStack(matchingRecipe.getOutputs().get(0));

                amountToInsert = simulateAddHashedItemToInvMap(stackKey, amountToInsert, outputInvMap);

                //since we're adding sequentially to the recipe, if the last one cant fit all,
                //subtract the result of the division of outputs per input.
                //if it's a partial division, reduce the input stack by one.
                if (amountToInsert > 0) {
                    amountOfCurrentItem -= amountToInsert / matchingRecipe.getOutputs().get(0).getCount();
                    if (amountToInsert % matchingRecipe.getOutputs().get(0).getCount() != 0) {
                        amountOfCurrentItem -= 1;
                    }
                }

                //add the result of the simulation, if successfully merged.
                if (amountOfCurrentItem > 0) {
                    recipeInputs.add(new CountableIngredient(inputIngredient.getIngredient(),
                            inputIngredient.getCount() * amountOfCurrentItem));

                    ItemStack copyToAdd = matchingRecipe.getOutputs().get(0).copy();
                    copyToAdd.setCount(matchingRecipe.getOutputs().get(0).getCount() * amountOfCurrentItem);
                    recipeOutputs.add(copyToAdd);

                    itemsLeftUntilMax -= amountOfCurrentItem;
                }
            }

            this.invalidInputsForRecipes = !matchedRecipe;
            this.isOutputsFull = (matchedRecipe && itemsLeftUntilMax == maxItemsLimit);

            if (recipeInputs.isEmpty()) {
                return null;
            }

            this.parallelRecipesPerformed = maxItemsLimit - itemsLeftUntilMax;

            return recipeMap.recipeBuilder()
                    .inputsIngredients(recipeInputs)
                    .outputs(recipeOutputs)
                    .EUt(Math.max(1, 16 / heatingCoilDiscount))
                    .duration((int) Math.max(1.0, 256 * ((maxItemsLimit - itemsLeftUntilMax) / (maxItemsLimit * 1.0))))
                    .build().getResult();
        }
    }
}
