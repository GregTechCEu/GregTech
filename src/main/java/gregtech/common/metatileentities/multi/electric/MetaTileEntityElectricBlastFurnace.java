package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.BlockWireCoil2.CoilType2;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MetaTileEntityElectricBlastFurnace extends RecipeMapMultiblockController {

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
        Object type = context.get("CoilType");
        if (type instanceof CoilType) {
            this.blastFurnaceTemperature = ((CoilType) type).getCoilTemperature();
        } else if(type instanceof CoilType2) {
            this.blastFurnaceTemperature = ((CoilType2) type).getCoilTemperature();
        } else {
            this.blastFurnaceTemperature = CoilType.CUPRONICKEL.getCoilTemperature();
        }

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

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "XXX")
                .aisle("XXX", "C#C", "C#C", "XMX")
                .aisle("XSX", "CCC", "CCC", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(states(getCasingState()).setMinGlobalLimited(10))
                        .or(autoAbilities()))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('C', heatingCoils())
                .where('#', air())
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

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
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
                return new int[]{EUt, duration};
            }
            boolean negativeEU = EUt < 0;

            int blastFurnaceTemperature = ((MetaTileEntityElectricBlastFurnace) this.metaTileEntity).getBlastFurnaceTemperature();
            int amountEUDiscount = Math.max(0, (blastFurnaceTemperature - recipeRequiredTemp) / 900);
            int amountPerfectOC = amountEUDiscount / 2;

            //apply a multiplicative 95% energy multiplier for every 900k over recipe temperature
            EUt *= Math.min(1, Math.pow(0.95, amountEUDiscount));

            // Apply Configurable Maintenance effects
            double resultDuration = duration;
            MultiblockWithDisplayBase displayBase = (MultiblockWithDisplayBase) metaTileEntity;
            int numMaintenanceProblems = ((MultiblockWithDisplayBase) metaTileEntity).getNumMaintenanceProblems();
            if (((MetaTileEntityElectricBlastFurnace) metaTileEntity).hasMaintenanceMechanics()) {
                IMaintenanceHatch hatch = displayBase.getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0);
                if (hatch.getDurationMultiplier() != 1.0) {
                    resultDuration *= hatch.getDurationMultiplier();
                }
            }

            int tier = getOverclockingTier(voltage);
            if (GTValues.V[tier] <= EUt || tier == 0)
                return new int[]{EUt, duration};
            if (negativeEU)
                EUt = -EUt;

            int resultEUt = EUt;

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
                resultDuration /= ConfigHolder.U.overclockDivisor;
            }

            // apply maintenance slowdown
            resultDuration = (int) (resultDuration * (1 + 0.1 * numMaintenanceProblems));

            return new int[]{negativeEU ? -resultEUt : resultEUt, (int) Math.ceil(resultDuration)};

        }
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("XEM", "CCC", "CCC", "XXX")
                .aisle("FXD", "C#C", "C#C", "XHX")
                .aisle("ISO", "CCC", "CCC", "XXX")
                .where('X', MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF))
                .where('S', MetaTileEntities.ELECTRIC_BLAST_FURNACE, EnumFacing.SOUTH)
                .where('#', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.MV], EnumFacing.NORTH)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.EAST)
                .where('D', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.WEST)
                .where('H', MetaTileEntities.MUFFLER_HATCH[GTValues.LV], EnumFacing.UP)
                .where('M', () -> ConfigHolder.U.GT5u.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF), EnumFacing.NORTH);
        for (CoilType coilType : CoilType.values()) {
            shapeInfo.add(builder.where('C', MetaBlocks.WIRE_COIL.getState(coilType)).build());
        }
        for (CoilType2 coilType : CoilType2.values()) {
            shapeInfo.add(builder.where('C', MetaBlocks.WIRE_COIL2.getState(coilType)).build());
        }
        return shapeInfo;
    }
}
