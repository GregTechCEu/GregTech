package gregtech.api.cover;

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
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface CoverWithUI extends Cover, IGuiHolder<SidedPosGuiData>, gregtech.api.gui.IUIHolder {

    @ApiStatus.Experimental
    default boolean usesMui2() {
        // this is gonna cause problems if implementing classes expect this to be false
        // all of our covers use mui2 though
        return true;
    }

    default void openUI(EntityPlayerMP player) {
        if (usesMui2()) {
            CoverGuiFactory.open(player, this);
        } else {
            // todo remove in 2.10
            CoverUIFactory.INSTANCE.openUI(this, player);
        }
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    default gregtech.api.gui.ModularUI createUI(EntityPlayer player) {
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
    default ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
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
    static Flow createTitleRow(ItemStack stack) {
        return createTitleRow(() -> stack);
    }

    /**
     * Create the Title bar widget for a Cover.
     */
    static Flow createTitleRow(Supplier<ItemStack> stack) {
        ItemDrawable itemDrawable = new ItemDrawable();
        return Flow.row()
                .pos(4, 4)
                .height(16).coverChildrenWidth()
                .child(new Widget<>()
                        .overlay(new DynamicDrawable(() -> itemDrawable.setItem(stack.get())))
                        .size(16)
                        .marginRight(4))
                .child(IKey.dynamic(() -> stack.get().getDisplayName())
                        .color(UI_TITLE_COLOR)
                        .asWidget().heightRel(1.0f));
    }

    /**
     * Create a new settings row for a Cover setting.
     */
    default ParentWidget<?> createSettingsRow() {
        return new ParentWidget<>().height(16).widthRel(1.0f).marginBottom(2);
    }

    /**
     * Create a dynamic lang key that switches between {@code cover.generic.enabled} and {@code cover.generic.disabled}
     * depending on the result of the given boolean supplier. <br/>
     *
     * @param keyBase the base of the lang key to use. {@code .enabled} and {@code .disabled} will be appended.
     */
    default IKey createEnabledKey(@NotNull String keyBase, @NotNull BooleanSupplier enabledState) {
        String enabled = keyBase + ".enabled";
        String disabled = keyBase + ".disabled";
        return IKey.lang(() -> enabledState.getAsBoolean() ? enabled : disabled);
    }

    default int getIncrementValue(MouseData data) {
        int adjust = 1;
        if (data.shift) adjust *= 4;
        if (data.ctrl) adjust *= 16;
        if (data.alt) adjust *= 64;
        return adjust;
    }

    default IKey createAdjustOverlay(boolean increment) {
        final StringBuilder builder = new StringBuilder();
        builder.append(increment ? '+' : '-');
        builder.append(getIncrementValue(MouseData.create(-1)));

        float scale = 1f;
        if (builder.length() == 3) {
            scale = 0.8f;
        } else if (builder.length() == 4) {
            scale = 0.6f;
        } else if (builder.length() > 4) {
            scale = 0.5f;
        }
        return IKey.str(builder.toString())
                .color(Color.WHITE.main)
                .scale(scale);
    }
}
