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

    public int insertStackedFluidKey(@Nonnull FluidKey toInsert, int amountToInsert) {
        int insertedAmount = 0;
        for (int i = 0; i < this.overlayedTanks.length; i++) {
            // populate the tanks if they are not already populated
            initTank(i);
            // if the fluid key matches the tank, insert the fluid
            if (toInsert.equals(this.overlayedTanks[i].getFluidKey())) {
                int spaceInTank = this.overlayedTanks[i].getCapacity() - this.overlayedTanks[i].getFluidAmount();
                int insertable = Math.min(spaceInTank, amountToInsert);
                if (insertable > 0) {
                    insertedAmount += insertable;
                    this.overlayedTanks[i].setFluidKey(toInsert);
                    this.overlayedTanks[i].setFluidAmount(this.overlayedTanks[i].getFluidAmount() + insertable);
                    amountToInsert -= insertable;
                }
                if (amountToInsert == 0) {
                    return insertedAmount;
                }
            }
        }
        // if we still have fluid to insert, insert it into the first tank that can accept it
        if (amountToInsert > 0) {
            // loop through the tanks until we find one that can accept the fluid
            for (OverlayedTank overlayedTank : this.overlayedTanks) {
                // if the tank is empty
                if (overlayedTank.getFluidKey() == null) {
                    int insertable = Math.min(overlayedTank.getCapacity(), amountToInsert);
                    if (insertable > 0) {
                        insertedAmount += insertable;
                        overlayedTank.setFluidKey(toInsert);
                        overlayedTank.setFluidAmount(overlayedTank.getFluidAmount() + insertable);
                        amountToInsert -= insertedAmount;
                    }
                    if (amountToInsert == 0) {
                        return insertedAmount;
                    }
                }
            }
        }
        // return the amount of fluid that was inserted
        return insertedAmount;
    }

    private static class OverlayedTank {
        private FluidKey fluidKey = null;
        private int fluidAmount = 0;
        private int capacity = 0;

        OverlayedTank(IFluidTankProperties property) {
            FluidStack stackToMirror = property.getContents();
            if (stackToMirror != null) {
                this.fluidKey = new FluidKey(stackToMirror);
                this.fluidAmount = stackToMirror.amount;
            }
            this.capacity = property.getCapacity();
        }

        OverlayedTank(FluidKey fluidKey, int fluidAmount, int capacity) {
            this.fluidKey = fluidKey;
            this.fluidAmount = fluidAmount;
            this.capacity = capacity;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getFluidAmount() {
            return fluidAmount;
        }

        public FluidKey getFluidKey() {
            return fluidKey;
        }

        public void setFluidKey(FluidKey fluidKey) {
            this.fluidKey = fluidKey;
        }

        public void setFluidAmount(int fluidAmount) {
            this.fluidAmount = fluidAmount;
        }

        public OverlayedTank copy() {
            return new OverlayedTank(this.fluidKey, this.fluidAmount, this.capacity);
        }
    }
}
