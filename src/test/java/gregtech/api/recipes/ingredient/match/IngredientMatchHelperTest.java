package gregtech.api.recipes.ingredient.match;

import gregtech.Bootstrap;
import gregtech.api.recipes.ingredients.match.IngredientMatchHelper;
import gregtech.api.recipes.ingredients.match.Matcher;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class IngredientMatchHelperTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void test() {
        List<ItemStack> stacks = new ObjectArrayList<>();
        List<Matcher<ItemStack>> matchers = new ObjectArrayList<>();
        assert Blocks.LOG != null;
        ItemStack wood = new ItemStack(Blocks.LOG, 1);
        assert Blocks.WOOL != null;
        ItemStack wool = new ItemStack(Blocks.WOOL, 5);
        assert Blocks.STONE_BUTTON != null;
        ItemStack nbt_button = new ItemStack(Blocks.STONE_BUTTON, 64);
        nbt_button.setTagCompound(new NBTTagCompound());
        assert nbt_button.getTagCompound() != null;
        nbt_button.getTagCompound().setBoolean("button", true);
        stacks.add(wood);
        stacks.add(wool);
        stacks.add(nbt_button);

        Matcher<ItemStack> woodM = Matcher.simpleMatcher(i -> i.isItemEqual(wood), 10);
        Matcher<ItemStack> woolM = Matcher.simpleMatcher(i -> i.isItemEqual(wool), 5);
        Matcher<ItemStack> nbt_buttonM = Matcher.simpleMatcher(i -> ItemStack.areItemStacksEqual(i, nbt_button), 2);
        matchers.add(woodM);
        matchers.add(woolM);
        matchers.add(nbt_buttonM);
        // should fail due to not enough logs
        assert IngredientMatchHelper.matchItems(matchers, stacks).getMatchResultsForScale(1) == null;

        wood.setCount(15);
        // should succeed with these exact consumptions
        long[] match = IngredientMatchHelper.matchItems(matchers, stacks).getMatchResultsForScale(1);
        assert match != null;
        assert match[0] == 10;
        assert match[1] == 5;
        assert match[2] == 2;
        wool.setCount(3);
        ItemStack wool2 = wool.copy();
        stacks.add(wool2);
        // should succeed with these exact consumptions
        match = IngredientMatchHelper.matchItems(matchers, stacks).getMatchResultsForScale(1);
        assert match != null;
        assert match[0] == 10;
        assert match[1] + match[3] == 5;
        assert match[2] == 2;

        ItemStack button = new ItemStack(Blocks.STONE_BUTTON, 1);
        stacks.add(button);
        nbt_button.setCount(8);
        Matcher<ItemStack> buttomM = Matcher.simpleMatcher(i -> i.isItemEqual(button), 7);
        matchers.add(buttomM);
        // should succeed with these exact consumptions
        match = IngredientMatchHelper.matchItems(matchers, stacks).getMatchResultsForScale(1);
        assert match != null;
        assert match[0] == 10;
        assert match[1] + match[3] == 5;
        assert match[2] == 8;
        assert match[4] == 1;

        nbt_button.setCount(1);
        button.setCount(64);
        // should fail due to not enough nbt buttons
        assert IngredientMatchHelper.matchItems(matchers, stacks).getMatchResultsForScale(1) == null;
    }
}
