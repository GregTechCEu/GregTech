package gregtech.api.items.gui;

import com.cleanroommc.modularui.api.IItemGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import gregtech.api.gui.GregTechGuiScreen;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ItemUIFactory extends IItemComponent, IItemGuiHolder {

    /**
     * Creates new UI basing on given holder. Holder contains information
     * about item stack and hand, and also player
     */
    @Deprecated
    ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer);

    @Override
    default ModularScreen createGuiScreen(EntityPlayer entityPlayer, ItemStack stack) {
        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
        return GregTechGuiScreen.simple(valueItem, context -> createUIPanel(context, entityPlayer, stack));
    }

    // TODO: make abstract
    default ModularPanel createUIPanel(GuiContext context, EntityPlayer player, ItemStack stack) {
        return ModularPanel.defaultPanel(context);
    }

    @Override
    default void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer, ItemStack var3) {
    }
}
