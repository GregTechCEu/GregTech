package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IEquals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEFluidSyncHandler extends AESyncHandler<IAEFluidStack> {

    public AEFluidSyncHandler(IConfigurableSlot<IAEFluidStack> config) {
        super(config);
        cache = new ExportOnlyAEFluidSlot();
        // TODO: when 2672 is merged, use GTByteBufAdapters.makeAdapter
        byteBufAdapter = new WrappedFluidStackByteBufAdapter();
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(@Nullable FluidStack stack) {
        setConfig(WrappedFluidStack.fromFluidStack(stack));
    }

    private static class WrappedFluidStackByteBufAdapter implements IByteBufAdapter<IAEFluidStack> {

        @Override
        public IAEFluidStack deserialize(PacketBuffer buffer) {
            return WrappedFluidStack.fromPacket(buffer);
        }

        @Override
        public void serialize(PacketBuffer buffer, IAEFluidStack u) {
            if (u instanceof WrappedFluidStack wrapped) {
                wrapped.writeToPacket(buffer);
            } else {
                throw new IllegalArgumentException(
                        "A non wrapped IAEFluidStack was passed to the AEFluidSyncHandler ByteBufAdapter!");
            }
        }

        @Override
        public boolean areEqual(@NotNull IAEFluidStack t1, @NotNull IAEFluidStack t2) {
            return IEquals.defaultTester().areEqual(t1, t2);
        }
    }
}
