package gregtech.api.util;

import gregtech.api.recipes.FluidKey;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;

public class OverlayedFluidHandler {

    private final OverlayedTank[] overlayedTanks;
    private final OverlayedTank[] originalTanks;
    private final IFluidHandler overlayed;

    public OverlayedFluidHandler(IFluidHandler toOverlay) {
        this.overlayedTanks = new OverlayedTank[toOverlay.getTankProperties().length];
        this.originalTanks = new OverlayedTank[toOverlay.getTankProperties().length];
        this.overlayed = toOverlay;
    }

    /**
     * Resets the {slots} array to the state when the handler was
     * first mirrored
     */

    public void reset() {
        for (int i = 0; i < this.originalTanks.length; i++) {
            if (this.originalTanks[i] != null) {
                this.overlayedTanks[i] = this.originalTanks[i].copy();
            }
        }
    }

    public IFluidTankProperties[] getTankProperties() {
        return overlayed.getTankProperties();
    }

    private void initTank(int tank) {
        FluidStack stackToMirror = overlayed.getTankProperties()[tank].getContents();
        this.originalTanks[tank] = new OverlayedTank(stackToMirror);
        this.overlayedTanks[tank] = new OverlayedTank(stackToMirror);
    }

    public int insertFluidKey(int tank, @Nonnull FluidKey toInsert, int amount) {
        initTank(tank);
        if (this.overlayedTanks[tank].getFluidKey() == null || this.overlayedTanks[tank].getFluidKey().equals(toInsert)) {
            int inserted;
            inserted = overlayed.fill(new FluidStack(toInsert.getFluid(), amount), false);
            if (inserted > 0) {
                this.overlayedTanks[tank].setFluidKey(toInsert);
                this.overlayedTanks[tank].setCount(inserted);
                return inserted;
            }
        }
        return 0;
    }

    public OverlayedFluidHandler copy() {
        OverlayedFluidHandler copy = new OverlayedFluidHandler(this.overlayed);
        for (int i = 0; i < this.originalTanks.length; i++) {
            if (this.originalTanks[i] != null) {
                copy.originalTanks[i] = this.originalTanks[i].copy();
                copy.overlayedTanks[i] = this.originalTanks[i].copy();
            }
        }
        return copy;
    }

    public void apply(OverlayedFluidHandler toApply) {
        for (int i = 0; i < this.originalTanks.length; i++) {
            this.originalTanks[i] = toApply.originalTanks[i];
            this.overlayedTanks[i] = toApply.overlayedTanks[i];
        }
    }

    private static class OverlayedTank {
        private FluidKey fluidKey = null;
        private int count = 0;

        OverlayedTank(FluidStack stackToMirror) {
            if (stackToMirror != null) {
                this.fluidKey = new FluidKey(stackToMirror);
                this.count = stackToMirror.amount;
            }
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

        public void setFluidKey(FluidKey fluidKey) {
            this.fluidKey = fluidKey;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public OverlayedTank copy() {
            return new OverlayedTank(this.fluidKey, this.count);
        }
    }
}
