package gregtech.common.items.behaviors;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.terminal.TerminalBuilder;
import gregtech.api.terminal.gui.widgets.os.TerminalOSWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

public class GuideTerminalBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!world.isRemote) {
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        TerminalOSWidget os = new TerminalOSWidget(12, 11, 333, 232).setBackground(GuiTextures.TERMINAL_BACKGROUND);
        TerminalBuilder.getApplications().forEach(os::installApplication);
        return ModularUI.builder(GuiTextures.TERMINAL_FRAME, 380, 256)
                .widget(os)
                .widget(new ImageWidget(0, 0, 380, 256, GuiTextures.TERMINAL_FRAME))
                .build(holder, entityPlayer);
    }
}
