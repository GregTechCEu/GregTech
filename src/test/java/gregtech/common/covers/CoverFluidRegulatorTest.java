package gregtech.common.covers;

import gregtech.Bootstrap;
import gregtech.api.capability.impl.*;
import net.minecraft.util.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.*;
import org.junit.*;

import java.util.function.*;

import static org.junit.Assert.*;

public class CoverFluidRegulatorTest {
    public static final Predicate<FluidStack> isWater = fs -> fs.getFluid() == FluidRegistry.WATER;

    /**
     * Required. Without this all item-related operations will fail because registries haven't been initialized.
     */
    @BeforeClass
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void doKeepExact_does_nothing_if_no_destination_tank_exists() {

        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        FluidStack water = new FluidStack(FluidRegistry.WATER, 1234);

        // Source consists of only an output tank containing a bit of water
        IFluidHandler source =
            new FluidHandlerProxy(new FluidTankList(false),
                                  new FluidTankList(false, new FluidTank(water.copy(), 64000)));

        // Tell it to keep exact from a machine with an empty fluid tank and null target fluid tank
        int amountTransferred = cfr.doKeepExact(1000, source, null, isWater, 1000);

        assertEquals("Unexpectedly moved fluids, nothing is supposed to happen", 0, amountTransferred);
    }

    @Test
    public void doKeepExact_moves_one_fluid_into_an_empty_tank() {

        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        FluidStack water = new FluidStack(FluidRegistry.WATER, 1234);

        IFluidHandler source =
            new FluidHandlerProxy(new FluidTankList(false),
                                  new FluidTankList(false, new FluidTank(water.copy(), 64000)));

        // Dest consists of one empty input tank
        IFluidHandler dest =
            new FluidHandlerProxy(new FluidTankList(false, new FluidTank(64000)),
                                  new FluidTankList(false));

        // Tell it to keep exact from a machine with an empty fluid tank and no target fluid tank
        int amountTransferred = cfr.doKeepExact(1000, source, dest, isWater, 1000);

        assertEquals("Wrong fluid amount moved", 1000, amountTransferred);
    }

    @Test
    public void doKeepExact_moves_only_as_much_fluid_as_exists_in_the_source() {

        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source =
            new FluidHandlerProxy(new FluidTankList(false),
                                  new FluidTankList(false,
                                                    new FluidTank(new FluidStack(FluidRegistry.WATER, 1234), 64000)));

        IFluidHandler dest =
            new FluidHandlerProxy(new FluidTankList(false, new FluidTank(64000)),
                                  new FluidTankList(false));

        int amountTransferred = cfr.doKeepExact(10000, source, dest, isWater, 10000);

        assertEquals("Wrong fluid amount moved", 1234, amountTransferred);
    }

    @Test
    public void doKeepExact_moves_only_the_fluid_required_if_more_could_be_moved() {

        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source =
            new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000)));

        IFluidHandler dest =
            new FluidHandlerProxy(
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 100), 64000)),
                new FluidTankList(false));

        int amountTransferred = cfr.doKeepExact(10000, source, dest, isWater, 144);

        assertEquals("Wrong fluid amount moved", 44, amountTransferred);
    }

    @Test
    public void doKeepExact_moves_multiple_valid_fluids() {

        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source =
            new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                                  new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 100mB water, another with nothing
        IFluidHandler dest =
            new FluidHandlerProxy(
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 100), 64000),
                                  new FluidTank(64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(10000, source, dest, fs -> true, 144);

        // expect that 44mB of water and 144mB of lava will be moved
        assertEquals("Wrong fluid amount moved", 44+144, amountTransferred);

        // verify final fluid quantities
        assertEquals(2, dest.getTankProperties().length);
        IFluidTankProperties tank1 = dest.getTankProperties()[0];
        IFluidTankProperties tank2 = dest.getTankProperties()[1];
        assertNotNull(tank1.getContents());
        assertNotNull(tank2.getContents());
        assertTrue(tank1.getContents().isFluidStackIdentical(new FluidStack(FluidRegistry.WATER, 144)));
        assertTrue(tank2.getContents().isFluidStackIdentical(new FluidStack(FluidRegistry.LAVA, 144)));
    }

    @Test
    public void doKeepExact_respects_transfer_limit_with_one_fluid() {

        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        // One output tank full of water
        IFluidHandler source =
            new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000)));

        // One input tank with nothing in it
        IFluidHandler dest =
            new FluidHandlerProxy(
                new FluidTankList(false, new FluidTank(64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(100, source, dest, fs -> true, 144);

        // expect that at most 100mB of fluids total will be moved this tick, as if possible it would do 144mB
        assertEquals("Wrong fluid amount moved", 100, amountTransferred);
    }

    @Test
    public void doKeepExact_respects_transfer_limit_with_multiple_fluids() {

        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source =
            new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                                  new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 100mB water, another with nothing
        IFluidHandler dest =
            new FluidHandlerProxy(
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 100), 64000),
                                  new FluidTank(64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(100, source, dest, fs -> true, 144);

        // expect that at most 100mB of fluids total will be moved this tick, as if possible it would do 188mB
        assertEquals("Wrong fluid amount moved", 100, amountTransferred);
    }

    @Test
    public void doKeepExact_does_nothing_if_levels_are_already_correct_in_dest() {

        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source =
            new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                                  new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 144mB water, another with 144mB lava
        IFluidHandler dest =
            new FluidHandlerProxy(
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 144), 64000),
                                  new FluidTank(new FluidStack(FluidRegistry.LAVA, 144), 64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(10000, source, dest, fs -> true, 144);

        // expect that no fluids are moved because Keep Exact levels are already met
        assertEquals("Wrong fluid amount moved", 0, amountTransferred);
    }

    @Test
    public void doKeepExact_ignores_fluids_not_in_filter() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source =
            new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                                  new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 144mB water, another with 100mB lava
        IFluidHandler dest =
            new FluidHandlerProxy(
                new FluidTankList(false,
                                  new FluidTank(new FluidStack(FluidRegistry.WATER, 144), 64000),
                                  new FluidTank(new FluidStack(FluidRegistry.LAVA, 100), 64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(10000, source, dest, isWater, 144);

        // expect that no fluids are moved because already have enough water and lava isn't in the filter
        assertEquals("Wrong fluid amount moved", 0, amountTransferred);
    }
}
