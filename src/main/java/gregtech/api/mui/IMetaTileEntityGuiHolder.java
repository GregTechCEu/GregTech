package gregtech.api.mui;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

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
                                 @UnknownNullability CuboidRayTraceResult hitResult) {
        return true;
    }

    /**
     * Build and return a {@link ModularPanel} with widgets to display to the player.
     * 
     * @param data        Contains data about how the {@link MetaTileEntity} was opened, like the player, position of
     *                    the MTE, and extra data from the server written in {@link #writeExtraGuiData(PacketBuffer)}.
     * @param syncManager the sync manager for this panel. Is used to register extra {@link SyncHandler}s not used in
     *                    methods that automatically register the sync handler like
     *                    {@link ButtonWidget#syncHandler(InteractionSyncHandler)} or
     *                    {@link TextFieldWidget#value(IStringValue)}.
     * @param settings    contains miscellaneous settings related to this UI.
     */
    @NotNull
    @Override
    ModularPanel buildUI(MetaTileEntityGuiData data, PanelSyncManager syncManager, UISettings settings);

    /**
     * Write extra data on the server that will be available on both sides before UI construction. <br/>
     * Retrieve the data with {@link MetaTileEntityGuiData#getBuffer()}.
     */
    default void writeExtraGuiData(@NotNull PacketBuffer buffer) {}
}
