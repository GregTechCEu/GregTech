package gregtech.api.util;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.NotifiableFluidTankFromList;
import gregtech.api.recipes.FluidKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OverlayedFluidHandler {

    private final OverlayedTank[] overlayedTanks;
    private final OverlayedTank[] originalTanks;
    private final IMultipleTankHandler overlayed;
    private boolean allowSameFluidFill = true;
    private final ObjectOpenHashSet<IFluidTankProperties> tankDeniesSameFluidFill = new ObjectOpenHashSet<>();
    private final Map<IMultipleTankHandler, ObjectOpenHashSet<FluidKey>> uniqueFluidMap = new Object2ObjectOpenHashMap<>();

    public OverlayedFluidHandler(IMultipleTankHandler toOverlay) {
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
        uniqueFluidMap.forEach((k, v) -> v.clear());
    }

    public IFluidTankProperties[] getTankProperties() {
        return overlayed.getTankProperties();
    }

    private void initTank(int tank) {
        if (this.overlayedTanks[tank] == null) {
            IFluidTankProperties fluidTankProperties = overlayed.getTankProperties()[tank];
            this.originalTanks[tank] = new OverlayedTank(fluidTankProperties);
            this.overlayedTanks[tank] = new OverlayedTank(fluidTankProperties);

            if (!overlayed.allowSameFluidFill()) {
                this.allowSameFluidFill = false;
            }
            if (overlayed.getTankAt(tank) instanceof NotifiableFluidTankFromList) {
                NotifiableFluidTankFromList nftfl = (NotifiableFluidTankFromList) overlayed.getTankAt(tank);
                if (!nftfl.getFluidTankList().get().allowSameFluidFill()) {
                    this.tankDeniesSameFluidFill.add(overlayed.getTankProperties()[tank]);
                    uniqueFluidMap.computeIfAbsent(nftfl.getFluidTankList().get(), list -> new ObjectOpenHashSet<>());
                }
            } else if (!this.allowSameFluidFill) {
                uniqueFluidMap.computeIfAbsent(overlayed, list -> new ObjectOpenHashSet<>());
            }
        }
    }

    public int insertStackedFluidKey(@Nonnull FluidKey toInsert, int amountToInsert) {
        int insertedAmount = 0;
        for (int i = 0; i < this.overlayedTanks.length; i++) {
            initTank(i);
            // populate the tanks if they are not already populated
            // if the fluid key matches the tank, insert the fluid
            OverlayedTank overlayedTank = this.overlayedTanks[i];
            if (toInsert.equals(overlayedTank.getFluidKey())) {
                if ((tankDeniesSameFluidFill.contains(overlayed.getTankProperties()[i]) || !this.allowSameFluidFill)) {
                    if (overlayed.getTankAt(i) instanceof NotifiableFluidTankFromList) {
                        NotifiableFluidTankFromList nftfl = (NotifiableFluidTankFromList) overlayed.getTankAt(i);
                        if (!(uniqueFluidMap.get(nftfl.getFluidTankList().get()).add(toInsert))) {
                            continue;
                        }
                    } else {
                        if (!(uniqueFluidMap.get(overlayed).add(toInsert))) {
                            continue;
                        }
                    }
                }
                int spaceInTank = overlayedTank.getCapacity() - overlayedTank.getFluidAmount();
                int canInsertUpTo = Math.min(spaceInTank, amountToInsert);
                if (canInsertUpTo > 0) {
                    insertedAmount += canInsertUpTo;
                    overlayedTank.setFluidKey(toInsert);
                    overlayedTank.setFluidAmount(overlayedTank.getFluidAmount() + canInsertUpTo);
                    amountToInsert -= canInsertUpTo;
                }
                if (amountToInsert == 0) {
                    return insertedAmount;
                }
            }
        }
        // if we still have fluid to insert, insert it into the first tank that can accept it
        if (amountToInsert > 0) {
            // loop through the tanks until we find one that can accept the fluid
            for (int i = 0, tanksLength = this.overlayedTanks.length; i < tanksLength; i++) {
                OverlayedTank overlayedTank = this.overlayedTanks[i];
                // if the tank is empty
                if (overlayedTank.getFluidKey() == null) {
                    if ((tankDeniesSameFluidFill.contains(overlayed.getTankProperties()[i]) || !this.allowSameFluidFill)) {
                        IMultipleTankHandler mth = (IMultipleTankHandler) overlayed;
                        if (mth.getTankAt(i) instanceof NotifiableFluidTankFromList) {
                            NotifiableFluidTankFromList nftfl = (NotifiableFluidTankFromList) mth.getTankAt(i);
                            if (!(uniqueFluidMap.get(nftfl.getFluidTankList().get()).add(toInsert))) {
                                continue;
                            }
                        } else {
                            if (!(uniqueFluidMap.get(mth).add(toInsert))) {
                                continue;
                            }
                        }
                    }
                    //check if this tanks accepts the fluid we're simulating
                    if (overlayed.getTankProperties()[i].canFillFluidType(new FluidStack(toInsert.getFluid(), amountToInsert))) {
                        int canInsertUpTo = Math.min(overlayedTank.getCapacity(), amountToInsert);
                        if (canInsertUpTo > 0) {
                            insertedAmount += canInsertUpTo;
                            overlayedTank.setFluidKey(toInsert);
                            overlayedTank.setFluidAmount(canInsertUpTo);
                            amountToInsert -= canInsertUpTo;
                        }
                        if (amountToInsert == 0) {
                            return insertedAmount;
                        }
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
