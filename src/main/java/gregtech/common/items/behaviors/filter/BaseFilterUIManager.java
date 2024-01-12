package gregtech.common.items.behaviors.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;

import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public abstract class BaseFilterUIManager implements IItemBehaviour, ItemUIFactory {

    protected static final ModularPanel ERROR = GTGuis.createPanel("ERROR", 100, 100)
            .child(IKey.str("This is an error!").asWidget().align(Alignment.Center));

    @Override
    public final ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote && !Interactable.hasShiftDown()) {
            MetaItemGuiFactory.open(player, hand);
        }
        if (Interactable.hasShiftDown() && heldItem.hasTagCompound()) {
            heldItem.setTagCompound(null);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public abstract ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager);

    protected final ModularPanel createBasePanel(ItemStack stack) {
        return GTGuis.createPanel(stack, getWidth(), getHeight())
                .child(CoverWithUI.createTitleRow(stack));
    }

    protected int getWidth() {
        return 176;
    }

    protected int getHeight() {
        return 188;
    }
}
