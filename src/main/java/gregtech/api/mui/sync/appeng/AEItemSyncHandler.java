package gregtech.api.mui.sync.appeng;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
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
        return AEStackByteBufAdapter.wrappedItemStackAdapter;
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
        for (int index = 0; index < slots.length; index++) {
            ItemStack stackToSet = index >= itemInputs.size() ? null : itemInputs.get(index);
            if (IntCircuitIngredient.isIntegratedCircuit(stackToSet)) {
                circuitValue = IntCircuitIngredient.getCircuitConfiguration(stackToSet);
            } else {
                setConfig(index, stackToSet);
            }
        }
        setGhostCircuit(circuitValue);

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
