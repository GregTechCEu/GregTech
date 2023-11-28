package gregtech.common.metatileentities.storage;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import gregtech.api.util.world.DummyWorld;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gregtech.api.util.GTStringUtils.itemStackToString;
import static gregtech.api.util.GTTransferUtils.insertItem;
import static gregtech.api.util.GTUtility.gregtechId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class QuantumChestTest {

    private static ItemStack GRAVEL;
    private static ItemStack SAND;

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
        GRAVEL = new ItemStack(Blocks.GRAVEL);
        GRAVEL.setCount(64);
        SAND = new ItemStack(Blocks.SAND);
        SAND.setCount(64);
    }

    @Test
    public void Test_Valid() {
        for (var quantumChest : createInstances()) {
            assertThat(quantumChest, notNullValue());
        }
    }

    @Test
    public void Test_Single_Insertion() {
        for (var quantumChest : createInstances()) {
            IItemHandler combinedInventory = quantumChest.getCombinedInventory();
            IItemHandler virtualInventory = quantumChest.getItemInventory();
            IItemHandlerModifiable exportItems = quantumChest.getExportItems();
            IItemHandlerModifiable importItems = quantumChest.getImportItems();

            ItemStack stack = insertItem(combinedInventory, GRAVEL.copy(), true);
            String reason = String.format("%s should be Empty!", itemStackToString(stack));
            assertThat(reason, stack.isEmpty());

            insertItem(combinedInventory, GRAVEL.copy(), false);
            int expected = 64;
            stack = exportItems.getStackInSlot(0);
            reason = String.format("Got %d in the export slot when it should be %d",
                    exportItems.getStackInSlot(0).getCount(), expected);
            assertThat(reason, stack.getCount(), is(expected));

            stack = insertItem(combinedInventory, GRAVEL.copy(), true);
            reason = String.format("%s should be Empty!", itemStackToString(stack));
            assertThat(reason, stack.isEmpty());

            insertItem(combinedInventory, GRAVEL.copy(), false);
            stack = virtualInventory.getStackInSlot(0);
            reason = String.format("Got %d in the virtualized inventory when it should be %d", stack.getCount(),
                    expected);
            assertThat(reason, stack.getCount(), is(expected));

            stack = importItems.insertItem(0, SAND.copy(), true);
            reason = String.format("%s should not be Empty!", itemStackToString(stack));
            assertThat(reason, !stack.isEmpty());

            stack = importItems.insertItem(0, SAND.copy(), false);
            reason = String.format("%s should be Sand!", itemStackToString(stack));
            assertThat(reason, !stack.isEmpty() && stack.isItemEqual(SAND));

            stack = virtualInventory.getStackInSlot(0);
            reason = String.format("Got %d in the virtualized inventory when it should be %d", stack.getCount(),
                    expected);
            assertThat(reason, stack.getCount(), is(expected));
        }
    }

    @Test
    public void Test_Stack_In_Slot() {
        for (var quantumChest : createInstances()) {
            IItemHandler itemInventory = quantumChest.getItemInventory();
            int expected = 256;

            insertItem(quantumChest.getCombinedInventory(), GTUtility.copy(expected, SAND), false);

            expected = 64;
            int exportCount = quantumChest.getExportItems().getStackInSlot(0).getCount();
            String reason = String.format(
                    "The combined count using the chest's handler and the export slot got %d, should've been %d",
                    exportCount, expected);
            assertThat(reason, exportCount, is(expected));

            expected = 192;
            int virtualizedAmount = itemInventory.getStackInSlot(0).getCount();
            reason = String.format("The virtualized amount in the chest's handler got %d, should've been %d",
                    exportCount, expected);
            assertThat(reason, virtualizedAmount, is(expected));
        }
    }

    @Test
    public void Test_Export_Checking() {
        for (var quantumChest : createInstances()) {
            IItemHandler itemHandler = quantumChest.getCombinedInventory();
            insertItem(itemHandler, GRAVEL.copy(), false);

            ItemStack export = quantumChest.getExportItems().getStackInSlot(0);

            insertItem(itemHandler, SAND.copy(), false);
            insertItem(itemHandler, SAND.copy(), false);

            insertItem(itemHandler, GRAVEL.copy(), false);

            String reason = "The virtualized stack is not the same as the export slot!";
            boolean isEqual = ItemStack.areItemsEqual(export, quantumChest.virtualItemStack) &&
                    export.getMetadata() == quantumChest.virtualItemStack.getMetadata();
            assertThat(reason, isEqual, is(true));
        }
    }

    @Test
    public void Test_Multiple_Insertions() {
        for (var quantumChest : createInstances()) {
            IItemHandler itemInventory = quantumChest.getItemInventory();
            IItemHandlerModifiable exportItems = quantumChest.getExportItems();
            IItemHandlerModifiable importItems = quantumChest.getImportItems();

            for (int i = 0; i < 16; i++) {
                importItems.insertItem(0, GRAVEL.copy(), false);
                quantumChest.update();
            }

            ItemStack virtualized = itemInventory.getStackInSlot(0);
            ItemStack export = exportItems.getStackInSlot(0);

            assertThat("Virtualized amount is wrong!", virtualized.getCount(), is(64 * 15));
            assertThat("Export slot is empty!", export.getCount() == 64 && !export.isEmpty());
            assertThat("Import slot has an item in it!", importItems.getStackInSlot(0).isEmpty());
        }
    }

    @Test
    public void Test_Insertion_And_Update() {
        for (var quantumChest : createInstances()) {
            IItemHandlerModifiable exportItems = quantumChest.getExportItems();
            IItemHandlerModifiable importItems = quantumChest.getImportItems();

            importItems.insertItem(0, GRAVEL.copy(), false);
            quantumChest.update();
            ItemStack stack = importItems.getStackInSlot(0);

            assertThat(String.format("%s should be Empty!", itemStackToString(stack)), stack.isEmpty());
            assertThat("Virtual stack should be empty!", quantumChest.virtualItemStack.isEmpty());
            assertThat("Export slot was not filled!", !exportItems.getStackInSlot(0).isEmpty());

            importItems.insertItem(0, GRAVEL.copy(), false);
            quantumChest.update();
            assertThat("Virtual stack should not be empty!", !quantumChest.virtualItemStack.isEmpty());
        }
    }

    @Test
    public void Test_Insertion_Near_Full() {
        for (var quantumChest : createInstances()) {
            if (quantumChest.getTier() >= GTValues.UHV) continue; // UHV can't be tested due to int overflow :floppaxd:
            IItemHandler combinedInventory = quantumChest.getCombinedInventory();
            int toInsert = quantumChest.getItemInventory().getSlotLimit(0) + 32;
            insertItem(combinedInventory, GTUtility.copy(toInsert, SAND), false);

            int remainder = insertItem(quantumChest.getImportItems(), SAND.copy(), true).getCount();
            quantumChest.update();
            String reason = String.format("Remainder should be exactly %d, but was %d!", 32, remainder);
            assertThat(reason, remainder, is(32));
        }
    }

    @Test
    public void Test_Voiding() {
        for (var quantumChest : createInstances()) {
            ItemStack stack = GRAVEL.copy();
            stack.setCount(Integer.MAX_VALUE);

            insertItem(quantumChest.getCombinedInventory(), stack, false);

            // UHV qchest stores exactly Integer.MAX_VALUE, so it will be 64 items less than expected
            long transferred = quantumChest.getTier() == GTValues.UHV ? quantumChest.itemsStoredInside + 64 :
                    quantumChest.itemsStoredInside;

            assertThat(String.format("%s voided %s too early!", quantumChest.getMetaFullName(), stack),
                    transferred == quantumChest.maxStoredItems);

            quantumChest.setVoiding(true);
            ItemStack remainder = insertItem(quantumChest.getItemInventory(), stack, false);
            assertThat(String.format("%s was not voided!", remainder), remainder.isEmpty());

            stack = SAND.copy();
            stack.setCount(Integer.MAX_VALUE);
            remainder = insertItem(quantumChest.getItemInventory(), stack, false);
            assertThat("Quantum Chest voided the wrong item!", remainder.getCount(), is(stack.getCount()));
        }
    }

    @Test
    public void Test_Extraction() {
        for (var quantumChest : createInstances()) {
            insertItem(quantumChest.getCombinedInventory(), GTUtility.copy(256, GRAVEL), false);

            int extractedCount = testAllSlots(quantumChest.getExportItems(), true);
            int expected = 64;

            String reason = String.format("Quantum chest failed to insert %d items into export slot, actually was %d!",
                    expected, extractedCount);
            assertThat(reason, extractedCount, is(expected));

            quantumChest.getExportItems().extractItem(0, 64, false);
            quantumChest.update();
            extractedCount = quantumChest.getItemInventory().getStackInSlot(0).getCount();

            expected = 128;
            reason = String.format("Virtualized count is %d, should be %d!", extractedCount, expected);
            assertThat(reason, extractedCount, is(expected));

            expected = 192;
            extractedCount = testAllSlots(quantumChest.getCombinedInventory(), true);

            reason = String.format("Extracted %d items, should've extracted %d!", extractedCount, expected);
            assertThat(reason, extractedCount, is(expected));
        }
    }

    private static QuantumChestWrapper[] createInstances() {
        QuantumChestWrapper[] quantumChests = new QuantumChestWrapper[10];
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            quantumChests[i] = new QuantumChestWrapper(gregtechId("super_chest." + voltageName), i + 1,
                    4000000L * (int) Math.pow(2, i));
        }

        for (int i = 5; i < quantumChests.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            long capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000L * (int) Math.pow(2, i);
            quantumChests[i] = new QuantumChestWrapper(gregtechId("quantum_chest." + voltageName), i, capacity);
        }
        return quantumChests;
    }

    private static int testAllSlots(IItemHandler handler, boolean simulate) {
        int extractedCount = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            extractedCount += handler.extractItem(i, Integer.MAX_VALUE, simulate).getCount();
        }
        return extractedCount;
    }

    private static class QuantumChestWrapper extends MetaTileEntityQuantumChest {

        public QuantumChestWrapper(ResourceLocation metaTileEntityId, int tier, long maxStoredItems) {
            super(metaTileEntityId, tier, maxStoredItems);
        }

        @Override
        protected void setVoiding(boolean isVoiding) {
            this.voiding = isVoiding;
        }

        @Override
        public World getWorld() {
            return DummyWorld.INSTANCE;
        }
    }
}
