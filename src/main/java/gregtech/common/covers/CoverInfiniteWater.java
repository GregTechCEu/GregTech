package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.cover2.CoverBase;
import gregtech.api.cover2.CoverDefinition2;
import gregtech.api.cover2.CoverableView;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class CoverInfiniteWater extends CoverBase implements ITickable {

    public CoverInfiniteWater(@NotNull CoverDefinition2 definition, @NotNull CoverableView coverableView, @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    @Override
    public void renderCover(@NotNull CCRenderState ccRenderState, @NotNull Matrix4 matrix4, IVertexOperation[] iVertexOperations, @NotNull Cuboid6 cuboid6, @NotNull BlockRenderLayer blockRenderLayer) {
        Textures.INFINITE_WATER.renderSided(getAttachedSide(), cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            IFluidHandler fluidHandler = getCoverable().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getAttachedSide());
            if (fluidHandler != null) {
                fluidHandler.fill(new FluidStack(FluidRegistry.WATER, 16000), true);
            }
        }
    }
}
