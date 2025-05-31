package gregtech.api.capability.impl;

import gregtech.Bootstrap;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat("wrong number of slots!", list.getSlots() == 16);
        assertThat("wrong number of handlers!", list.size() == 1);
        list.add(secondHandler);
        assertThat("wrong number of slots!", list.getSlots() == 32);
        assertThat("wrong number of handlers!", list.size() == 2);

        // test removal
        IItemHandler removed = list.remove(0);
        assertThat("wrong number of slots!", list.getSlots() == 16);
        assertThat("wrong number of handlers!", list.size() == 1);
        assertThat("removed handler is not the first handler!", Objects.equals(removed, firstHandler));
        int newIndex = list.getIndexOffset(secondHandler);
        assertThat("second handler was not updated!", newIndex == 0);

        IItemHandler get = list.get(0);
        assertThat("Second handler is not first!", get == secondHandler);

        // test add after removal
        list.add(firstHandler);

        get = list.get(1);
        assertThat("First handler is not second!", get == firstHandler);
        assertThat("wrong number of slots!", list.getSlots() == 32);
        assertThat("wrong number of handlers!", list.size() == 2);

        // test immutable
        ItemHandlerList immutable = list.toImmutable();

        boolean testRemove = false;
        try {
            immutable.remove(0);
        } catch (UnsupportedOperationException ignored) {
            testRemove = true;
        }
        assertThat("list was modified!", testRemove);

        boolean testAdd = false;
        try {
            immutable.add(firstHandler);
        } catch (UnsupportedOperationException ignored) {
            testAdd = true;
        }
        assertThat("list was modified!", testAdd);
    }
}
