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

public class MetaTileEntitySingleTurbine extends SimpleGeneratorMetaTileEntity {

    public MetaTileEntitySingleTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                       ICubeRenderer renderer, int tier,
                                       Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySingleTurbine(metaTileEntityId, recipeMap, renderer, getTier(),
                getTankScalingFunction());
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (getFrontFacing().getAxis() == EnumFacing.Axis.Y) {
            // If facing is up or down, render the turbine on all 4 sides.
            // Turbine renderer renders front and back, so just render it on both axes.
            this.renderer.renderOrientedState(renderState, translation, pipeline, EnumFacing.NORTH, workable.isActive(),
                    workable.isWorkingEnabled());
            this.renderer.renderOrientedState(renderState, translation, pipeline, EnumFacing.WEST, workable.isActive(),
                    workable.isWorkingEnabled());
        } else {
            super.renderOverlays(renderState, translation, pipeline);
        }
    }
}
