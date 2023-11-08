package gregtech.common.metatileentities.storage;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gregtech.api.util.GTStringUtils.itemStackToString;
import static gregtech.api.util.GTUtility.gregtechId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class QuantumChestTest {

    private static final MetaTileEntityQuantumChest[] quantumChests = new MetaTileEntityQuantumChest[10];

    private static ItemStack GRAVEL;

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
        createInstances();
        GRAVEL = new ItemStack(Blocks.GRAVEL);
        GRAVEL.setCount(64);
    }

    @Test
    public void Test_Quantum_Chests_Valid() {
        for (int i = 0; i < quantumChests.length; i++) {
            MatcherAssert.assertThat(quantumChests[i], notNullValue());
        }
    }

    @Test
    public void Test_Quantum_Chest_Insertion() {
        for (var quantumChest : quantumChests) {
            IItemHandler itemInventory = quantumChest.getItemInventory();
            IItemHandlerModifiable exportItems = quantumChest.getExportItems();
            IItemHandlerModifiable importItems = quantumChest.getImportItems();

            ItemStack stack = itemInventory.insertItem(0, GRAVEL.copy(), true);
            MatcherAssert.assertThat(String.format("%s should be Empty!", itemStackToString(stack)), stack.isEmpty());

            itemInventory.insertItem(0, GRAVEL.copy(), false);
            MatcherAssert.assertThat(exportItems.getStackInSlot(0).getCount(), is(64));

            stack = itemInventory.insertItem(0, GRAVEL.copy(), true);
            MatcherAssert.assertThat(String.format("%s should be Empty!", itemStackToString(stack)), stack.isEmpty());

            itemInventory.insertItem(0, GRAVEL.copy(), false);
            MatcherAssert.assertThat(itemInventory.getStackInSlot(0).getCount(), is(64));
        }
    }

    private static void createInstances() {
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            quantumChests[i] = new MetaTileEntityQuantumChest(gregtechId("super_chest." + voltageName), i + 1, 4000000L * (int) Math.pow(2, i));
        }

        for (int i = 5; i < quantumChests.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            long capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000L * (int) Math.pow(2, i);
            quantumChests[i] = new MetaTileEntityQuantumChest(gregtechId("quantum_chest." + voltageName), i, capacity);
        }
    }
}
