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
        if (this.overlayedTanks[tank] == null) {
            IFluidTankProperties fluidTankProperties = overlayed.getTankProperties()[tank];
            this.originalTanks[tank] = new OverlayedTank(fluidTankProperties);
            this.overlayedTanks[tank] = new OverlayedTank(fluidTankProperties);
        }
    }

    public int insertFluidKey(int tank, @Nonnull FluidKey toInsert, int amount) {
        initTank(tank);
        if (this.overlayedTanks[tank].getFluidKey() == null || this.overlayedTanks[tank].getFluidKey().equals(toInsert)) {
            int insertable = 0;
            //refer to the original tank for the possibility of the fluid tank being filtered
            if (overlayed.getTankProperties()[tank].canFillFluidType(new FluidStack(toInsert.getFluid(), amount))){
                insertable = Math.min(amount, this.overlayedTanks[tank].getCapacity() - this.overlayedTanks[tank].getCount());
            }
            if (insertable > 0) {
                this.overlayedTanks[tank].setFluidKey(toInsert);
                this.overlayedTanks[tank].setCount(insertable);
                return insertable;
            }
        }
        return 0;
    }

    private static class OverlayedTank {
        private FluidKey fluidKey = null;
        private int count = 0;
        private int capacity = 0;

        OverlayedTank(IFluidTankProperties property) {
            FluidStack stackToMirror = property.getContents();
            if (stackToMirror != null) {
                this.fluidKey = new FluidKey(stackToMirror);
                this.count = stackToMirror.amount;
            }
            this.capacity = property.getCapacity();
        }

        OverlayedTank(FluidKey fluidKey, int count, int capacity) {
            this.fluidKey = fluidKey;
            this.count = count;
            this.capacity = capacity;
        }

        public int getCapacity() {
            return capacity;
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
            return new OverlayedTank(this.fluidKey, this.count, this.capacity);
        }
    }
}
