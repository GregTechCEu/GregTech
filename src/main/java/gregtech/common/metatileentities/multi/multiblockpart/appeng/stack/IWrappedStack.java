package gregtech.common.metatileentities.multi.multiblockpart.appeng.stack;

import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IWrappedStack<AEStackType extends IAEStack<AEStackType>, RealStackType> extends IAEStack<AEStackType> {

    @NotNull
    RealStackType getDefinition();

    void writeToPacketBuffer(@NotNull PacketBuffer packetBuffer);

    boolean delegateAndSizeEqual(@Nullable IWrappedStack<AEStackType, RealStackType> wrappedStack);

    @NotNull
    IWrappedStack<AEStackType, RealStackType> copyWrapped();

    @NotNull
    AEStackType copyAsAEStack();
}
