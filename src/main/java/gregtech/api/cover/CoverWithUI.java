package gregtech.api.cover;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import gregtech.api.newgui.GTGuis;
import gregtech.api.gui.GregTechGuiScreen;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface CoverWithUI extends IGuiHolder {

    default boolean usesMui2() {
        return false;
    }

    default void openUI(EntityPlayerMP player) {
        if (usesMui2()) {
            CoverBehavior cover = (CoverBehavior) this;
            GTGuis.getCoverUiInfo(cover.getAttachedSide()).open(player, cover.coverHolder.getWorld(), cover.coverHolder.getPos());
        } else {
            CoverBehaviorUIFactory.INSTANCE.openUI((CoverBehavior) this, player);
        }
    }

    @Deprecated
    ModularUI createUI(EntityPlayer player);

    @Override
    default ModularScreen createScreen(GuiCreationContext guiCreationContext, ModularPanel mainPanel) {
        return new GregTechGuiScreen(mainPanel);
    }

    @Override
    default ModularPanel buildUI(GuiCreationContext guiCreationContext, GuiSyncManager guiSyncManager, boolean isClient) {
        return null;
    }
}
