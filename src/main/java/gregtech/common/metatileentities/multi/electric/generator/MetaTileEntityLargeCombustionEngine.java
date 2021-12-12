package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLargeCombustionEngine extends FuelMultiblockController {

    private final int tier;

    public MetaTileEntityLargeCombustionEngine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.COMBUSTION_GENERATOR_FUELS_2, tier);
        this.recipeMapWorkable = new LargeCombustionEngineWorkableHandler(this);
        this.recipeMapWorkable.enableOverclockVoltage();
        this.recipeMapWorkable.setOverclockTier(tier);
        this.tier = tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeCombustionEngine(metaTileEntityId, tier);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            if (getInputFluidInventory() != null) {
                FluidStack lubricantStack = getInputFluidInventory().drain(Materials.Lubricant.getFluid(Integer.MAX_VALUE), false);
                FluidStack oxygenStack = getInputFluidInventory().drain(Materials.Oxygen.getFluid(Integer.MAX_VALUE), false);
                int lubricantAmount = lubricantStack == null ? 0 : lubricantStack.amount;
                int oxygenAmount = oxygenStack == null ? 0 : oxygenStack.amount;
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.lubricant_amount", lubricantAmount));
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.oxygen_amount", oxygenAmount));
                textList.add(new TextComponentTranslation(oxygenAmount >= 2 ? "gregtech.multiblock.large_combustion_engine.oxygen_boosted" : "gregtech.multiblock.large_combustion_engine.supply_oxygen_to_boost"));
            }

            if (isStructureObstructed())
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.obstructed").setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.large_combustion_engine.tooltip.1", GTValues.V[tier]));
        tooltip.add(I18n.format("gregtech.machine.large_combustion_engine.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.large_combustion_engine.tooltip.3", GTValues.V[tier] * 3));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XDX", "XXX")
                .aisle("XCX", "CGC", "XCX")
                .aisle("XCX", "CGC", "XCX")
                .aisle("AAA", "AYA", "AAA")
                .where('X', states(getCasingState()))
                .where('G', states(getGearboxState()))
                .where('C', states(getCasingState()).setMinGlobalLimited(3).or(autoAbilities(false, true, true, true, true, true, true)))
                .where('D', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('A', states(getIntakeState()).addTooltips("gregtech.multiblock.pattern.clear_amount_1"))
                .where('Y', selfPredicate())
                .build();
    }

    public IBlockState getCasingState() {
        return tier > GTValues.EV ? MetaBlocks.METAL_CASING.getState(MetalCasingType.TUNGSTENSTEEL_ROBUST) :
                MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE);
    }

    public IBlockState getGearboxState() {
        return  tier > GTValues.EV ? MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TUNGSTENSTEEL_GEARBOX) :
                MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TITANIUM_GEARBOX);
    }

    public IBlockState getIntakeState() {
        return  tier > GTValues.EV ? MetaBlocks.MULTIBLOCK_CASING.getState(MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING) :
                MetaBlocks.MULTIBLOCK_CASING.getState(MultiblockCasingType.ENGINE_INTAKE_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return  tier > GTValues.EV ? Textures.ROBUST_TUNGSTENSTEEL_CASING : Textures.STABLE_TITANIUM_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.DIESEL_ENGINE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean isStructureObstructed() {
        return checkIntakesObstructed();
    }

    private boolean checkIntakesObstructed() {
        EnumFacing facing = this.getFrontFacing();
        boolean permuteXZ = facing.getAxis() == EnumFacing.Axis.Z;
        BlockPos centerPos = this.getPos().offset(facing);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                //Skip the controller block itself
                if (x == 0 && y == 0)
                    continue;
                BlockPos blockPos = centerPos.add(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                IBlockState blockState = this.getWorld().getBlockState(blockPos);
                if (!blockState.getBlock().isAir(blockState, this.getWorld(), blockPos))
                    return true;
            }
        }
        return false;
    }

    private static class LargeCombustionEngineWorkableHandler extends MultiblockFuelRecipeLogic {

        private static final float DURATION_BOOST = 2.0f;
        private static final float VOLTAGE_BOOST = 3.0f;

        private boolean isDurationBoosted = false;
        private boolean isVoltageBoosted = false;

        private static final FluidStack oxygenStack = Materials.Oxygen.getFluid(40);
        private static final FluidStack lubricantStack = Materials.Lubricant.getFluid(1);

        private boolean hasLubricant;

        public LargeCombustionEngineWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        protected void updateRecipeProgress() {
            super.updateRecipeProgress();
            if (drawEnergy(recipeEUt, true) && (totalContinuousRunningTime == 1 || totalContinuousRunningTime % 20 == 0)) {
                if (oxygenStack.isFluidStackIdentical(((RecipeMapMultiblockController) metaTileEntity).getInputFluidInventory().drain(oxygenStack, false))) {
                    ((RecipeMapMultiblockController) metaTileEntity).getInputFluidInventory().drain(oxygenStack, true);
                    isDurationBoosted = true;
                    isVoltageBoosted = true;
                } else {
                    isDurationBoosted = false;
                    isVoltageBoosted = false;
                }
            }
            if (drawEnergy(recipeEUt, true) && (totalContinuousRunningTime == 1 || totalContinuousRunningTime % 72 == 0)) {
                for (IFluidTank tank : ((RecipeMapMultiblockController) metaTileEntity).getInputFluidInventory()) {
                    FluidStack drained = tank.drain(lubricantStack.amount, false);
                    if (lubricantStack.isFluidStackIdentical(drained)) {
                        ((RecipeMapMultiblockController) metaTileEntity).getInputFluidInventory().drain(lubricantStack, true);
                        hasLubricant = true;
                        break;
                    } else {
                        hasLubricant = false;
                    }
                }
            }
        }

        @Override
        protected boolean canProgressRecipe() {
            return hasLubricant && super.canProgressRecipe();
        }

        @Override
        protected float getDurationBoost() {
            return isDurationBoosted ? DURATION_BOOST : super.getDurationBoost();
        }

        @Override
        protected float getVoltageBoost() {
            return isVoltageBoosted ? VOLTAGE_BOOST : super.getVoltageBoost();
        }

        @Override
        public void invalidate() {
            super.invalidate();
            isDurationBoosted = false;
            isVoltageBoosted = false;
            hasLubricant = false;
        }
    }
}
