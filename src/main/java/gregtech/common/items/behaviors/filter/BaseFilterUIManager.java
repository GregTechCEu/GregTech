package gregtech.common.items.behaviors.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;

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

import java.util.List;

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
    public abstract ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings);

    protected final ModularPanel createBasePanel(ItemStack stack) {
        return GTGuis.createPanel(stack, 176, 188)
                .child(CoverWithUI.createTitleRow(stack));
    }

    @Override
    public GTGuiTheme getUITheme() {
        return GTGuiTheme.COVER;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.filter_ui_manager"));
    }
}
