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
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityLargeCombustionEngine extends FuelMultiblockController {

    public MetaTileEntityLargeCombustionEngine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COMBUSTION_GENERATOR_FUELS_2, GTValues.EV);
        this.recipeMapWorkable = new LargeCombustionEngineWorkableHandler(this);
        this.recipeMapWorkable.enableOverclockVoltage();
        this.recipeMapWorkable.setOverclockTier(GTValues.EV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeCombustionEngine(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            FluidStack lubricantStack = getImportFluids().drain(Materials.Lubricant.getFluid(Integer.MAX_VALUE), false);
            FluidStack oxygenStack = getImportFluids().drain(Materials.Oxygen.getFluid(Integer.MAX_VALUE), false);
//            FluidStack fuelStack = ((LargeCombustionEngineWorkableHandler) recipeMapWorkable).getFuelStack();
            int lubricantAmount = lubricantStack == null ? 0 : lubricantStack.amount;
            int oxygenAmount = oxygenStack == null ? 0 : oxygenStack.amount;
//            int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

//            ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.lubricant_amount", lubricantAmount));
//            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.fuel_amount", fuelAmount, fuelName));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_combustion_engine.oxygen_amount", oxygenAmount));
            textList.add(new TextComponentTranslation(oxygenAmount >= 2 ? "gregtech.multiblock.large_combustion_engine.oxygen_boosted" : "gregtech.multiblock.large_combustion_engine.supply_oxygen_to_boost"));

            if (isStructureObstructed())
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.obstructed").setStyle(new Style().setColor(TextFormatting.RED)));
        }
        super.addDisplayText(textList);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate predicate = states(getCasingState()).or(autoAbilities(false, true, true, true, true, true, true));
        return FactoryBlockPattern.start()
                .aisle("XXX", "XDX", "XXX")
                .aisle("XCX", "CGC", "XTX")
                .aisle("XCX", "CGC", "XTX")
                .aisle("AAA", "AYA", "AAA")
                .where('X', states(getCasingState()))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TITANIUM_GEARBOX)))
                .where('C', predicate)
                .where('T', predicate.or(abilities(MultiblockAbility.MUFFLER_HATCH)))
                .where('D', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('A', states(MetaBlocks.MULTIBLOCK_CASING.getState(MultiblockCasingType.ENGINE_INTAKE_CASING)).addTooltips("gregtech.multiblock.pattern.clear_amount_1"))
                .where('Y', selfPredicate())
                .build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.STABLE_TITANIUM_CASING;
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

    @SuppressWarnings("InnerClassMayBeStatic")
    private class LargeCombustionEngineWorkableHandler extends MultiblockFuelRecipeLogic {

        public LargeCombustionEngineWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }
    }
}
