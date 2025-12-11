package gregtech.common.items.behaviors.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;

import com.cleanroommc.modularui.widgets.CycleButtonWidget;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;

import gregtech.common.covers.filter.BaseFilter;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public abstract class BaseFilterUIManager implements IItemBehaviour, ItemUIFactory {

    @Override
    public final ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote && !player.isSneaking()) {
            MetaItemGuiFactory.open(player, hand);
        }
        if (player.isSneaking() && heldItem.hasTagCompound()) {
            heldItem.setTagCompound(null);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        return buildUI(guiData.getUsedItemStack(), guiData, guiSyncManager, settings);
    }

    public ModularPanel buildUI(ItemStack stack, HandGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        return createBasePanel(stack)
                .child(createWidgets(stack, syncManager)
                        .marginTop(22)
                        .marginLeft(7))
                .bindPlayerInventory();
    }

    /**
     * Called when opening the filter panel from the hand
     * @param stack filter stack
     * @return the sized panel
     */
    // panel when opening ui in hand
    protected ModularPanel createBasePanel(ItemStack stack) {
        return GTGuis.createPanel(stack, 176, 166)
//                .child(CoverWithUI.createTitleRow(stack))
                ;
    }

    @Override
    public GTGuiTheme getUITheme() {
        return GTGuiTheme.COVER;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.filter_ui_manager"));
    }

    // panel when opening ui in another ui
    public @NotNull ModularPanel createPopupPanel(ItemStack stack, PanelSyncManager syncManager, String panelName) {
        return GTGuis.createPopupPanel(panelName, 98, 81);
    }

    // panel when opening as a cover
    @NotNull
    public abstract ModularPanel createPanel(ItemStack stack, PanelSyncManager syncManager);

    /** Creates the widgets standalone so that they can be put into their own panel */

    @NotNull
    public abstract Widget<?> createWidgets(ItemStack stack, PanelSyncManager syncManager);

    public BaseFilterReader getFilterReader(ItemStack stack) {
        return BaseFilter.getFilterFromStack(stack).getFilterReader();
    }

    public IWidget createBlacklistUI(ItemStack filterStack) {
        BaseFilterReader filterReader = getFilterReader(filterStack);
        return new ParentWidget<>().coverChildren()
                .child(new CycleButtonWidget()
                        .value(new BooleanSyncValue(
                                filterReader::isBlacklistFilter,
                                filterReader::setBlacklistFilter))
                        .stateBackground(0, GTGuiTextures.BUTTON_BLACKLIST[0])
                        .stateBackground(1, GTGuiTextures.BUTTON_BLACKLIST[1])
                        .addTooltip(0, IKey.lang("cover.filter.blacklist.disabled"))
                        .addTooltip(1, IKey.lang("cover.filter.blacklist.enabled")));
    }
}
