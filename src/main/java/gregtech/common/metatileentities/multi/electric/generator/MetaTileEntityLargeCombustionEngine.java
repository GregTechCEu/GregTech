package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLargeCombustionEngine extends FuelMultiblockController {

    private final int tier;
    private final boolean isExtreme;
    private boolean boostAllowed;

    public MetaTileEntityLargeCombustionEngine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.COMBUSTION_GENERATOR_FUELS, tier);
        this.recipeMapWorkable = new LargeCombustionEngineWorkableHandler(this, tier > GTValues.EV);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
        this.tier = tier;
        this.isExtreme = tier > GTValues.EV;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeCombustionEngine(metaTileEntityId, tier);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            if (getInputFluidInventory() != null) {
                FluidStack lubricantStack = getInputFluidInventory().drain(Materials.Lubricant.getFluid(Integer.MAX_VALUE), false);
                FluidStack oxygenStack = getInputFluidInventory().drain(Materials.Oxygen.getFluid(Integer.MAX_VALUE), false);
                FluidStack liquidOxygenStack = getInputFluidInventory().drain(Materials.LiquidOxygen.getFluid(Integer.MAX_VALUE), false);
                int lubricantAmount = lubricantStack == null ? 0 : lubricantStack.amount;
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.lubricant_amount", lubricantAmount));
                if (boostAllowed) {
                    if (!isExtreme) {
                        if (((LargeCombustionEngineWorkableHandler) recipeMapWorkable).isOxygenBoosted) {
                            int oxygenAmount = oxygenStack == null ? 0 : oxygenStack.amount;
                            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.oxygen_amount", oxygenAmount));
                            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.oxygen_boosted"));
                        } else {
                            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.supply_oxygen_to_boost"));
                        }
                    }
                    else {
                        if (((LargeCombustionEngineWorkableHandler) recipeMapWorkable).isOxygenBoosted) {
                            int liquidOxygenAmount = liquidOxygenStack == null ? 0 : liquidOxygenStack.amount;
                            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.liquid_oxygen_amount", liquidOxygenAmount));
                            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.liquid_oxygen_boosted"));
                        } else {
                            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.supply_liquid_oxygen_to_boost"));
                        }
                    }
                }
                else {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.boost_disallowed"));
                }
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
        if (isExtreme) {
            tooltip.add(I18n.format("gregtech.machine.large_combustion_engine.tooltip.boost_extreme", GTValues.V[tier] * 4));
        } else {
            tooltip.add(I18n.format("gregtech.machine.large_combustion_engine.tooltip.boost_regular", GTValues.V[tier] * 3));
        }
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
                .where('D', metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.OUTPUT_ENERGY).stream()
                        .filter(mte -> {
                            IEnergyContainer container = mte.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
                            return container != null && container.getOutputVoltage() * container.getOutputAmperage() >= GTValues.V[tier];
                        })
                        .toArray(MetaTileEntity[]::new))
                        .addTooltip("gregtech.multiblock.pattern.error.limited.1", GTValues.VN[tier]))
                .where('A', states(getIntakeState()).addTooltips("gregtech.multiblock.pattern.clear_amount_1"))
                .where('Y', selfPredicate())
                .build();
    }

    public IBlockState getCasingState() {
        return isExtreme ? MetaBlocks.METAL_CASING.getState(MetalCasingType.TUNGSTENSTEEL_ROBUST) :
                MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE);
    }

    public IBlockState getGearboxState() {
        return isExtreme ? MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TUNGSTENSTEEL_GEARBOX) :
                MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TITANIUM_GEARBOX);
    }

    public IBlockState getIntakeState() {
        return isExtreme ? MetaBlocks.MULTIBLOCK_CASING.getState(MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING) :
                MetaBlocks.MULTIBLOCK_CASING.getState(MultiblockCasingType.ENGINE_INTAKE_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return isExtreme ? Textures.ROBUST_TUNGSTENSTEEL_CASING : Textures.STABLE_TITANIUM_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return isExtreme ? Textures.EXTREME_COMBUSTION_ENGINE_OVERLAY : Textures.LARGE_COMBUSTION_ENGINE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || checkIntakesObstructed();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        IEnergyContainer energyContainer = getEnergyContainer();
        this.boostAllowed = energyContainer != null && energyContainer.getOutputVoltage() >= GTValues.V[this.tier + 1];
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

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    public boolean isBoostAllowed() {
        return boostAllowed;
    }

    private static class LargeCombustionEngineWorkableHandler extends MultiblockFuelRecipeLogic {

        private boolean isOxygenBoosted = false;

        private final MetaTileEntityLargeCombustionEngine combustionEngine;
        private final boolean isExtreme;
        private final int tier;

        private static final FluidStack OXYGEN_STACK = Materials.Oxygen.getFluid(20);
        private static final FluidStack LIQUID_OXYGEN_STACK = Materials.LiquidOxygen.getFluid(80);
        private static final FluidStack LUBRICANT_STACK = Materials.Lubricant.getFluid(1);

        public LargeCombustionEngineWorkableHandler(RecipeMapMultiblockController tileEntity, boolean isExtreme) {
            super(tileEntity);
            this.combustionEngine = (MetaTileEntityLargeCombustionEngine) tileEntity;
            this.isExtreme = isExtreme;
            this.tier = isExtreme ? GTValues.IV : GTValues.EV;
        }

        @Override
        protected void updateRecipeProgress() {
            if (canRecipeProgress && drawEnergy(recipeEUt, true)) {

                //drain lubricant and invalidate if it fails
                if (totalContinuousRunningTime == 1 || totalContinuousRunningTime % 72 == 0) {
                    IMultipleTankHandler inputTank = combustionEngine.getInputFluidInventory();
                    if (LUBRICANT_STACK.isFluidStackIdentical(inputTank.drain(LUBRICANT_STACK, false))) {
                        inputTank.drain(LUBRICANT_STACK, true);
                    } else {
                        invalidate();
                        return;
                    }
                }

                //drain oxygen if present to boost production, and if the dynamo hatch supports it
                if (combustionEngine.isBoostAllowed() && (totalContinuousRunningTime == 1 || totalContinuousRunningTime % 20 == 0)) {
                    IMultipleTankHandler inputTank = combustionEngine.getInputFluidInventory();
                    FluidStack boosterStack = isExtreme ? LIQUID_OXYGEN_STACK : OXYGEN_STACK;
                    if (boosterStack.isFluidStackIdentical(inputTank.drain(boosterStack, false))) {
                        isOxygenBoosted = true;
                        inputTank.drain(boosterStack, true);
                    } else {
                        isOxygenBoosted = false;
                    }
                }

                drawEnergy(recipeEUt, false);

                //as recipe starts with progress on 1 this has to be > only not => to compensate for it
                if (++progressTime > maxProgressTime) {
                    completeRecipe();
                }
            }
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return super.shouldSearchForRecipes() && LUBRICANT_STACK.isFluidStackIdentical(((RecipeMapMultiblockController) metaTileEntity).getInputFluidInventory().drain(LUBRICANT_STACK, false));
        }

        @Override
        protected long getMaxVoltage() {
            //this multiplies consumption through parallel
            if (isOxygenBoosted)
                return GTValues.V[tier] * 2;
            else
                return GTValues.V[tier];
        }

        @Override
        protected long boostProduction(long production) {
            //this multiplies production without increasing consumption
            if (isOxygenBoosted)
                if (!isExtreme)
                    //recipe gives 2A EV and we want 3A EV, for 150% efficiency
                    return production * 3 / 2;
                else
                    //recipe gives 2A IV and we want 4A IV, for 200% efficiency
                    return production * 2;
            return production;
        }

        @Override
        public void invalidate() {
            isOxygenBoosted = false;
            super.invalidate();
        }
    }
}
