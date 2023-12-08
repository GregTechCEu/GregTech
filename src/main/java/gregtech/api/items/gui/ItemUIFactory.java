package gregtech.api.items.gui;

import gregtech.api.gui.ModularUI;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GregTechGuiScreen;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import org.jetbrains.annotations.ApiStatus;

public interface ItemUIFactory extends IItemComponent, IGuiHolder {

    /**
     * Creates new UI basing on given holder. Holder contains information
     * about item stack and hand, and also player
     */
    @Deprecated
    default ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return null;
    }

    @ApiStatus.NonExtendable
    @Override
    default ModularScreen createScreen(GuiCreationContext creationContext, ModularPanel mainPanel) {
        return new GregTechGuiScreen(mainPanel, getUITheme());
    }

    default GTGuiTheme getUITheme() {
        return GTGuiTheme.STANDARD;
    }

    @Override
    default ModularPanel buildUI(GuiCreationContext guiCreationContext, GuiSyncManager guiSyncManager,
                                 boolean isClient) {
        return null;
    }
}
