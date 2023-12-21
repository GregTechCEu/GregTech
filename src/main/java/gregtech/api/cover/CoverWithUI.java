package gregtech.api.cover;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GregTechGuiScreen;
import gregtech.api.mui.factory.CoverGuiFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import org.jetbrains.annotations.ApiStatus;

public interface CoverWithUI extends Cover, IUIHolder, IGuiHolder<SidedPosGuiData> {

    @ApiStatus.Experimental
    default boolean usesMui2() {
        return false;
    }

    default void openUI(EntityPlayerMP player) {
        if (usesMui2()) {
            CoverGuiFactory.open(player, this);
        } else {
            CoverUIFactory.INSTANCE.openUI(this, player);
        }
    }

    @Deprecated
    default ModularUI createUI(EntityPlayer player) {
        return null;
    }

    @ApiStatus.NonExtendable
    @SideOnly(Side.CLIENT)
    @Override
    default ModularScreen createScreen(SidedPosGuiData guiData, ModularPanel mainPanel) {
        return new GregTechGuiScreen(mainPanel, getUITheme());
    }

    default GTGuiTheme getUITheme() {
        return GTGuiTheme.STANDARD;
    }

    @Override
    default ModularPanel buildUI(SidedPosGuiData guiData, GuiSyncManager guiSyncManager) {
        return null;
    }

    @Override
    default boolean isValid() {
        return getCoverableView().isValid();
    }

    @Override
    default boolean isRemote() {
        return getCoverableView().getWorld().isRemote;
    }

    @Override
    default void markAsDirty() {
        getCoverableView().markDirty();
    }
}
