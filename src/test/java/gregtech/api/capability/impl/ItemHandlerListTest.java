package gregtech.api.capability.impl;

import gregtech.Bootstrap;

import net.minecraftforge.items.ItemStackHandler;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class ItemHandlerListTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void testListOperations() {
        var list = new ItemHandlerList(Collections.emptyList());

        // test add
        list.add(new ItemStackHandler(16));
//        MatcherAssert.assertThat("size is wrong", list.getSlots() == 16);
        list.add(new ItemStackHandler(16));
//        MatcherAssert.assertThat("size is wrong", list.getSlots() == 32);
//        MatcherAssert.assertThat("size is wrong", list.size() == 2);

        // test removal
        list.remove(0);
//        MatcherAssert.assertThat("size is wrong", list.getSlots() == 16);
//        MatcherAssert.assertThat("size is wrong", list.size() == 1);
    }
}
