package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.*;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.RecipePropertyStorage;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityLargeChemicalReactor extends RecipeMapMultiblockController {

    private int coilTier;

    public MetaTileEntityLargeChemicalReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.LARGE_CHEMICAL_RECIPES);
        this.recipeMapWorkable = new LargeChemicalReactorWorkableHandler(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeChemicalReactor(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate casing = states(getCasingState()).setMinGlobalLimited(10);
        TraceabilityPredicate abilities = autoAbilities();
        return FactoryBlockPattern.start()
                .aisle("XXX", "XCX", "XXX")
                .aisle("XCX", "CPC", "XCX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', casing.or(abilities))
                .where('P', states(getPipeCasingState()))
                .where('C', heatingCoils().setMinGlobalLimited(1).setMaxGlobalLimited(1)
                        .or(abilities)
                        .or(casing))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        for (BlockWireCoil.CoilType coilType : BlockWireCoil.CoilType.values()) {
            MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder()
                    .where('S', MetaTileEntities.LARGE_CHEMICAL_REACTOR, EnumFacing.SOUTH)
                    .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING))
                    .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE))
                    .where('C', MetaBlocks.WIRE_COIL.getState(coilType))
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[3], EnumFacing.SOUTH)
                    .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[3], EnumFacing.NORTH)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[3], EnumFacing.SOUTH)
                    .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[3], EnumFacing.SOUTH)
                    .where('H', MetaTileEntities.FLUID_EXPORT_HATCH[3], EnumFacing.SOUTH)
                    .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING), EnumFacing.SOUTH);

            shapeInfo.add(baseBuilder.shallowCopy()
                    .aisle("XEX", "XCX", "XXX")
                    .aisle("XXX", "XPX", "XXX")
                    .aisle("IMO", "FSH", "XXX")
                    .build()
            );
            shapeInfo.add(baseBuilder.shallowCopy()
                    .aisle("XEX", "XXX", "XXX")
                    .aisle("XXX", "XPX", "XCX")
                    .aisle("IMO", "FSH", "XXX")
                    .build()
            );
            shapeInfo.add(baseBuilder.shallowCopy()
                    .aisle("XEX", "XXX", "XXX")
                    .aisle("XCX", "XPX", "XXX")
                    .aisle("IMO", "FSH", "XXX")
                    .build()
            );
            shapeInfo.add(baseBuilder.shallowCopy()
                    .aisle("XEX", "XXX", "XXX")
                    .aisle("XXX", "CPX", "XXX")
                    .aisle("IMO", "FSH", "XXX")
                    .build()
            );
            shapeInfo.add(baseBuilder.shallowCopy()
                    .aisle("XEX", "XXX", "XXX")
                    .aisle("XXX", "XPC", "XXX")
                    .aisle("IMO", "FSH", "XXX")
                    .build()
            );
        }
        return shapeInfo;
    }


    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.INERT_PTFE_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }

    protected IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.perfect_oc"));
        tooltip.add(I18n.format("gregtech.machine.large_chemical_reactor.tooltip.1"));
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_CHEMICAL_REACTOR_OVERLAY;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        if (type instanceof BlockWireCoil.CoilType)
            this.coilTier = ((BlockWireCoil.CoilType) type).ordinal();
        else
            this.coilTier = 0;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed() && coilTier != -1) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_chemical_reactor.voltage_tier",
                    GTValues.VN[coilTier == BlockWireCoil.CoilType.TRITANIUM.ordinal() ? GTValues.MAX : coilTier + 1]));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.coilTier = -1;
    }

    private int getCoilTier() {
        return this.coilTier;
    }

    private static class LargeChemicalReactorWorkableHandler extends MultiblockRecipeLogic {

        public LargeChemicalReactorWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity, true);
        }

        @Override
        protected int[] overclockRecipe(RecipePropertyStorage propertyStorage, int recipeEUt, boolean negativeEU, long maxVoltage, int duration, int maxOverclocks) {
            int coilTier = ((MetaTileEntityLargeChemicalReactor) metaTileEntity).getCoilTier();
            if (coilTier == -1)
                return super.overclockRecipe(propertyStorage, recipeEUt, negativeEU, maxVoltage, duration, maxOverclocks);

            return lcrOverclockingLogic(recipeEUt * (negativeEU ? -1 : 1),
                    maxVoltage,
                    duration,
                    maxOverclocks,
                    coilTier
            );
        }

        @Nonnull
        public static int[] lcrOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration, int maxOverclocks, int coilTier) {
            // perfect overclock until the voltage reaches the coil limit, skip cupronickel since LV cannot OC
            if (coilTier > 0) {
                // use the normal overclock logic to do perfect OCs up to the coil tier voltage, +1 to avoid ULV
                int[] overclock = standardOverclockingLogic(recipeEUt, GTValues.V[coilTier + 1], recipeDuration, PERFECT_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, maxOverclocks);

                // overclock normally as much as possible after perfects are exhausted
                return standardOverclockingLogic(overclock[0], maximumVoltage, overclock[1], STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, maxOverclocks);
            }

            // no perfects are performed, do normal overclocking
            return standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, maxOverclocks);
        }
    }
}
