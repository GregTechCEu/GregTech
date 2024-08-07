package gregtech.common.pipelike.net.fluid;

import gregtech.api.graphnet.pipenet.insertion.TransferControl;

import gregtech.api.graphnet.pipenet.insertion.TransferControlProvider;
import gregtech.api.graphnet.predicate.test.FluidTestObject;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFluidTransferController {

    TransferControl<IFluidTransferController> CONTROL = new TransferControl<>("Fluid") {

        @Override
        public @NotNull IFluidTransferController get(@Nullable Object potentialHolder) {
            if (!(potentialHolder instanceof TransferControlProvider holder)) return DEFAULT;
            IFluidTransferController found = holder.getControllerForControl(CONTROL);
            return found == null ? DEFAULT : found;
        }

        @Override
        public @NotNull IFluidTransferController getNoPassage() {
            return NO_PASSAGE;
        }
    };

    IFluidTransferController DEFAULT = new IFluidTransferController() {};

    IFluidTransferController NO_PASSAGE = new IFluidTransferController() {

        @Override
        public int insertToHandler(@NotNull FluidTestObject testObject, int amount,
                                   @NotNull IFluidHandler destHandler, boolean doFill) {
            return 0;
        }

        @Override
        public @Nullable FluidStack extractFromHandler(@Nullable FluidTestObject testObject, int amount,
                                                       IFluidHandler sourceHandler, boolean doDrain) {
            return null;
        }
    };

    /**
     * @return the amount filled.
     */
    default int insertToHandler(@NotNull FluidTestObject testObject, int amount, @NotNull IFluidHandler destHandler, boolean doFill) {
        return destHandler.fill(testObject.recombine(amount), doFill);
    }

    /**
     * @return the fluidstack drained.
     */
    @Nullable
    default FluidStack extractFromHandler(@Nullable FluidTestObject testObject, int amount, IFluidHandler sourceHandler,
                                         boolean doDrain) {
        if (testObject == null) return sourceHandler.drain(amount, doDrain);
        else {
            FluidStack recombined = testObject.recombine();
            FluidStack drained = sourceHandler.drain(recombined, false);
            if (testObject.test(drained)) {
                if (doDrain) {
                    return sourceHandler.drain(recombined, true);
                } else {
                    return drained;
                }
            } else return null;
        }
    }
}
