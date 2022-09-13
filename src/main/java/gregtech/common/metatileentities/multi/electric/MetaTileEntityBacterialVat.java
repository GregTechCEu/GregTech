package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.IRadiationHatch;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.RadiationProperty;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityBacterialVat extends RecipeMapMultiblockController {

    public MetaTileEntityBacterialVat(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.BACTERIAL_VAT_RECIPES);
        this.recipeMapWorkable = new BacterialVatRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBacterialVat(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "GGGGG", "GGGGG", "GGGGG", "XXXXX")
                .aisle("XXXXX", "G   G", "G   G", "G   G", "XXXXX")
                .aisle("XXXXX", "G   G", "G   G", "G   G", "XXXXX")
                .aisle("XXXXX", "G   G", "G   G", "G   G", "XXXXX")
                .aisle("XXSXX", "GGGGG", "GGGGG", "GGGGG", "XXXXX")
                .where('X', states(getCasingState()).setMinGlobalLimited(42)
                        .or(autoAbilities(true, true, true, true, true, false, false))
                        .or(abilities(MultiblockAbility.RADIATION_HATCH))
                        .or(metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.EXPORT_FLUIDS).stream()
                                .filter(mte->!(mte instanceof MetaTileEntityMultiFluidHatch))
                                .toArray(MetaTileEntity[]::new)).setMaxGlobalLimited(1)))
                .where('S', selfPredicate())
                .where('G', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS)))
                .where(' ', air())
                .build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (this.isStructureFormed()) {
            float totalRad = 0;
            for (IRadiationHatch hatch : getAbilities(MultiblockAbility.RADIATION_HATCH)) {
                totalRad += hatch.getRadValue();
            }
            textList.add(new TextComponentTranslation("gregtech.machine.bacterial_vat.radiation_count", totalRad));
        }
        super.addDisplayText(textList);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    protected boolean checkRecipeRads(Recipe recipe) {
        float totalRad = 0;
        for (IRadiationHatch hatch : getAbilities(MultiblockAbility.RADIATION_HATCH)) {
            totalRad += hatch.getRadValue();
            if (hatch.isCreative()) {
                return true;
            }
        }
        return recipe.getProperty(RadiationProperty.getInstance(), 0.0f) == totalRad;
    }

    private static class BacterialVatRecipeLogic extends MultiblockRecipeLogic {

        public BacterialVatRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        protected boolean checkRecipe(@Nonnull Recipe recipe) {
            return ((MetaTileEntityBacterialVat) metaTileEntity).checkRecipeRads(recipe) && super.checkRecipe(recipe);
        }
    }
}
