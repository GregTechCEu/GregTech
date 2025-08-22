package gregtech.api.mui.sync.appeng;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.mui.GTByteBufAdapters;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTUtility;
import gregtech.api.util.JEIUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AEItemSyncHandler extends AESyncHandler<IAEItemStack> {

    protected final ExportOnlyAEItemList itemList;
    protected final GhostCircuitItemStackHandler ghostCircuitHandler;

    public AEItemSyncHandler(ExportOnlyAEItemList itemList, @Nullable Runnable dirtyNotifier,
                             @NotNull GhostCircuitItemStackHandler ghostCircuitHandler) {
        super(itemList.getInventory(), itemList.isStocking(), dirtyNotifier);
        this.itemList = itemList;
        this.ghostCircuitHandler = ghostCircuitHandler;
    }

    @Override
    protected @NotNull IConfigurableSlot<IAEItemStack> @NotNull [] initializeCache() {
        // noinspection unchecked
        IConfigurableSlot<IAEItemStack>[] cache = new IConfigurableSlot[slots.length];
        for (int index = 0; index < slots.length; index++) {
            cache[index] = new ExportOnlyAEItemSlot();
        }
        return cache;
    }

    @Override
    protected @NotNull IByteBufAdapter<IAEItemStack> initializeByteBufAdapter() {
        return GTByteBufAdapters.WRAPPED_ITEM_STACK;
    }

    @Override
    public boolean isStackValidForSlot(int index, @Nullable IAEItemStack stack) {
        if (stack == null || stack.getDefinition().isEmpty()) return true;
        if (!isStocking) return true;
        return !itemList.hasStackInConfig(stack.getDefinition(), true);
    }

    @Override
    public IRecipeTransferError receiveRecipe(@NotNull IRecipeLayout recipeLayout, boolean maxTransfer,
                                              boolean simulate) {
        if (simulate) return null;

        Int2ObjectMap<ItemStack> originalItemInputs = JEIUtil.getDisplayedInputItemStacks(recipeLayout.getItemStacks(),
                false, true);
        List<ItemStack> itemInputs = new ArrayList<>(originalItemInputs.values());
        GTUtility.collapseItemList(itemInputs);

        int circuitValue = GhostCircuitItemStackHandler.NO_CONFIG;
        Iterator<ItemStack> inputsIterator = itemInputs.iterator();
        while (inputsIterator.hasNext()) {
            ItemStack stack = inputsIterator.next();
            if (IntCircuitIngredient.isIntegratedCircuit(stack)) {
                inputsIterator.remove();
                circuitValue = IntCircuitIngredient.getCircuitConfiguration(stack);
            }
        }
        setGhostCircuit(circuitValue);

        int lastSlotIndex;
        for (lastSlotIndex = 0; lastSlotIndex < itemInputs.size(); lastSlotIndex++) {
            ItemStack newConfig = itemInputs.get(lastSlotIndex);
            setConfig(lastSlotIndex, newConfig);
        }
        clearConfigFrom(lastSlotIndex);

        return null;
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable ItemStack stack) {
        setConfig(index, WrappedItemStack.fromItemStack(stack));
    }

    protected void setGhostCircuit(int circuitValue) {
        ghostCircuitHandler.setCircuitValue(circuitValue);
    }
}
