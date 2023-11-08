package gregtech.common.metatileentities.storage;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gregtech.api.util.GTStringUtils.itemStackToString;
import static gregtech.api.util.GTTransferUtils.insertItem;
import static gregtech.api.util.GTUtility.gregtechId;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

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
    public void Test_Insertion() {
        for (var quantumChest : createInstances()) {
            IItemHandler itemInventory = quantumChest.getItemInventory();
            IItemHandlerModifiable exportItems = quantumChest.getExportItems();
            IItemHandlerModifiable importItems = quantumChest.getImportItems();

            ItemStack stack = itemInventory.insertItem(0, GRAVEL.copy(), true);
            assertThat(String.format("%s should be Empty!", itemStackToString(stack)), stack.isEmpty());

            itemInventory.insertItem(0, GRAVEL.copy(), false);
            assertThat(exportItems.getStackInSlot(0).getCount(), is(64));

            stack = itemInventory.insertItem(0, GRAVEL.copy(), true);
            assertThat(String.format("%s should be Empty!", itemStackToString(stack)), stack.isEmpty());

            itemInventory.insertItem(0, GRAVEL.copy(), false);
            assertThat(itemInventory.getStackInSlot(0).getCount(), is(64));

            stack = importItems.insertItem(0, SAND.copy(), true);
            assertThat(String.format("%s should not be Empty!", itemStackToString(stack)), !stack.isEmpty());

            stack = importItems.insertItem(0, SAND.copy(), false);
            assertThat(String.format("%s should be Sand!", itemStackToString(stack)), !stack.isEmpty());

            importItems.insertItem(0, GRAVEL.copy(), false);
            quantumChest.fakeUpdate(false);
            stack = importItems.getStackInSlot(0);
            assertThat(String.format("%s should be Empty!", itemStackToString(stack)), stack.isEmpty());

            stack = itemInventory.getStackInSlot(0);
            assertThat(stack.getCount(), is(128));
        }
    }

    @Test
    public void Test_Overflow() {
        for (var quantumChest : createInstances()) {
            ItemStack stack = GRAVEL.copy();
            stack.setCount(Integer.MAX_VALUE);

            insertItem(quantumChest.getItemInventory(), stack, false);

            // UHV qchest stores exactly Integer.MAX_VALUE, so it will be 64 items less than expected
            long transferred = quantumChest.getTier() == GTValues.UHV ? quantumChest.itemsStoredInside + 64 : quantumChest.itemsStoredInside;

            assertThat(String.format("%s voided %s too early!", quantumChest.getMetaFullName(), stack), transferred == quantumChest.maxStoredItems);

            quantumChest.setVoiding(true);
            ItemStack remainder = insertItem(quantumChest.getItemInventory(), stack, false);
            assertThat(String.format("%s was not voided!", remainder), remainder.isEmpty());
        }
    }

    @Test
    public void Test_Extraction() {
        for (var quantumChest : createInstances()) {
            insertItem(quantumChest.getItemInventory(), GTUtility.copy(256, GRAVEL), false);

            ItemStack extracted = quantumChest.getExportItems().extractItem(0, 64, true);
            assertThat(String.format("%s did not extract properly!", quantumChest.getMetaFullName()), !extracted.isEmpty() && extracted.getCount() > 0);

            quantumChest.getExportItems().extractItem(0, 64, false);
            quantumChest.fakeUpdate(false);
            ItemStack virtualized = quantumChest.getItemInventory().getStackInSlot(0);
            assertThat(String.format("%s did not extract properly!", quantumChest.getMetaFullName()), virtualized.getCount() == 128);

            extracted = quantumChest.getItemInventory().extractItem(0, Integer.MAX_VALUE, false);
            int amountInExport = quantumChest.getExportItems().extractItem(0, Integer.MAX_VALUE, false).getCount();
            extracted.grow(amountInExport);

            assertThat(String.format("%s extracted too much!", quantumChest.getMetaFullName()), extracted.getCount() > 128);
            assertThat(String.format("%s extracted too little!", quantumChest.getMetaFullName()), quantumChest.itemsStoredInside == 0);
        }
    }

    private static QuantumChestWrapper[] createInstances() {
        QuantumChestWrapper[] quantumChests = new QuantumChestWrapper[10];
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            quantumChests[i] = new QuantumChestWrapper(gregtechId("super_chest." + voltageName), i + 1, 4000000L * (int) Math.pow(2, i));
        }

        for (int i = 5; i < quantumChests.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            long capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000L * (int) Math.pow(2, i);
            quantumChests[i] = new QuantumChestWrapper(gregtechId("quantum_chest." + voltageName), i, capacity);
        }
        return quantumChests;
    }

    private static class QuantumChestWrapper extends MetaTileEntityQuantumChest {
        public QuantumChestWrapper(ResourceLocation metaTileEntityId, int tier, long maxStoredItems) {
            super(metaTileEntityId, tier, maxStoredItems);
        }

        public void fakeUpdate(boolean isRemote) {
            if (!isRemote) {
                if (itemsStoredInside < maxStoredItems) {
                    ItemStack inputStack = importItems.getStackInSlot(0);
                    ItemStack outputStack = exportItems.getStackInSlot(0);
                    if (outputStack.isEmpty() || outputStack.isItemEqual(inputStack) && ItemStack.areItemStackTagsEqual(inputStack, outputStack)) {
                        if (!inputStack.isEmpty() && (virtualItemStack.isEmpty() || areItemStackIdentical(virtualItemStack, inputStack))) {
                            int amountOfItemsToInsert = (int) Math.min(inputStack.getCount(), maxStoredItems - itemsStoredInside);
                            if (this.itemsStoredInside == 0L || virtualItemStack.isEmpty()) {
                                this.virtualItemStack = GTUtility.copy(1, inputStack);
                            }
                            inputStack.shrink(amountOfItemsToInsert);
                            importItems.setStackInSlot(0, inputStack);
                            this.itemsStoredInside += amountOfItemsToInsert;

                            markDirty();
                        }
                    }
                }
                if (itemsStoredInside > 0 && !virtualItemStack.isEmpty()) {
                    ItemStack outputStack = exportItems.getStackInSlot(0);
                    int maxStackSize = virtualItemStack.getMaxStackSize();
                    if (outputStack.isEmpty() || (areItemStackIdentical(virtualItemStack, outputStack) && outputStack.getCount() < maxStackSize)) {
                        int amountOfItemsToRemove = (int) Math.min(maxStackSize - outputStack.getCount(), itemsStoredInside);
                        if (outputStack.isEmpty()) {
                            outputStack = GTUtility.copy(amountOfItemsToRemove, virtualItemStack);
                        } else outputStack.grow(amountOfItemsToRemove);
                        exportItems.setStackInSlot(0, outputStack);
                        this.itemsStoredInside -= amountOfItemsToRemove;
                        if (this.itemsStoredInside == 0) {
                            this.virtualItemStack = ItemStack.EMPTY;
                        }
                        markDirty();
                    }
                }

                if (voiding && !importItems.getStackInSlot(0).isEmpty()) {
                    importItems.setStackInSlot(0, ItemStack.EMPTY);
                }

                if (isAutoOutputItems()) {
                    pushItemsIntoNearbyHandlers(getOutputFacing());
                }

                if (previousStack == null || !areItemStackIdentical(previousStack, virtualItemStack)) {
                    previousStack = virtualItemStack;
                }
                if (previousStackSize != itemsStoredInside) {
                    previousStackSize = itemsStoredInside;
                }
            }
        }

        @Override
        protected void setVoiding(boolean isVoiding) {
            this.voiding = isVoiding;
        }
    }
}
