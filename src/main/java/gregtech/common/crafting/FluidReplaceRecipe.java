package gregtech.common.crafting;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.NotNull;

/**
 * A recipe which inputs a single Fluid Container, and outputs the same Fluid Container with a new contained Fluid
 */
public class FluidReplaceRecipe extends GTShapedOreRecipe {

    public FluidReplaceRecipe(boolean isClearing, ResourceLocation group, @NotNull ItemStack result, Object... recipe) {
        super(isClearing, group, result, recipe);
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
        if (isClearing) {
            return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        } else {
            // modified behavior from net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(inv)
            NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
            for (int i = 0; i < ret.size(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                // fluid containers are always consumed
                if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                    ret.set(i, ItemStack.EMPTY);
                } else ret.set(i, ForgeHooks.getContainerItem(stack));
            }
            return ret;
        }
    }

    @NotNull
    @Override
    public ItemStack getCraftingResult(@NotNull InventoryCrafting inv) {
        IFluidHandlerItem recipeCap = output.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (recipeCap == null)
            throw new IllegalStateException("FluidReplaceRecipe output did not have an IFluidHandlerItem capability");

        FluidStack outputFluid = recipeCap.drain(Integer.MAX_VALUE, false);
        if (outputFluid == null) throw new IllegalStateException("FluidReplaceRecipe output did not have a fluid");
        if (outputFluid.amount != 1000)
            throw new IllegalStateException("FluidReplaceRecipe output must have exactly 1000mB of fluid");

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack input = inv.getStackInSlot(i);
            IFluidHandlerItem inputCap = input.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                    null);
            if (inputCap == null) continue;

            // if the input has a fluid, it must only hold 1000mB
            FluidStack inputFluid = inputCap.drain(Integer.MAX_VALUE, false);
            if (inputFluid != null && inputFluid.amount != 1000) continue;

            boolean isBucket;
            ItemStack output;
            if (isBucket(input.getItem())) {
                output = new ItemStack(ForgeModContainer.getInstance().universalBucket);
                isBucket = true;
            } else {
                output = input.copy(); // copy the input to preserve its NBT
                isBucket = false;
            }

            IFluidHandlerItem outputCap = output.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                    null);
            if (outputCap == null) return ItemStack.EMPTY;
            outputCap.drain(Integer.MAX_VALUE, true); // ensure the output is empty

            FluidStack drained = inputCap.drain(1000, false);
            if (drained != null && drained.amount == 1000) {
                if (isBucket) {
                    // if there is a bucket for the fluid, use the bucket
                    if (FluidRegistry.hasBucket(outputFluid.getFluid())) {
                        return FluidUtil.getFilledBucket(outputFluid.copy());
                    }

                    // otherwise return nothing, as the input container is invalid
                    return ItemStack.EMPTY;
                } else if (outputCap.fill(outputFluid.copy(), true) == 1000) {
                    return output;
                }
            }
            return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isBucket(@NotNull Item item) {
        return item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET || item == Items.MILK_BUCKET ||
                item == ForgeModContainer.getInstance().universalBucket;
    }
}
