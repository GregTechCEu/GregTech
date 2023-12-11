package gregtech.common.metatileentities.converter;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class ConverterTraitTest {

    private static ConverterTestWrapper converter_1A;
    private static ConverterTestWrapper converter_4A;
    private static ConverterTestWrapper converter_8A;
    private static ConverterTestWrapper converter_16A;

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
        converter_1A = new ConverterTestWrapper(GTValues.LV, 1);
        converter_4A = new ConverterTestWrapper(GTValues.LV, 4);
        converter_8A = new ConverterTestWrapper(GTValues.LV, 8);
        converter_16A = new ConverterTestWrapper(GTValues.LV, 16);
    }

    @Test
    public void Test_Converters_Valid() {
        MatcherAssert.assertThat(converter_1A, notNullValue());
        MatcherAssert.assertThat(converter_4A, notNullValue());
        MatcherAssert.assertThat(converter_8A, notNullValue());
        MatcherAssert.assertThat(converter_16A, notNullValue());
    }

    @Test
    public void Test_FE_To_EU() {
        resetEnergyStorage();
        converter_1A.setFeToEu(true);

        IEnergyStorage storage = converter_1A.getCapability(CapabilityEnergy.ENERGY, EnumFacing.SOUTH);
        IEnergyContainer container = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                EnumFacing.NORTH);
        MatcherAssert.assertThat(storage, notNullValue());
        MatcherAssert.assertThat(container, notNullValue());

        // Add FE
        storage.receiveEnergy(128, false); // 1A LV, in FE
        MatcherAssert.assertThat(storage.getEnergyStored(), is(128));
        MatcherAssert.assertThat(container.getEnergyStored(), is(32L));

        // Push to EU
        ConverterTrait trait = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_CONVERTER, null);
        MatcherAssert.assertThat(trait, notNullValue());
        trait.pushEnergy();
        MatcherAssert.assertThat(EUStorage.getEnergyStored(), is(32L));
        MatcherAssert.assertThat(container.getEnergyStored(), is(0L));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(0));
    }

    @Test
    public void Test_FE_To_EU_Off_Ratio() {
        resetEnergyStorage();
        converter_1A.setFeToEu(true);

        IEnergyStorage storage = converter_1A.getCapability(CapabilityEnergy.ENERGY, EnumFacing.SOUTH);
        IEnergyContainer container = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                EnumFacing.NORTH);
        MatcherAssert.assertThat(storage, notNullValue());
        MatcherAssert.assertThat(container, notNullValue());

        // Add weird amount of FE
        int accepted = storage.receiveEnergy(130, false); // 1A LV + a little extra, in FE
        MatcherAssert.assertThat(accepted, is(128));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(128));
        MatcherAssert.assertThat(container.getEnergyStored(), is(32L));
    }

    /**
     * This test demonstrates a potential problem, being that the minimum
     * amount of FE a converter can receive is the ratio of FE -> EU.
     *
     * This could be addressed at a later date if it causes significant problems,
     * but I do not think that it is a true problem at the moment.
     */
    @Test
    public void Test_Show_FE_Minimum() {
        resetEnergyStorage();
        converter_1A.setFeToEu(true);

        IEnergyStorage storage = converter_1A.getCapability(CapabilityEnergy.ENERGY, EnumFacing.SOUTH);
        IEnergyContainer container = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                EnumFacing.NORTH);
        MatcherAssert.assertThat(storage, notNullValue());
        MatcherAssert.assertThat(container, notNullValue());

        // Demonstrate that less than 4 FE cannot be accepted
        int accepted = storage.receiveEnergy(3, false);
        MatcherAssert.assertThat(accepted, is(0));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(0));
        MatcherAssert.assertThat(container.getEnergyStored(), is(0L));
    }

    @Test
    public void Test_EU_To_FE() {
        resetEnergyStorage();
        converter_1A.setFeToEu(false);

        IEnergyContainer container = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                EnumFacing.SOUTH);
        IEnergyStorage storage = converter_1A.getCapability(CapabilityEnergy.ENERGY, EnumFacing.NORTH);
        MatcherAssert.assertThat(container, notNullValue());
        MatcherAssert.assertThat(storage, notNullValue());

        // Add EU
        container.addEnergy(32); // 1A LV
        MatcherAssert.assertThat(container.getEnergyStored(), is(32L));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(128));

        // Push to FE
        ConverterTrait trait = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_CONVERTER, null);
        MatcherAssert.assertThat(trait, notNullValue());
        trait.pushEnergy();
        MatcherAssert.assertThat(FEStorage.getEnergyStored(), is(128));
        MatcherAssert.assertThat(container.getEnergyStored(), is(0L));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(0));
    }

    @Test
    public void Test_No_Energy_Loss() {
        resetEnergyStorage();
        converter_1A.setFeToEu(false);

        IEnergyContainer container = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                EnumFacing.SOUTH);
        IEnergyStorage storage = converter_1A.getCapability(CapabilityEnergy.ENERGY, EnumFacing.NORTH);
        MatcherAssert.assertThat(container, notNullValue());
        MatcherAssert.assertThat(storage, notNullValue());

        // Add EU
        container.addEnergy(32); // 1A LV
        MatcherAssert.assertThat(container.getEnergyStored(), is(32L));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(128));

        // Mostly fill dummy FE Storage
        FEStorage.receiveEnergy(995, false);
        MatcherAssert.assertThat(FEStorage.getEnergyStored(), is(995));

        // Drain a little bit of energy
        // Storage can hold 5 FE, but converter can only output 4 due to not voiding EU
        ConverterTrait trait = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_CONVERTER, null);
        MatcherAssert.assertThat(trait, notNullValue());
        trait.pushEnergy();
        MatcherAssert.assertThat(FEStorage.getEnergyStored(), is(999));
        MatcherAssert.assertThat(container.getEnergyStored(), is(31L));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(124));

        // Another push should not send anything
        trait.pushEnergy();
        MatcherAssert.assertThat(FEStorage.getEnergyStored(), is(999));
        MatcherAssert.assertThat(container.getEnergyStored(), is(31L));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(124));

        // Remove a little bit of energy
        FEStorage.extractEnergy(3, false);
        MatcherAssert.assertThat(FEStorage.getEnergyStored(), is(996));

        // Push again
        trait.pushEnergy();
        MatcherAssert.assertThat(FEStorage.getEnergyStored(), is(1000));
        MatcherAssert.assertThat(container.getEnergyStored(), is(30L));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(120));
    }

    @Test
    public void Test_Non_Identical_Ratio_Configuration() {
        resetEnergyStorage();
        converter_1A.setFeToEu(true);

        // Change two ratios to different value
        ConfigHolder.compat.energy.feToEuRatio = 4;
        ConfigHolder.compat.energy.euToFeRatio = 1;

        IEnergyStorage storage = converter_1A.getCapability(CapabilityEnergy.ENERGY, EnumFacing.SOUTH);
        IEnergyContainer container = converter_1A.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                EnumFacing.NORTH);
        MatcherAssert.assertThat(storage, notNullValue());
        MatcherAssert.assertThat(container, notNullValue());

        // No changes should be observed
        int accepted = storage.receiveEnergy(128, false);
        MatcherAssert.assertThat(accepted, is(128));
        MatcherAssert.assertThat(storage.getEnergyStored(), is(128));
        MatcherAssert.assertThat(container.getEnergyStored(), is(32L));

        converter_1A.setFeToEu(false);

        // When flipped to EU -> FE, stored EU is expected to be same, while FE is expected to become eq to stored EU
        // according to config value.
        MatcherAssert.assertThat(storage.getEnergyStored(), is(32));
        MatcherAssert.assertThat(container.getEnergyStored(), is(32L));

        ConfigHolder.compat.energy.feToEuRatio = 4;
        ConfigHolder.compat.energy.euToFeRatio = 4;
    }

    private static void resetEnergyStorage() {
        ((ConverterTraitTestWrapper) converter_1A.getCapability(GregtechCapabilities.CAPABILITY_CONVERTER, null))
                .drainStorage();
        FEStorage.extractEnergy(Integer.MAX_VALUE, false);
        EUStorage.removeEnergy(Long.MAX_VALUE);
    }

    private static class ConverterTestWrapper extends MetaTileEntityConverter {

        private static int resourceId = 0;

        public ConverterTestWrapper(int tier, int amps) {
            super(GTUtility.gregtechId("converter_" + resourceId++), tier, amps);
        }

        @Override
        protected ConverterTrait initializeTrait() {
            return new ConverterTraitTestWrapper(this, amps, true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing side) {
            if (side == null) return (T) converterTrait;
            if (isFeToEu()) {
                return (T) (side == getFrontFacing() ? converterTrait.getEnergyEUContainer() :
                        converterTrait.getEnergyFEContainer());
            }
            return (T) (side == getFrontFacing() ? converterTrait.getEnergyFEContainer() :
                    converterTrait.getEnergyEUContainer());
        }

        @Override
        public void setFeToEu(boolean feToEu) {
            converterTrait.setFeToEu(feToEu);
        }
    }

    private static final IEnergyStorage FEStorage = new IEnergyStorage() {

        private final int energyCapacity = 1000;
        private int energyStored;

        @Override
        public int receiveEnergy(int i, boolean b) {
            int canReceive = Math.min(energyCapacity - energyStored, i);
            if (!b) energyStored += canReceive;
            return canReceive;
        }

        @Override
        public int extractEnergy(int i, boolean b) {
            int canExtract = Math.min(energyStored, i);
            if (!b) energyStored -= canExtract;
            return canExtract;
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getMaxEnergyStored() {
            return energyCapacity;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    private static final IEnergyContainer EUStorage = new IEnergyContainer() {

        private final long energyCapacity = 1024;
        private long energyStored;

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            return addEnergy(voltage * amperage) / voltage;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return true;
        }

        @Override
        public long changeEnergy(long differenceAmount) {
            MatcherAssert.assertThat(differenceAmount >= 0, is(true));
            return addEnergy(differenceAmount);
        }

        @Override
        public long addEnergy(long energyToAdd) {
            long canReceive = Math.min(energyCapacity - energyStored, energyToAdd);
            energyStored += canReceive;
            return canReceive;
        }

        @Override
        public long removeEnergy(long energyToRemove) {
            long canExtract = Math.min(energyStored, energyToRemove);
            energyStored -= canExtract;
            return canExtract;
        }

        @Override
        public long getEnergyStored() {
            return energyStored;
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity;
        }

        @Override
        public long getInputAmperage() {
            return 1;
        }

        @Override
        public long getInputVoltage() {
            return GTValues.V[GTValues.LV];
        }
    };

    private static class ConverterTraitTestWrapper extends ConverterTrait {

        public ConverterTraitTestWrapper(MetaTileEntityConverter mte, int amps, boolean feToEu) {
            super(mte, amps, feToEu);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <T> T getCapabilityAtFront(Capability<T> capability) {
            return (T) (isFeToEu() ? EUStorage : FEStorage);
        }

        public void drainStorage() {
            storedEU = 0;
        }
    }
}
