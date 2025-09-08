package gregtech.common.crafting;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.items.toolitem.IGTTool;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ShapedOreEnergyTransferRecipe extends ShapedOreRecipe {

    private final Predicate<ItemStack> chargePredicate;
    private final boolean transferMaxCharge;

    public ShapedOreEnergyTransferRecipe(ResourceLocation group, @NotNull ItemStack result,
                                         Predicate<ItemStack> chargePredicate, boolean overrideCharge,
                                         boolean transferMaxCharge, Object... recipe) {
        super(group, result, CraftingHelper.parseShaped(recipe));
        this.chargePredicate = chargePredicate;
        this.transferMaxCharge = transferMaxCharge;
        if (overrideCharge) {
            fixOutputItemMaxCharge();
        }
    }

    // transfer initial max charge for correct display in JEI
    private void fixOutputItemMaxCharge() {
        long totalMaxCharge = 0L;
        for (Ingredient ingredient : getIngredients()) {
            long maxCharge = 0L;
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (!(stack.getItem() instanceof IGTTool)) continue;
                IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

                if (electricItem == null) continue;
                maxCharge = Math.max(maxCharge, electricItem.getMaxCharge());
            }
            totalMaxCharge += maxCharge;
        }

        IElectricItem electricItem = output.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (totalMaxCharge > 0L && electricItem instanceof ElectricItem item) {
            item.setMaxChargeOverride(totalMaxCharge);
        }
    }

    @NotNull
    @Override
    public ItemStack getCraftingResult(@NotNull InventoryCrafting inventoryCrafting) {
        ItemStack resultStack = super.getCraftingResult(inventoryCrafting);
        chargeStackFromComponents(resultStack, inventoryCrafting, chargePredicate, transferMaxCharge);
        return resultStack;
    }

    public static void chargeStackFromComponents(ItemStack toolStack, IInventory ingredients,
                                                 Predicate<ItemStack> chargePredicate, boolean transferMaxCharge) {
        IElectricItem electricItem = toolStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        long totalMaxCharge = 0L;
        long toCharge = 0L;
        if (electricItem != null && electricItem.getMaxCharge() > 0L) {
            for (int slotIndex = 0; slotIndex < ingredients.getSizeInventory(); slotIndex++) {
                ItemStack stackInSlot = ingredients.getStackInSlot(slotIndex);
                if (!chargePredicate.test(stackInSlot)) {
                    continue;
                }
                IElectricItem batteryItem = stackInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM,
                        null);
                if (batteryItem == null) {
                    continue;
                }
                totalMaxCharge += batteryItem.getMaxCharge();
                toCharge += batteryItem.discharge(Long.MAX_VALUE, Integer.MAX_VALUE, true, true, true);
            }
        }
        if (electricItem instanceof ElectricItem && transferMaxCharge) {
            ((ElectricItem) electricItem).setMaxChargeOverride(totalMaxCharge);
        }
        // noinspection DataFlowIssue
        electricItem.charge(toCharge, Integer.MAX_VALUE, true, false);
    }
}
