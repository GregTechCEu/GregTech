package gregtech.common.metatileentities.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import java.util.function.Function;

public class MetaTileEntitySingleCombustion extends SimpleGeneratorMetaTileEntity {

    public MetaTileEntitySingleCombustion(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                          ICubeRenderer renderer, int tier,
                                          Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySingleCombustion(metaTileEntityId, recipeMap, renderer, getTier(),
                getTankScalingFunction());
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (getFrontFacing() == EnumFacing.DOWN) {
            // Trick the render into rendering on the top, as this case didn't render otherwise
            this.renderer.renderOrientedState(renderState, translation, pipeline, EnumFacing.NORTH, workable.isActive(),
                    workable.isWorkingEnabled());
        } else if (getFrontFacing() != EnumFacing.UP) {
            // Don't render the top overlay if the facing is up, as the textures
            // would collide, otherwise render normally.
            super.renderOverlays(renderState, translation, pipeline);
        }
    }
}
