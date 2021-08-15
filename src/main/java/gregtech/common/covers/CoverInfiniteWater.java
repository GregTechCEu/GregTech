package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.render.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Arrays;
import java.util.Optional;

import static gregtech.api.unification.material.Materials.Lava;
import static gregtech.api.unification.material.Materials.Water;

public class CoverInfiniteWater extends CoverBehavior implements ITickable {

    private static final int SPEED = 4;
    private long timer = 0L;
    private final IFluidHandler fluidHandler;

    public CoverInfiniteWater(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        if(!coverHolder.getWorld().isRemote)
            fluidHandler = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
        else
            fluidHandler = null;
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        Textures.INFINITE_WATER.renderSided(attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public void update() {
        if(coverHolder.getWorld().isRemote) return;
        if (timer++ % ((20 * SPEED) / (Math.pow(2, SPEED - 1))) == 0) {
            FluidStack fluidStack = fluidHandler.drain(Water.getFluid(Integer.MAX_VALUE), false);
            Arrays.stream(fluidHandler.getTankProperties())
                    .filter(tank -> Optional.ofNullable(tank.getContents())
                            .orElse(Lava.getFluid(1)).isFluidEqual(Water.getFluid(1)) && tank.getCapacity() > 64000)
                    .findFirst()
                    .ifPresent($ -> fluidHandler.fill(Water.getFluid(16000), true));
            int amount = fluidStack != null ? Math.max(16000 - fluidStack.amount, 0) : 16000;
            fluidHandler.fill(Water.getFluid(amount), true);
        }
    }
}
