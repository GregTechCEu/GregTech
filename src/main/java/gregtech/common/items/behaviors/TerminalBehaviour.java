package gregtech.common.items.behaviors;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.EmptyTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

public class TerminalBehaviour implements IItemBehaviour, ItemUIFactory {
    private static final TextureArea TERMINAL_FRAME = TextureArea.fullImage("textures/gui/terminal/terminal_frame.png");


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
        TerminalOSWidget os = new TerminalOSWidget(12, 11, 333, 232, holder.getCurrentItem().getOrCreateSubCompound("terminal"))
                .setBackground(TerminalTheme.WALL_PAPER);
        CircleButtonWidget home = new CircleButtonWidget(363, 126, 11, 2, 0)
                .setColors(0, TerminalTheme.COLOR_F_1.getColor(), 0)
                .setClickListener(clickData -> os.homeTrigger(clickData.isClient));
        return ModularUI.builder(new EmptyTextureArea(380, 256), 380, 256)
                .widget(os)
                .widget(new ImageWidget(0, 0, 380, 256, TERMINAL_FRAME))
                .widget(home)
                .build(holder, entityPlayer);
    }
}
