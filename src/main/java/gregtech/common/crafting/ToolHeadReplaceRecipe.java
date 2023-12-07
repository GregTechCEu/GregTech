package gregtech.common.crafting;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// basically a modified RecipeRepairItem
public class ToolHeadReplaceRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private static final Map<OrePrefix, IGTTool[]> TOOL_HEAD_TO_TOOL_MAP = new HashMap<>();

    public static void setToolHeadForTool(OrePrefix toolHead, IGTTool tool) {
        if (!tool.isElectric()) return;
        TOOL_HEAD_TO_TOOL_MAP.computeIfAbsent(toolHead, p -> new IGTTool[GTValues.MAX])[tool.getElectricTier()] = tool;
    }

    @Override
    public boolean matches(@NotNull InventoryCrafting inv, @NotNull World world) {
        List<ItemStack> list = new ArrayList<>();

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                list.add(stack);
                if (list.size() > 2) {
                    return false;
                }
            }
        }

        if (list.size() == 2) {
            ItemStack stack1 = list.get(0);
            ItemStack stack2 = list.get(1);

            IGTTool tool;
            UnificationEntry toolHead;
            if (stack1.getItem() instanceof IGTTool) {
                tool = (IGTTool) stack1.getItem();
                toolHead = OreDictUnifier.getUnificationEntry(stack2);
            } else if (stack2.getItem() instanceof IGTTool) {
                tool = (IGTTool) stack2.getItem();
                toolHead = OreDictUnifier.getUnificationEntry(stack1);
            } else return false;

            if (!tool.isElectric()) return false;
            if (toolHead == null) return false;
            IGTTool[] output = TOOL_HEAD_TO_TOOL_MAP.get(toolHead.orePrefix);
            return output != null && output[tool.getElectricTier()] != null;
        }
        return false;
    }

    @NotNull
    @Override
    public ItemStack getCraftingResult(@NotNull InventoryCrafting inv) {
        List<ItemStack> list = new ArrayList<>();

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack itemstack = inv.getStackInSlot(i);

            if (!itemstack.isEmpty()) {
                list.add(itemstack);
            }
        }

        if (list.size() == 2) {
            ItemStack first = list.get(0), second = list.get(1);

            IGTTool tool;
            ItemStack toolStack;
            UnificationEntry toolHead;
            if (first.getItem() instanceof IGTTool) {
                tool = (IGTTool) first.getItem();
                toolStack = first;
                toolHead = OreDictUnifier.getUnificationEntry(second);
            } else if (second.getItem() instanceof IGTTool) {
                tool = (IGTTool) second.getItem();
                toolStack = second;
                toolHead = OreDictUnifier.getUnificationEntry(first);
            } else return ItemStack.EMPTY;

            if (!tool.isElectric()) return ItemStack.EMPTY;
            if (toolHead == null) return ItemStack.EMPTY;
            IGTTool[] toolArray = TOOL_HEAD_TO_TOOL_MAP.get(toolHead.orePrefix);
            IGTTool newTool = toolArray[tool.getElectricTier()];
            if (newTool == null) return ItemStack.EMPTY;

            return newTool.get(toolHead.material,
                    tool.getCharge(toolStack), tool.getMaxCharge(toolStack));
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
}
