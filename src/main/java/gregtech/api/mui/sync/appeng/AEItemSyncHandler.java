package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEItemSyncHandler extends AESyncHandler<IAEItemStack> {

    public AEItemSyncHandler(IConfigurableSlot<IAEItemStack>[] config, @Nullable Runnable dirtyNotifier) {
        super(config, dirtyNotifier);
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

    public void setConfig(int index, @Nullable ItemStack stack) {
        setConfig(index, WrappedItemStack.fromItemStack(stack));
    }
}
