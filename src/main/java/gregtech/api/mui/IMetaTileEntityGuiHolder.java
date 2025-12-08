package gregtech.api.mui;

// mte imports are for the javadoc, will be removed as stuff is ported
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import gregtech.common.metatileentities.electric.MetaTileEntityPump;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputHatch;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumExtender;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumProxy;

import net.minecraft.network.PacketBuffer;

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

/**
 * TODO: these classes still use legacy MUI. Implement this on them and port to MUI(2): <br/>
 * - {@link SimpleGeneratorMetaTileEntity} - PR 2808 <br/>
 * - {@link SimpleMachineMetaTileEntity} - PR 2808 <br/>
 * - {@link MetaTileEntityClipboard} <br/>
 * - {@link MetaTileEntityPump} <br/>
 * - {@link MetaTileEntityCentralMonitor} <br/>
 * - {@link MetaTileEntityMonitorScreen} <br/>
 * - {@link MetaTileEntityMEInputBus} - PR 2824 <br/>
 * - {@link MetaTileEntityMEInputHatch} - PR 2824 <br/>
 * - {@link MetaTileEntityMEOutputBus} - PR 2824 <br/>
 * - {@link MetaTileEntityMEOutputHatch} - PR 2824 <br/>
 * - All steam machines - PR 2808 <br/>
 * - {@link MetaTileEntityQuantumExtender} <br/>
 * - {@link MetaTileEntityQuantumProxy}
 */
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
    default boolean shouldOpenUI() {
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
