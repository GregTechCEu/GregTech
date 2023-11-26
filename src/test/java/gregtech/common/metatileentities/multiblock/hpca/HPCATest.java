package gregtech.common.metatileentities.multiblock.hpca;

import gregtech.Bootstrap;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityHPCA.HPCAGridHandler;
import gregtech.common.metatileentities.multiblock.hpca.helper.HPCAHelper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class HPCATest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void Test_Edge_No_Computation() {
        HPCAGridHandler handler = HPCAHelper.gridBuilder(g -> g
                .numCooler(4)
                .coolerBuilder(c -> c
                        .EUt(32, 128) // different upkeep and max EU/t values
                        .coolingAmount(4)));

        final int maxCWUt = handler.getMaxCWUt();
        final int upkeepEUt = handler.getUpkeepEUt();
        final int maxEUt = handler.getMaxEUt();
        final int maxCoolingDemand = handler.getMaxCoolantDemand();
        final int maxCoolingAmount = handler.getMaxCoolingAmount();
        int allocated;
        double temperatureChange;

        assertThat(maxCWUt, is(0));
        assertThat(upkeepEUt, is(32 * 4));
        assertThat(maxEUt, is(128 * 4));
        assertThat(maxCoolingDemand, is(0));
        assertThat(maxCoolingAmount, is(4 * 4));

        // Test an allocation request
        allocated = handler.allocateCWUt(2, true);
        assertThat(allocated, is(0));
        assertThat(handler.getAllocatedCWUt(), is(0));
        allocated = handler.allocateCWUt(2, false);
        assertThat(allocated, is(0));
        assertThat(handler.getAllocatedCWUt(), is(0));
        assertThat(handler.getCurrentEUt(), is(upkeepEUt)); // NOT max EU/t, as that would need allocated CWU/t
        temperatureChange = handler.calculateTemperatureChange(null, false);
        assertThat(temperatureChange, is((double) -maxCoolingAmount));
    }

    @Test
    public void Test_Edge_Equal_Upkeep_Max_EUt() {
        HPCAGridHandler handler = HPCAHelper.gridBuilder(g -> g
                .numComputation(4)
                .computationBuilder(c -> c
                        .EUt(32, 32) // upkeepEUt and maxEUt are the same
                        .CWUt(4)
                        .coolingPerTick(2))
                .numCooler(4)
                .coolerBuilder(c -> c
                        .EUt(32, 32) // upkeepEUt and maxEUt are the same
                        .coolingAmount(2)));

        final int maxCWUt = handler.getMaxCWUt();
        final int upkeepEUt = handler.getUpkeepEUt();
        final int maxEUt = handler.getMaxEUt();
        final int maxCoolingDemand = handler.getMaxCoolingDemand();
        final int maxCoolingAmount = handler.getMaxCoolingAmount();
        int allocated, requested;
        double temperatureChange;

        final int FIXED_EUT = 32 * 8;
        assertThat(maxCWUt, is(4 * 4));
        assertThat(upkeepEUt, is(FIXED_EUT));
        assertThat(maxEUt, is(FIXED_EUT));
        assertThat(maxCoolingDemand, is(2 * 4));
        assertThat(maxCoolingAmount, is(2 * 4));

        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            requested = Math.max(1, r.nextInt(maxCWUt + 1));
            allocated = handler.allocateCWUt(requested, true);
            assertThat(allocated, is(requested));
            assertThat(handler.getAllocatedCWUt(), is(0));
            allocated = handler.allocateCWUt(requested, false);
            assertThat(allocated, is(requested));
            assertThat(handler.getAllocatedCWUt(), is(allocated));
            assertThat(handler.getCurrentEUt(), is(FIXED_EUT));
            temperatureChange = handler.calculateTemperatureChange(null, false);
            double ratio = 1.0 * allocated / maxCWUt;
            assertThat(temperatureChange, closeTo(maxCoolingDemand * ratio - maxCoolingAmount, 1.0D));
            handler.tick();
        }
    }

    // stress test
    @Test
    public void Test_Random() {
        Consumer<Random> testRunner = r -> {
            HPCAGridHandler handler = HPCAHelper.gridBuilder(g -> g
                    .numComputation(() -> r.nextInt(30))
                    .computationBuilder(c -> c
                            .EUt(() -> r.nextInt(128))
                            .CWUt(() -> r.nextInt(128))
                            .coolingPerTick(() -> r.nextInt(128)))
                    .numCooler(() -> r.nextInt(30))
                    .coolerBuilder(c -> c
                            .EUt(() -> r.nextInt(128))
                            .coolingAmount(() -> r.nextInt(128))));

            final int maxEUt = handler.getMaxEUt();
            final int upkeepEUt = handler.getUpkeepEUt();
            final int maxCWUt = handler.getMaxCWUt();
            final int maxCoolingDemand = handler.getMaxCoolingDemand();
            final int maxCoolingAmount = handler.getMaxCoolingAmount();
            int allocated, requested, currentEUt;
            double temperatureChange;

            // exit, we unit test these edge cases elsewhere
            if (maxCWUt == 0) return;
            if (maxEUt <= upkeepEUt) return;

            // Test an exact max CWUt allocation
            allocated = handler.allocateCWUt(maxCWUt, true);
            assertThat(allocated, is(maxCWUt));
            assertThat(handler.getAllocatedCWUt(), is(0));
            allocated = handler.allocateCWUt(maxCWUt, false);
            assertThat(allocated, is(maxCWUt));
            assertThat(handler.getAllocatedCWUt(), is(maxCWUt));
            assertThat(handler.getCurrentEUt(), is(maxEUt));
            temperatureChange = handler.calculateTemperatureChange(null, false);
            assertThat(temperatureChange, is((double) (maxCoolingDemand - maxCoolingAmount)));
            handler.tick();

            // Test an over-max CWUt allocation
            allocated = handler.allocateCWUt(maxCWUt + 100, true);
            assertThat(allocated, is(maxCWUt));
            assertThat(handler.getAllocatedCWUt(), is(0));
            allocated = handler.allocateCWUt(maxCWUt + 100, false);
            assertThat(allocated, is(maxCWUt));
            assertThat(handler.getAllocatedCWUt(), is(maxCWUt));
            assertThat(handler.getCurrentEUt(), is(maxEUt));
            temperatureChange = handler.calculateTemperatureChange(null, false);
            assertThat(temperatureChange, is((double) (maxCoolingDemand - maxCoolingAmount)));
            handler.tick();

            // Test a bunch of random CWUt allocations
            for (int i = 0; i < 100; i++) {
                requested = r.nextInt(maxCWUt);
                allocated = handler.allocateCWUt(requested, true);
                assertThat(allocated, is(requested));
                assertThat(handler.getAllocatedCWUt(), is(0));
                allocated = handler.allocateCWUt(requested, false);
                assertThat(allocated, is(requested));
                assertThat(handler.getAllocatedCWUt(), is(requested));
                currentEUt = handler.getCurrentEUt();
                assertThat(null, currentEUt > 0 && currentEUt < maxEUt);
                temperatureChange = handler.calculateTemperatureChange(null, false);
                final double ratio = 1.0 * allocated / maxCWUt;
                assertThat(temperatureChange, closeTo(maxCoolingDemand * ratio - maxCoolingAmount, 1.0D));
                handler.tick();
            }
        };
        for (int i = 0; i < 10000; i++) {
            Random r = new Random();
            long seed = r.nextLong();
            r.setSeed(seed);
            testRunner.accept(r);
        }
    }
}
