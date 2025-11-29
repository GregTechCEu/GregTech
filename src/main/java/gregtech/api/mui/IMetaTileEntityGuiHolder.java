package gregtech.api.mui;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface IMetaTileEntityGuiHolder extends IGuiHolder<MetaTileEntityGuiData> {

    @ApiStatus.NonExtendable
    @NotNull
    @Override
    default ModularScreen createScreen(MetaTileEntityGuiData data, ModularPanel mainPanel) {
        return new GregTechGuiScreen(mainPanel, getUITheme());
    }

    default GTGuiTheme getUITheme() {
        return GTGuiTheme.STANDARD;
    }

    /**
     * @return whether this {@link MetaTileEntity} should open its UI.
     */
    default boolean shouldOpenUI(@NotNull EntityPlayerMP player, @NotNull EnumHand hand, @NotNull EnumFacing side,
                                 @NotNull CuboidRayTraceResult hitResult) {
        return true;
    }

    @NotNull
    @Override
    ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings);

    /**
     * Write extra data on the server that will be available on both sides before UI construction. <br/>
     * Retrieve the data with {@link MetaTileEntityGuiData#getBuffer()}.
     */
    default void writeExtraGuiData(@NotNull PacketBuffer buffer) {}
}
