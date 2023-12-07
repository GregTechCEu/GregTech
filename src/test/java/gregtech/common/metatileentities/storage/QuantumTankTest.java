package gregtech.common.metatileentities.storage;

import gregtech.Bootstrap;
import gregtech.api.GTValues;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_FLUID_AMOUNT;
import static gregtech.api.util.GTUtility.gregtechId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class QuantumTankTest {

    private static FluidStack WATER;
    private static FluidStack LAVA;
    private static ItemStack BUCKET_WATER;
    private static ItemStack BUCKET_LAVA;

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
        WATER = new FluidStack(FluidRegistry.WATER, 1000);
        LAVA = new FluidStack(FluidRegistry.LAVA, 1000);
        BUCKET_WATER = FluidUtil.getFilledBucket(WATER);
        BUCKET_LAVA = FluidUtil.getFilledBucket(LAVA);
    }

    @Test
    public void Test_Valid() {
        for (var quantumTank : createInstances()) {
            assertThat(quantumTank, is(notNullValue()));
        }
    }

    @Test
    public void Test_Insertion() {
        for (var quantumTank : createInstances()) {
            IFluidHandler handler = quantumTank.getFluidInventory();

            int filled = handler.fill(WATER.copy(), false);
            assertThat("Not all fluid was inserted!", filled == 1000);

            handler.fill(WATER.copy(), true);
            assertThat("Quantum tank was not fully filled!", quantumTank.fluidTank.getFluidAmount() == 1000);

            filled = handler.fill(LAVA.copy(), true);
            assertThat("Quantum tank inserted fluid different from it's internal fluid!", filled == 0);

            // todo test here to check inserting via filled fluid containers
        }
    }

    @Test
    public void Test_Voiding() {
        for (var quantumTank : createInstances()) {
            IFluidHandler handler = quantumTank.getFluidInventory();
            FluidStack resource = WATER.copy();
            resource.amount = Integer.MAX_VALUE;
            int inserted = handler.fill(resource, true);
            assertThat("Quantum Tank accepted too much fluid!",
                    inserted == handler.getTankProperties()[0].getCapacity());

            quantumTank.setVoiding(true);
            inserted = handler.fill(resource, true);
            assertThat("Fluid was not properly voided!", inserted == resource.amount);

            inserted = handler.fill(LAVA.copy(), true);
            assertThat("Quantum tank voided the wrong fluid!", inserted == 0);
        }
    }

    @Test
    public void Test_Extraction() {
        for (var quantumTank : createInstances()) {
            IFluidHandler handler = quantumTank.getFluidInventory();
            FluidStack inTank = LAVA.copy();
            inTank.amount = 5000;
            handler.fill(inTank, true);

            FluidStack extracted = handler.drain(2500, true);
            assertThat("extracted was null!", extracted != null);
            assertThat("Too much/little fluid was extracted!", extracted.amount == 2500);

            inTank = handler.getTankProperties()[0].getContents();
            assertThat("tank contents was null!", inTank != null);
            assertThat("Too much/little fluid in quantum tank!", inTank.amount == 2500);

            // todo tests for extracting with an empty fluid container
        }
    }

    private QuantumTankWrapper[] createInstances() {
        QuantumTankWrapper[] quantumTanks = new QuantumTankWrapper[10];
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            quantumTanks[i] = new QuantumTankWrapper(gregtechId("super_tank." + voltageName), i + 1,
                    4000000 * (int) Math.pow(2, i));
        }

        for (int i = 5; i < quantumTanks.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            int capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000 * (int) Math.pow(2, i);
            quantumTanks[i] = new QuantumTankWrapper(gregtechId("quantum_tank." + voltageName), i, capacity);
        }
        return quantumTanks;
    }

    private static class QuantumTankWrapper extends MetaTileEntityQuantumTank {

        public QuantumTankWrapper(ResourceLocation metaTileEntityId, int tier, int maxFluidCapacity) {
            super(metaTileEntityId, tier, maxFluidCapacity);
        }

        @Override
        protected void setVoiding(boolean isVoiding) {
            this.voiding = isVoiding;
        }

        private void fakeUpdate(boolean isRemote) {
            EnumFacing currentOutputFacing = getOutputFacing();
            if (!isRemote) {
                fillContainerFromInternalTank();
                fillInternalTankFromFluidContainer();
                if (isAutoOutputFluids()) {
                    pushFluidsIntoNearbyHandlers(currentOutputFacing);
                }

                FluidStack currentFluid = fluidTank.getFluid();
                if (previousFluid == null) {
                    // tank was empty, but now is not
                    if (currentFluid != null) {
                        updatePreviousFluid(currentFluid);
                    }
                } else {
                    if (currentFluid == null) {
                        // tank had fluid, but now is empty
                        updatePreviousFluid(null);
                    } else if (previousFluid.getFluid().equals(currentFluid.getFluid()) &&
                            previousFluid.amount != currentFluid.amount) {
                                // tank has fluid with changed amount
                                previousFluid.amount = currentFluid.amount;
                                writeCustomData(UPDATE_FLUID_AMOUNT, buf -> buf.writeInt(currentFluid.amount));
                            } else
                        if (!previousFluid.equals(currentFluid)) {
                            // tank has a different fluid from before
                            updatePreviousFluid(currentFluid);
                        }
                }
            }
        }

        @Override
        protected void updatePreviousFluid(FluidStack currentFluid) {
            previousFluid = currentFluid == null ? null : currentFluid.copy();
        }
    }
}
