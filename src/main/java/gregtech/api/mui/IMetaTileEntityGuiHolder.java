package gregtech.api.mui;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.IGuiHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface IMetaTileEntityGuiHolder extends IGuiHolder<MetaTileEntityGuiData> {

    /**
     * Write extra data on the server that will be available on both sides before UI construction. <br/>
     * Retrieve the data with {@link MetaTileEntityGuiData#getBuffer()}.
     */
    @ApiStatus.Internal
    default void writeExtraGuiData(@NotNull PacketBuffer buffer) {}
}
