package gregtech.api.cover;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import gregtech.api.gui.GTGuis;
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
    default ModularScreen createClientGui(EntityPlayer entityPlayer) {
        CoverBehavior cover = (CoverBehavior) this;
        return GregTechGuiScreen.simple(cover.getCoverDefinition().getCoverId(), context -> createUIPanel(context, entityPlayer));
    }

    // TODO: make abstract
    default ModularPanel createUIPanel(GuiContext context, EntityPlayer player) {
        return ModularPanel.defaultPanel(context);
    }

    @Override
    default void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer) {
    }
}
