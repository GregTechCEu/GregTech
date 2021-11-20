package gregtech.api.util;

import gregtech.api.recipes.FluidKey;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;

public class OverlayedFluidHandler {

    private OverlayedTank[] overlayedTanks;
    private final OverlayedTank[] originalTanks;
    private final IFluidHandler overlayed;

    public OverlayedFluidHandler(IFluidHandler toOverlay) {
        this.overlayedTanks = new OverlayedTank[toOverlay.getTankProperties().length];
        this.originalTanks = new OverlayedTank[toOverlay.getTankProperties().length];
        this.overlayed = toOverlay;
        for (int tank = 0; tank < toOverlay.getTankProperties().length; tank++) {
            FluidStack stackToMirror = toOverlay.getTankProperties()[tank].getContents();
            if (stackToMirror != null) {
                this.originalTanks[tank] = new OverlayedTank(stackToMirror);
                this.overlayedTanks[tank] = new OverlayedTank(stackToMirror);
            }
        }
    }

    /**
     * Resets the {slots} array to the state when the handler was
     * first mirrored
     */

    public void reset() {
        this.overlayedTanks = originalTanks.clone();
    }

    public IFluidTankProperties[] getTankProperties() {
        return overlayed.getTankProperties();
    }

    public int insertFluidKey(int tank, @Nonnull FluidKey toInsert, int amount) {
        if (this.overlayedTanks[tank] == null || this.overlayedTanks[tank].getFluidKey().equals(toInsert)) {
            int inserted;
            inserted = overlayed.fill(new FluidStack(FluidRegistry.getFluid(toInsert.fluid), amount), true);
            if (inserted > 0) {
                this.overlayedTanks[tank] = new OverlayedTank(toInsert, inserted);
                return inserted - amount;
            }
        }
        return amount;
    }

    public int insertFluidStack(int tank, @Nonnull FluidStack toInsert) {
        if (this.overlayedTanks[tank] == null || this.overlayedTanks[tank].getFluidKey().equals(new FluidKey(toInsert))) {
            int inserted;
            inserted = overlayed.fill(toInsert, false);
            if (inserted > 0) {
                this.overlayedTanks[tank] = new OverlayedTank(new FluidKey(toInsert), inserted);
                return inserted - toInsert.amount;
            }
        }
        return toInsert.amount;
    }

    private static class OverlayedTank {
        private final FluidKey fluidKey;
        private final int count;

        OverlayedTank(FluidStack stackToMirror) {
            this.fluidKey = new FluidKey(stackToMirror);
            this.count = stackToMirror.amount;
        }

        OverlayedTank(FluidKey fluidKey, int count) {
            this.fluidKey = fluidKey;
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public FluidKey getFluidKey() {
            return fluidKey;
        }
    }
}
