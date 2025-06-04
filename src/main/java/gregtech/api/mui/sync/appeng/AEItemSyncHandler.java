package gregtech.api.mui.sync.appeng;

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
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AEItemSyncHandler extends AESyncHandler<IAEItemStack> {

    private final ExportOnlyAEItemList itemList;

    public AEItemSyncHandler(ExportOnlyAEItemList itemList, @Nullable Runnable dirtyNotifier) {
        super(itemList.getInventory(), itemList.isStocking(), dirtyNotifier);
        this.itemList = itemList;
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
        return IAEStackByteBufAdapter.wrappedItemStackAdapter;
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

        List<ItemStack> originalItemInputs = JEIUtil.getDisplayedInputItemStacks(recipeLayout.getItemStacks());
        List<ItemStack> itemInputs = new ArrayList<>(originalItemInputs.size());
        originalItemInputs.forEach(stack -> {
            if (!stack.isEmpty()) {
                itemInputs.add(stack.copy());
            }
        });
        GTUtility.collapseItemList(itemInputs);

        for (int index = 0; index < slots.length; index++) {
            ItemStack stackToSet = index >= itemInputs.size() ? null : itemInputs.get(index);
            setConfig(index, stackToSet);
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable ItemStack stack) {
        setConfig(index, WrappedItemStack.fromItemStack(stack));
    }
}
