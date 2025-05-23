package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IEquals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEItemSyncHandler extends AESyncHandler<IAEItemStack> {

    public AEItemSyncHandler(IConfigurableSlot<IAEItemStack> config) {
        super(config);
        cache = new ExportOnlyAEItemSlot();
        // TODO: when 2672 is merged, use GTByteBufAdapters.makeAdapter
        byteBufAdapter = new WrappedItemStackByteBufAdapter();
    }

    public void setConfig(@Nullable ItemStack stack) {
        setConfig(WrappedItemStack.fromItemStack(stack));
    }

    private static class WrappedItemStackByteBufAdapter implements IByteBufAdapter<IAEItemStack> {

        @Override
        public WrappedItemStack deserialize(PacketBuffer buffer) {
            return WrappedItemStack.fromPacket(buffer);
        }

        @Override
        public void serialize(PacketBuffer buffer, IAEItemStack u) {
            if (u instanceof WrappedItemStack wrapped) {
                wrapped.writeToPacket(buffer);
            } else {
                throw new IllegalArgumentException(
                        "A non wrapped IAEItemStack was passed to the AEItemSyncHandler ByteBufAdapter!");
            }
        }

        @Override
        public boolean areEqual(@NotNull IAEItemStack t1, @NotNull IAEItemStack t2) {
            return IEquals.defaultTester().areEqual(t1, t2);
        }
    }
}
