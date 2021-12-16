package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.*;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
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
            long maxVoltage = this.recipeMapWorkable.getMaxVoltage();
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_chemical_reactor.voltage_tier", maxVoltage, GTValues.VN[GTUtility.getTierByVoltage(maxVoltage)]));
            if (GTValues.HT)
                textList.add(new TextComponentTranslation("gregtech.machine.large_chemical_reactor.tooltip.ht"));
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

    @SuppressWarnings("InnerClassMayBeStatic")
    private static class LargeChemicalReactorWorkableHandler extends MultiblockRecipeLogic {

        public LargeChemicalReactorWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity, true);
        }

        @Override
        public long getMaxVoltage() {
            int coilTier = ((MetaTileEntityLargeChemicalReactor) metaTileEntity).getCoilTier();
            if (coilTier == -1)
                return 0;

            // for GTValues.HT = true, allow any voltage with the best coil
            long maxVoltage = super.getMaxVoltage();
            if (GTValues.HT && coilTier == BlockWireCoil.CoilType.TRITANIUM.ordinal())
                return maxVoltage;

            // coil tier is equal to the maximum allowed voltage, + 1 to avoid ULV
            return Math.min(maxVoltage, GTValues.V[coilTier + 1]);
        }
    }
}
