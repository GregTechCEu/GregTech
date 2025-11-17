package gregtech.api.items.gui;

import gregtech.api.gui.ModularUI;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.GregTechGuiScreen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.ApiStatus;

public interface ItemUIFactory extends IItemComponent, IGuiHolder<PlayerInventoryGuiData> {

    /**
     * Creates new UI basing on given holder. Holder contains information
     * about item stack and hand, and also player
     */
    @Deprecated
    default ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return null;
    }

    @ApiStatus.NonExtendable
    @SideOnly(Side.CLIENT)
    @Override
    default ModularScreen createScreen(PlayerInventoryGuiData creationContext, ModularPanel mainPanel) {
        return new GregTechGuiScreen(mainPanel, getUITheme());
    }

    default GTGuiTheme getUITheme() {
        return GTGuiTheme.STANDARD;
    }

    @Override
    default ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager panelSyncManager,
                                 UISettings settings) {
        return GTGuis.errorPanel();
    }
}
