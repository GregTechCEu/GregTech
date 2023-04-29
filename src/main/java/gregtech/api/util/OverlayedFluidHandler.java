package gregtech.api.util;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.NotifiableFluidTankFromList;
import gregtech.api.recipes.FluidKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class OverlayedFluidHandler {

    private final OverlayedTank[] overlayedTanks;
    private final OverlayedTank[] originalTanks;
    private final IMultipleTankHandler overlayed;

    private final ObjectOpenCustomHashSet<IFluidTankProperties> tankDeniesSameFluidFill = new ObjectOpenCustomHashSet<>(IFluidTankPropertiesHashStrategy.create());
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

            if (overlayed.getTankAt(tank) instanceof NotifiableFluidTankFromList) {
                NotifiableFluidTankFromList nftfl = (NotifiableFluidTankFromList) overlayed.getTankAt(tank);
                if (!nftfl.getFluidTankList().get().allowSameFluidFill()) {
                    this.tankDeniesSameFluidFill.add(overlayed.getTankProperties()[tank]);
                }
            }
        }
    }

    public int insertStackedFluidKey(@Nonnull FluidKey toInsert, int amountToInsert) {
        if (amountToInsert <= 0) {
            return 0;
        }
        int insertedAmount = 0;
        // search for tanks with same fluid type first
        for (int i = 0; i < this.overlayedTanks.length; i++) {
            // initialize the tanks if they are not already populated
            initTank(i);
            OverlayedTank overlayedTank = this.overlayedTanks[i];
            // if the fluid key matches the tank, insert the fluid
            if (toInsert.equals(overlayedTank.fluidKey)) {
                if (!markFluidAsUnique(toInsert, i)) continue;
                int canInsertUpTo = overlayedTank.tryInsert(toInsert, amountToInsert);
                if (canInsertUpTo > 0) {
                    insertedAmount += canInsertUpTo;
                    amountToInsert -= canInsertUpTo;
                    if (amountToInsert <= 0) {
                        return insertedAmount;
                    }
                }
            }
        }
        // if we still have fluid to insert, loop through empty tanks until we find one that can accept the fluid
        for (int i = 0; i < this.overlayedTanks.length; i++) {
            OverlayedTank overlayedTank = this.overlayedTanks[i];
            // if the tank is empty
            if (overlayedTank.fluidKey == null) {
                if (!markFluidAsUnique(toInsert, i)) continue;
                //check if this tanks accepts the fluid we're simulating
                if (overlayed.getTankProperties()[i].canFillFluidType(new FluidStack(toInsert.getFluid(), amountToInsert))) {
                    int inserted = overlayedTank.tryInsert(toInsert, amountToInsert);
                    if (inserted > 0) {
                        insertedAmount += inserted;
                        amountToInsert -= inserted;
                        if (amountToInsert <= 0) {
                            return insertedAmount;
                        }
                    }
                }
            }
        }
        // return the amount of fluid that was inserted
        return insertedAmount;
    }

    /**
     * Marks the fluid as unique, to prevent further insertions in other tank slots.
     * If the fluid is already marked as unique in previous calls to this method,
     * this method fails and returns {@code false}.
     * <p>
     * If {@link IMultipleTankHandler#allowSameFluidFill()} is {@code false} for
     * {@link #overlayed} field or tank provider returned by {@link
     * NotifiableFluidTankFromList#getFluidTankList()}, this method gets bypassed
     * and {@code true} is returned without modifying any state.
     *
     * @param fluid     the fluid to mark
     * @param tankIndex the index of the tank
     * @return {@code true} if the fluid is not marked as unique in previous calls,
     * or the tank allows same fluids to be filled in multiple slots
     */
    private boolean markFluidAsUnique(@Nonnull FluidKey fluid, int tankIndex) {
        if (overlayed.getTankAt(tankIndex) instanceof NotifiableFluidTankFromList) {
            if (!overlayed.allowSameFluidFill() || tankDeniesSameFluidFill.contains(overlayed.getTankProperties()[tankIndex])) {
                NotifiableFluidTankFromList nftfl = (NotifiableFluidTankFromList) overlayed.getTankAt(tankIndex);
                return this.uniqueFluidMap
                        .computeIfAbsent(nftfl.getFluidTankList().get(), t -> new ObjectOpenHashSet<>())
                        .add(fluid);
            }
        } else if (!overlayed.allowSameFluidFill()) {
            return this.uniqueFluidMap
                    .computeIfAbsent(overlayed, t -> new ObjectOpenHashSet<>())
                    .add(fluid);
        }
        return true;
    }

    private static class OverlayedTank {

        private final int capacity;

        @Nullable
        private FluidKey fluidKey = null;
        private int fluidAmount = 0;

        OverlayedTank(IFluidTankProperties property) {
            FluidStack stackToMirror = property.getContents();
            if (stackToMirror != null) {
                this.fluidKey = new FluidKey(stackToMirror);
                this.fluidAmount = stackToMirror.amount;
            }
            this.capacity = property.getCapacity();
        }

        OverlayedTank(@Nullable FluidKey fluidKey, int fluidAmount, int capacity) {
            this.fluidKey = fluidKey;
            this.fluidAmount = fluidAmount;
            this.capacity = capacity;
        }

        /**
         * Tries to insert set amount of fluid into this tank. If operation succeeds,
         * the fluid key of this tank will be set to {@code fluid} and the {@code
         * fluidAmount} will be changed to reflect the state after insertion.
         *
         * @param fluid          Fluid key
         * @param amountToInsert Amount of the fluid to insert
         * @return Amount of fluid inserted into this tank
         */
        public int tryInsert(@Nonnull FluidKey fluid, int amountToInsert) {
            int maxInsert = Math.min(this.capacity - this.fluidAmount, amountToInsert);
            if (maxInsert > 0) {
                this.fluidKey = fluid;
                this.fluidAmount += maxInsert;
                return maxInsert;
            } else return 0;
        }

        public OverlayedTank copy() {
            return new OverlayedTank(this.fluidKey, this.fluidAmount, this.capacity);
        }
    }
}
