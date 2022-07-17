package gregtech.api.cover;

import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import gregtech.api.guiOld.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface CoverWithUI {

    default void openUI(EntityPlayerMP player) {
        CoverBehaviorUIFactory.INSTANCE.openUI((CoverBehavior) this, player);
    }

    @Deprecated
    default ModularUI createUI(EntityPlayer player) {
        return null;
    }

    default ModularWindow createWindow(UIBuildContext buildContext) {
        return null;
    }
}
