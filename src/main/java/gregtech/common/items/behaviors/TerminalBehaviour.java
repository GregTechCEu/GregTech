package gregtech.common.items.behaviors;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.EmptyTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.terminal.hardware.HardwareProvider;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TerminalBehaviour implements IItemBehaviour, ItemUIFactory, ISubItemHandler {
    public static final TextureArea TERMINAL_FRAME = TextureArea.fullImage("textures/gui/terminal/terminal_frame.png");

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        itemStack.getOrCreateSubCompound("terminal").removeTag("_click");
        if (pos != null) {
            itemStack.getOrCreateSubCompound("terminal").setTag("_click", NBTUtil.createPosTag(pos));
            if (!world.isRemote) {
                PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
                holder.openUI();
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        itemStack.getOrCreateSubCompound("terminal").removeTag("_click");
        if (!world.isRemote) {
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        HardwareProvider provider = itemStack.getCapability(GregtechCapabilities.CAPABILITY_HARDWARE_PROVIDER, null);
        if (isCreative(itemStack)) {
            lines.add(I18n.format("metaitem.terminal.tooltip.creative"));
        }
        if (provider != null) {
            List<Hardware> hardware = provider.getHardware();
            lines.add(I18n.format("metaitem.terminal.tooltip.hardware", hardware.size()));
            for (Hardware hw : hardware) {
                String info = hw.addInformation();
                if (info == null) {
                    lines.add(hw.getLocalizedName());
                } else {
                    lines.add(String.format("%s (%s)", hw.getLocalizedName(), info));
                }
            }
        }
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        TerminalOSWidget os = new TerminalOSWidget(12, 11, 333, 232, holder.getCurrentItem())
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

    public static boolean isCreative(ItemStack itemStack) {
        return itemStack != null && itemStack.getOrCreateSubCompound("terminal").getBoolean("_creative");
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound("terminal").getBoolean("_creative") ? "creative" : "normal";
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack);
        ItemStack copy = itemStack.copy();
        copy.getOrCreateSubCompound("terminal").setBoolean("_creative", true);
        subItems.add(copy);
    }
}
