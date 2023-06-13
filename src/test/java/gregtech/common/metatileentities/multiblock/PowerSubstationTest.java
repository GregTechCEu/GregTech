package gregtech.common.metatileentities.multiblock;

import gregtech.Bootstrap;
import gregtech.api.metatileentity.multiblock.IBatteryBlockPart;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation.PowerStationEnergyBank;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;

public class PowerSubstationTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void Test_1_Slot() {
        PowerStationEnergyBank storage = createStorage(100);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(100));

        // Random fill and drain tests
        MatcherAssert.assertThat(storage.fill(50), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(50));
        MatcherAssert.assertThat(storage.fill(100), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));

        MatcherAssert.assertThat(storage.drain(50), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(50));
        MatcherAssert.assertThat(storage.drain(100), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Fully fill and drain
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));

        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Try to overfill and overdrain
        MatcherAssert.assertThat(storage.fill(1000), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));

        MatcherAssert.assertThat(storage.drain(1000), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
    }

    @Test
    public void Test_4_Slot_Equal_Sizes() {
        PowerStationEnergyBank storage = createStorage(100, 100, 100, 100);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(400));

        // No overlap of slots
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Overlap slots
        MatcherAssert.assertThat(storage.fill(150), is(150L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(150));
        MatcherAssert.assertThat(storage.fill(50), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.fill(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(150), is(150L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(250));
        MatcherAssert.assertThat(storage.drain(50), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.drain(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Fully fill and drain
        MatcherAssert.assertThat(storage.fill(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));
        MatcherAssert.assertThat(storage.fill(400), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(400), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Try to overfill and overdrain
        MatcherAssert.assertThat(storage.fill(1000), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(1000), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
    }

    @Test
    public void Test_4_Slot_Different_Sizes() {
        PowerStationEnergyBank storage = createStorage(100, 200, 300, 400);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(1000));

        // No overlap of slots
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.fill(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.fill(300), is(300L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(600));
        MatcherAssert.assertThat(storage.fill(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(600));
        MatcherAssert.assertThat(storage.drain(300), is(300L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.drain(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Overlap slots
        MatcherAssert.assertThat(storage.fill(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.fill(600), is(600L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(900));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(900));
        MatcherAssert.assertThat(storage.drain(600), is(600L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.drain(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Fully fill and drain
        MatcherAssert.assertThat(storage.fill(1000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));
        MatcherAssert.assertThat(storage.fill(1000), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(1000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(1000), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Try to overfill and overdrain
        MatcherAssert.assertThat(storage.fill(10000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(10000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
    }

    @SuppressWarnings("NumericOverflow")
    @Test
    public void Test_Over_Long() {
        PowerStationEnergyBank storage = createStorage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));

        long halfLong = Long.MAX_VALUE / 2;

        MatcherAssert.assertThat(storage.fill(halfLong), is(halfLong));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(halfLong));
        MatcherAssert.assertThat(storage.fill(Long.MAX_VALUE), is(Long.MAX_VALUE));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(halfLong, Long.MAX_VALUE));

        MatcherAssert.assertThat(storage.drain(halfLong), is(halfLong));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(Long.MAX_VALUE));
        MatcherAssert.assertThat(storage.drain(Long.MAX_VALUE), is(Long.MAX_VALUE));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Test overflow
        Assertions.assertThrows(IllegalArgumentException.class, () -> storage.fill(Long.MAX_VALUE + 1000));
        Assertions.assertThrows(IllegalArgumentException.class, () -> storage.drain(Long.MAX_VALUE + 1000));
    }

    @Test
    public void Test_Rebuild_Storage() {
        PowerStationEnergyBank storage = createStorage(100, 500, 4000);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(4600));

        // Set up the storage with some amount of energy
        MatcherAssert.assertThat(storage.fill(3000), is(3000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(3000));

        // Rebuild with more storage than needed
        storage = rebuildStorage(storage, 1000, 4000, 4000);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(9000));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(3000));

        // Reset
        storage = createStorage(100, 500, 4000);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(4600));

        // Set up storage with energy again
        MatcherAssert.assertThat(storage.fill(3000), is(3000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(3000));

        // Rebuild with less storage than needed
        storage = rebuildStorage(storage, 100, 100, 400, 500);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(1100));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1100));
    }

    private static Matcher<BigInteger> isBigInt(long value, long... additional) {
        BigInteger retVal = BigInteger.valueOf(value);
        if (additional != null) {
            for (long l : additional) {
                retVal = retVal.add(BigInteger.valueOf(l));
            }
        }
        return is(retVal);
    }

    private static PowerStationEnergyBank createStorage(long... storageValues) {
        List<IBatteryBlockPart> batteries = new ArrayList<>();
        for (long value : storageValues) {
            batteries.add(new TestBattery(value));
        }
        return new PowerStationEnergyBank(batteries);
    }

    private static PowerStationEnergyBank rebuildStorage(PowerStationEnergyBank storage, long... storageValues) {
        List<IBatteryBlockPart> batteries = new ArrayList<>();
        for (long value : storageValues) {
            batteries.add(new TestBattery(value));
        }
        return storage.rebuild(batteries);
    }

    private static class TestBattery implements IBatteryBlockPart {

        private final long capacity;

        private TestBattery(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        // not used in this test
        @Override
        public int getTier() {
            return 0;
        }

        // not used in this test
        @Nonnull
        @Override
        public String getName() {
            return "";
        }
    }
}
