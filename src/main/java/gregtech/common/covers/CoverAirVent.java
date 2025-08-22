package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.properties.impl.DimensionProperty;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import static gregtech.api.GTValues.SECOND;

//From GT6
public class CoverAirVent extends CoverBase implements ITickable {

    private final int transferRate;
    private FluidStack airType;

    public CoverAirVent(CoverDefinition definition, CoverableView coverableView, EnumFacing attachedSide, int transferRate) {
        super(definition, coverableView, attachedSide);
        this.transferRate = transferRate;
    }

    @Override
    public boolean canAttach(CoverableView coverableView, EnumFacing attachedSide) {
        return coverableView.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer renderLayer) {
        Textures.AIR_VENT_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        if (getWorld().isRemote || getOffsetTimer() % SECOND != 0L) return;

        // Obstructed block in neighbor is not allowed, otherwise stop updating.
        if (getWorld().getBlockState(getPos().offset(getAttachedSide())).isFullBlock()) return;

        if (getTileEntityHere() == null) return;

        IFluidHandler fluidHandler = getTileEntityHere().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getAttachedSide());
        if (fluidHandler == null)
            return;

        if (airType == null) {
            RecipeMaps.GAS_COLLECTOR_RECIPES.getRecipeList().stream()
                    .filter(recipe -> {
                        // We check whitelist dimensions only, so if player in blacklisted dimension, then skipped.
                        DimensionProperty.DimensionPropertyList list = recipe.getProperty(DimensionProperty.getInstance(), null);
                        if (list == null) return false;
                        return list.whiteListDimensions.stream()
                               .anyMatch(dim -> getWorld().provider.getDimensionType().getId() == dim);
                    })
                    .findFirst()
                    .ifPresent(recipe -> {
                        if (!recipe.getFluidOutputs().isEmpty()) {
                            this.airType = new FluidStack(recipe.getFluidOutputs().get(0).getFluid(), this.transferRate);
                        }
                    });
        }

        if (airType != null) {
            fluidHandler.fill(airType.copy(), true);
        }
    }
}
