package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable ItemStack stack) {
        setConfig(index, WrappedItemStack.fromItemStack(stack));
    }
}
