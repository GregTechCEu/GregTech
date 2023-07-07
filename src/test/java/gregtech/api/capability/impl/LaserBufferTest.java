package gregtech.api.capability.impl;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

class LaserBufferTest {

    @Test
    void changeEnergy() {
        LaserBuffer buffer = new LaserBuffer(1024L);
        MatcherAssert.assertThat(buffer.getEnergyCapacity(), is(1024L));

        MatcherAssert.assertThat(buffer.changeEnergy(128), is(128L));
        MatcherAssert.assertThat(buffer.getEnergyStored(), is(128L));

        MatcherAssert.assertThat(buffer.changeEnergy(-64), is(-64L));
        MatcherAssert.assertThat(buffer.getEnergyStored(), is(64L));

        MatcherAssert.assertThat(buffer.changeEnergy(-16384), is(-64L));
        MatcherAssert.assertThat(buffer.getEnergyStored(), is(0L));

        MatcherAssert.assertThat(buffer.changeEnergy(2048), is(1024L));
        MatcherAssert.assertThat(buffer.getEnergyStored(), is(1024L));
    }
}
