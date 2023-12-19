package gregtech.common.covers;

import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverInfiniteWater extends CoverBase implements ITickable {

    public CoverInfiniteWater(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                              @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    @Override
    public void renderCover(@NotNull CCRenderState ccRenderState, @NotNull Matrix4 matrix4,
                            IVertexOperation[] iVertexOperations, @NotNull Cuboid6 cuboid6,
                            @NotNull BlockRenderLayer blockRenderLayer) {
        Textures.INFINITE_WATER.renderSided(getAttachedSide(), cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            IFluidHandler fluidHandler = getCoverableView()
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getAttachedSide());
            if (fluidHandler != null) {
                fluidHandler.fill(new FluidStack(FluidRegistry.WATER, 16000), true);
            }
        }
    }
}
