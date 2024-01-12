package gregtech.api.cover;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GregTechGuiScreen;
import gregtech.api.mui.factory.CoverGuiFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
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
        return GTGuiTheme.COVER;
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

    /* Helper methods for UI creation with covers that are commonly used */

    /**
     * The color used for Cover UI titles, and used in {@link #createTitleRow}.
     */
    int UI_TITLE_COLOR = 0xFF222222;
    /**
     * The color used for Cover UI text. Available for reference, but is
     * handled automatically by the {@link GTGuiTheme#COVER} theme.
     */
    int UI_TEXT_COLOR = 0xFF555555;

    /**
     * Create the Title bar widget for a Cover.
     */
    static Row createTitleRow(ItemStack stack) {
        return new Row()
                .pos(4, 4)
                .height(16).coverChildrenWidth()
                .child(new ItemDrawable(stack).asWidget().size(16).marginRight(4))
                .child(IKey.str(stack.getDisplayName()).color(UI_TITLE_COLOR).asWidget().heightRel(1.0f));
    }

    /**
     * Create a new settings row for a Cover setting.
     */
    default ParentWidget<?> createSettingsRow() {
        return new ParentWidget<>().height(16).widthRel(1.0f).marginBottom(2);
    }

    /**
     * Get a BoolValue for use with toggle buttons which are "linked together,"
     * meaning only one of them can be pressed at a time.
     */
    default <T extends Enum<T>> BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }

    /**
     * Get a BoolValue for use with toggle buttons which are "linked together,"
     * meaning only one of them can be pressed at a time.
     */
    default BoolValue.Dynamic boolValueOf(IntSyncValue syncValue, int value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }
}
