package gregtech.api.capability.impl;

import gregtech.Bootstrap;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class ItemHandlerListTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void testListOperations() {
        var list = new ItemHandlerList();
        ItemStackHandler firstHandler = new ItemStackHandler(16);
        ItemStackHandler secondHandler = new ItemStackHandler(16);

        // test add
        list.add(firstHandler);
        MatcherAssert.assertThat("wrong number of slots!", list.getSlots() == 16);
        MatcherAssert.assertThat("wrong number of handlers!", list.size() == 1);
        list.add(secondHandler);
        MatcherAssert.assertThat("wrong number of slots!", list.getSlots() == 32);
        MatcherAssert.assertThat("wrong number of handlers!", list.size() == 2);

        // test removal
        IItemHandler removed = list.remove(0);
        MatcherAssert.assertThat("wrong number of slots!", list.getSlots() == 16);
        MatcherAssert.assertThat("wrong number of handlers!", list.size() == 1);
        MatcherAssert.assertThat("removed handler is not the first handler!", Objects.equals(removed, firstHandler));
        int newIndex = list.getIndexOffset(secondHandler);
        MatcherAssert.assertThat("second handler was not updated!", newIndex == 0);
    }
}
