package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class MetaTileEntityElectricBlastFurnace extends RecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {
        MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS,
        MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.EXPORT_FLUIDS,
        MultiblockAbility.INPUT_ENERGY
    };

    private int blastFurnaceTemperature;

    public MetaTileEntityElectricBlastFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.BLAST_RECIPES);
        if (ConfigHolder.U.GT5u.ebfTemperatureBonuses)
            this.recipeMapWorkable = new ElectricBlastFurnaceWorkableHandler(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityElectricBlastFurnace(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature", blastFurnaceTemperature)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        }
        super.addDisplayText(textList);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.blastFurnaceTemperature = context.getOrDefault("CoilType", CoilType.CUPRONICKEL).getCoilTemperature();

        if (ConfigHolder.U.GT5u.ebfTemperatureBonuses)
            this.blastFurnaceTemperature += 100 * Math.max(0, GTUtility.getTierByVoltage(getEnergyContainer().getInputVoltage()) - GTValues.MV);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.blastFurnaceTemperature = 0;
    }

    @Override
    public boolean checkRecipe(Recipe recipe, boolean consumeIfSuccess) {
        int recipeRequiredTemp = recipe.getProperty(BlastTemperatureProperty.getInstance(), 0);
        return this.blastFurnaceTemperature >= recipeRequiredTemp;
    }

    public static Predicate<BlockWorldState> heatingCoilPredicate() {
        return blockWorldState -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof BlockWireCoil))
                return false;
            BlockWireCoil blockWireCoil = (BlockWireCoil) blockState.getBlock();
            CoilType coilType = blockWireCoil.getState(blockState);
            CoilType currentCoilType = blockWorldState.getMatchContext().getOrPut("CoilType", coilType);
            return currentCoilType.getName().equals(coilType.getName());
        };
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
            .aisle("XXX", "CCC", "CCC", "XXX")
            .aisle("XXX", "C#C", "C#C", "XXX")
            .aisle("XSX", "CCC", "CCC", "XXX")
            .setAmountAtLeast('L', 10)
            .where('S', selfPredicate())
            .where('L', statePredicate(getCasingState()))
            .where('X', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
            .where('C', heatingCoilPredicate())
            .where('#', isAirPredicate())
            .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (ConfigHolder.U.GT5u.ebfTemperatureBonuses) {
            tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.1"));
            tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.2"));
            tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.3"));
        }
    }

    public int getBlastFurnaceTemperature() {
        return this.blastFurnaceTemperature;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    public static class ElectricBlastFurnaceWorkableHandler extends MultiblockRecipeLogic {

        public ElectricBlastFurnaceWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        protected void setupRecipe(Recipe recipe) {
            int[] resultOverclock = calculateOverclock(recipe.getEUt(), this.getMaxVoltage(), recipe.getDuration(),
                    recipe.getProperty(BlastTemperatureProperty.getInstance(), 0));

            this.progressTime = 1;
            setMaxProgress(resultOverclock[1]);
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

        protected int[] calculateOverclock(int EUt, long voltage, int duration, int recipeRequiredTemp) {
            if (!allowOverclocking) {
                return new int[] {EUt, duration};
            }
            boolean negativeEU = EUt < 0;

            int blastFurnaceTemperature = ((MetaTileEntityElectricBlastFurnace) this.metaTileEntity).getBlastFurnaceTemperature();
            int amountEUDiscount = Math.max(0, (blastFurnaceTemperature - recipeRequiredTemp) / 900);
            int amountPerfectOC = amountEUDiscount / 2;

            //apply a multiplicative 95% energy multiplier for every 900k over recipe temperature
            EUt *= Math.min(1, Math.pow(0.95, amountEUDiscount));

            int tier = getOverclockingTier(voltage);
            if (GTValues.V[tier] <= EUt || tier == 0)
                return new int[]{EUt, duration};
            if (negativeEU)
                EUt = -EUt;
            if (EUt <= 16) {
                int multiplier = EUt <= 8 ? tier : tier - 1;
                int resultEUt = EUt * (1 << multiplier) * (1 << multiplier);
                int resultDuration = duration / (1 << multiplier);
                return new int[]{negativeEU ? -resultEUt : resultEUt, resultDuration};
            } else {
                int resultEUt = EUt;
                double resultDuration = duration;

                //do not overclock further if duration is already too small
                //perfect overclock for every 1800k over recipe temperature
                while (resultDuration >= 3 && resultEUt <= GTValues.V[tier - 1] && amountPerfectOC > 0) {
                    resultEUt *= 4;
                    resultDuration /= 4;
                    amountPerfectOC--;
                }

                //do not overclock further if duration is already too small
                //regular overclocking
                while (resultDuration >= 3 && resultEUt <= GTValues.V[tier - 1]) {
                    resultEUt *= 4;
                    resultDuration /= 2.8;
                }

                return new int[]{negativeEU ? -resultEUt : resultEUt, (int) Math.ceil(resultDuration)};
            }
        }
    }
}
