package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockMultiblockTank;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

public class MetaTileEntityFrothFlotationTank extends RecipeMapMultiblockController {
    public MetaTileEntityFrothFlotationTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FROTH_FLOTATION);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFrothFlotationTank(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("   B   ", "   B   ", "   B   ", "       ", "       ")
                .aisle("  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ")
                .aisle(" AAAAA ", " ABBBA ", " ABBBA ", " ADDDA ", " A   A ")
                .aisle("BAAAAAB", "BABBBAB", "BABBBAB", " ADBDA ", " A E A ")
                .aisle(" AAAAA ", " ABBBA ", " ABBBA ", " ADDDA ", " A   A ")
                .aisle("  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ")
                .aisle("   B   ", "   B   ", "   S   ", "       ", "       ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN)).setMinGlobalLimited(51)
                        .or(autoAbilities(true, true, true, true, true, true, true)))
                .where('B', states(MetaBlocks.BOILER_CASING.getState((BoilerCasingType.STEEL_PIPE))))
                .where('D', states(MetaBlocks.MULTIBLOCK_TANK.getState(BlockMultiblockTank.MultiblockTankType.FLOTATION)))
                .where('E', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where(' ', any())
                .build();
    }
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc", new Object[0]));
    }

    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FROTH_FLOTATION_OVERLAY;
    }
}
