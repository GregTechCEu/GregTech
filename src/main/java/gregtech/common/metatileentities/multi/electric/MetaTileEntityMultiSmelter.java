package gregtech.common.metatileentities.multi.electric;

import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.logic.StandardRecipeView;
import gregtech.api.recipes.logic.TrimmedRecipeView;
import gregtech.api.recipes.machines.RecipeLookupFurnace;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MetaTileEntityMultiSmelter extends RecipeMapMultiblockController {

    protected int heatingCoilLevel;
    protected int heatingCoilDiscount;

    public MetaTileEntityMultiSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES);
        this.recipeMapWorkable = new MultiSmelterWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMultiSmelter(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(recipeMapWorkable.getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltageIn()))
                .addCustom(tl -> {
                    if (isStructureFormed()) {
                        // Heating coil discount
                        if (heatingCoilDiscount > 1) {
                            ITextComponent coilDiscount = TextComponentUtil.stringWithColor(
                                    TextFormatting.AQUA,
                                    TextFormattingUtil.formatNumbers(100.0 / heatingCoilDiscount) + "%");

                            ITextComponent base = TextComponentUtil.translationWithColor(
                                    TextFormatting.GRAY,
                                    "gregtech.multiblock.multi_furnace.heating_coil_discount",
                                    coilDiscount);

                            ITextComponent hoverText = TextComponentUtil.translationWithColor(
                                    TextFormatting.GRAY,
                                    "gregtech.multiblock.multi_furnace.heating_coil_discount_hover");

                            TextComponentUtil.setHover(base, hoverText);
                            tl.add(base);
                        }

                        // Custom parallels line so we can have a hover text
                        if (recipeMapWorkable.getBaseParallelLimit() > 1) {
                            ITextComponent parallels = TextComponentUtil.stringWithColor(
                                    TextFormatting.DARK_PURPLE,
                                    TextFormattingUtil.formatNumbers(recipeMapWorkable.getBaseParallelLimit()));
                            ITextComponent bodyText = TextComponentUtil.translationWithColor(
                                    TextFormatting.GRAY,
                                    "gregtech.multiblock.parallel",
                                    parallels);
                            ITextComponent hoverText = TextComponentUtil.translationWithColor(
                                    TextFormatting.GRAY,
                                    "gregtech.multiblock.multi_furnace.parallel_hover");
                            tl.add(TextComponentUtil.setHover(bodyText, hoverText));
                        }
                    }
                })
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgressPercent());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object coilType = context.get("CoilType");
        if (coilType instanceof IHeatingCoilBlockStats) {
            this.heatingCoilLevel = ((IHeatingCoilBlockStats) coilType).getLevel();
            this.heatingCoilDiscount = ((IHeatingCoilBlockStats) coilType).getEnergyDiscount();
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

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "XXX")
                .aisle("XXX", "C#C", "XMX")
                .aisle("XSX", "CCC", "XXX")
                .where('S', selfPredicate())
                .where('X',
                        states(getCasingState()).setMinGlobalLimited(9)
                                .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('C', heatingCoils())
                .where('#', air())
                .build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.MULTI_FURNACE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    /**
     * @param parallel the amount of parallel recipes
     * @param discount the energy discount
     * @return the un-overclocked EUt for an amount of parallel recipes
     */
    public static long getEUtAtParallel(int parallel, int discount) {
        return getVoltageAfterDiscount(RecipeLookupFurnace.STANDARD_VOLTAGE, discount) *
                getAmperageAtParallel(1, parallel);
    }

    /**
     * @param voltage  the voltage of the recipe
     * @param discount the voltage discount
     * @return the voltage after discount
     */
    public static long getVoltageAfterDiscount(long voltage, int discount) {
        return Math.max(1, voltage / discount);
    }

    /**
     * @param amperage the amperage of the recipe
     * @param parallel the parallel of the recipe
     * @return the amperage at this parallel
     */
    public static long getAmperageAtParallel(long amperage, int parallel) {
        return (long) Math.ceil(amperage * parallel / 8d);
    }

    /**
     * @param heatingCoilLevel the level to get the parallel for
     * @return the max parallel for the heating coil level
     */
    public static int getMaxParallel(int heatingCoilLevel) {
        return 32 * heatingCoilLevel;
    }

    /**
     * @param parallel      the amount of parallel recipes
     * @param parallelLimit the maximum limit on parallel recipes
     * @return the un-overclocked duration for an amount of parallel recipes
     */
    public static int getDurationForParallel(int parallel, int parallelLimit) {
        return getDurationForParallel(RecipeLookupFurnace.STANDARD_DURATION, parallel, parallelLimit);
    }

    /**
     * @param duration      the base duration of the recipe
     * @param parallel      the amount of parallel recipes
     * @param parallelLimit the maximum limit on parallel recipes
     * @return the un-overclocked duration for an amount of parallel recipes
     */
    public static int getDurationForParallel(int duration, int parallel, int parallelLimit) {
        return (int) Math.max(1.0, duration * 2d * parallel / Math.max(1, parallelLimit));
    }

    protected class MultiSmelterWorkable extends MultiblockRecipeLogic {

        public MultiSmelterWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        protected int determineParallel(@NotNull RecipeView recipe) {
            @SuppressWarnings("deprecation")
            int limit = getParallelLimit(recipe.getRecipe());
            long recipeAmperage = recipe.getActualAmperage();
            long maxAmperage = getMaxParallelAmperage(recipe.getActualVoltage(), recipe.getRecipe().isGenerating());
            int parallel = 0;
            while (parallel - limit > 1) {
                int middle = (limit + parallel) / 2;
                if (testParallel(recipeAmperage, maxAmperage, middle)) {
                    parallel = middle;
                } else {
                    limit = middle;
                }
            }
            return testParallel(recipeAmperage, maxAmperage, limit) ? limit : parallel;
        }

        protected boolean testParallel(long recipeAmperage, long maxAmperage, int parallel) {
            return getAmperageAtParallel(recipeAmperage, parallel) <= maxAmperage;
        }

        @Override
        protected @NotNull StandardRecipeView getTrimmedRecipeView(@NotNull Recipe recipe,
                                                                   @NotNull MatchCalculation<ItemStack> itemMatch,
                                                                   @NotNull MatchCalculation<FluidStack> fluidMatch) {
            return new MultiSmelterRecipeView(recipe, itemMatch, fluidMatch, getEUtDiscount(), 1,
                    metaTileEntity.getItemOutputLimit(), metaTileEntity.getFluidOutputLimit());
        }

        @Override
        public int getBaseParallelLimit() {
            return getMaxParallel(heatingCoilLevel);
        }

        @Override
        protected boolean canSubtick() {
            return false;
        }
    }

    protected class MultiSmelterRecipeView extends TrimmedRecipeView {

        public MultiSmelterRecipeView(@NotNull Recipe recipe, @NotNull MatchCalculation<ItemStack> itemMatch,
                                      @NotNull MatchCalculation<FluidStack> fluidMatch, double voltageDiscount,
                                      int initialParallel, int maxItems, int maxFluids) {
            super(recipe, itemMatch, fluidMatch, voltageDiscount, initialParallel, maxItems, maxFluids);
        }

        @Override
        public int getActualDuration() {
            return getDurationForParallel(super.getActualDuration(), getParallel(), getMaxParallel(heatingCoilLevel));
        }

        @Override
        public long getActualVoltage() {
            return getVoltageAfterDiscount(super.getActualVoltage(), heatingCoilDiscount);
        }
    }
}
