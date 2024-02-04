package gregtech.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.BlockUniqueCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.BRONZE_FIREBOX;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX;

public class MetaTileEntityArcFurnace extends RecipeMapMultiblockController {

    public MetaTileEntityArcFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FERMENTING_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityArcFurnace(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" XXX ", " FFF ", " XXX ", "     ", "     ")
                .aisle("XXXXX", "FG#GF", "XG#GX", " GPG ", " G G ")
                .aisle("XXXXX", "F###F", "X###X", " PPP ", " G G ")
                .aisle("XXXXX", "FG#GF", "XG#GX", " GPG ", " G G ")
                .aisle(" XSX ", " FFF ", " XXX ", "     ", "     ")
                .where('S', selfPredicate())
                .where('X', states(MetaBlocks.MACHINE_CASING.getState(MachineCasingType.ULV))
                        .setMinGlobalLimited(32)
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('F', states(MetaBlocks.BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('G', states(MetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.GRAPHITE_ELECTRODE)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }
}
