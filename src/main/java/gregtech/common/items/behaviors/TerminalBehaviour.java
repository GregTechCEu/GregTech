package gregtech.common.items.behaviors;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.terminal.hardware.HardwareProvider;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.common.terminal.hardware.BatteryHardware;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TerminalBehaviour implements IItemBehaviour, ItemUIFactory, ISubItemHandler {

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        if (player.isSneaking()) {
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
    public void onUpdate(ItemStack itemStack, Entity entity) {
        NBTTagCompound tabletNBT = itemStack.getOrCreateSubCompound("terminal");
        if (entity.ticksExisted % 20 == 0 && tabletNBT.hasKey("_ar")) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase livingBase = (EntityLivingBase) entity;
                if (!livingBase.getHeldItemMainhand().isItemEqual(itemStack) &&
                        !livingBase.getHeldItemOffhand().isItemEqual(itemStack)) {
                    return;
                }
            }

            String appName = tabletNBT.getString("_ar");
            int tier = TerminalRegistry.getApplication(appName).getMaxTier();
            if (!TerminalBehaviour.isCreative(itemStack)) {
                tier = Math.min(tabletNBT.getCompoundTag(appName).getInteger("_tier"), tier);
            }
            long cost = 0;
            for (Hardware hardware : TerminalRegistry.getAppHardwareDemand(appName, tier)) {
                if (hardware instanceof BatteryHardware) {
                    cost = ((BatteryHardware) hardware).getCharge();
                    break;
                }
            }
            if (cost > 0) {
                IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM,
                        null);
                if (electricItem != null) {
                    long back = electricItem.discharge(cost, 999, true, false, false);
                    if (back != cost) {
                        tabletNBT.removeTag("_ar");
                    }
                } else {
                    tabletNBT.removeTag("_ar");
                }
            }
        }
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
        return ModularUI.builder(IGuiTexture.EMPTY, 380, 256)
                .widget(new TerminalOSWidget(12, 11, holder.getCurrentItem()))
                .shouldColor(false)
                .build(holder, entityPlayer);
    }

    public static boolean isCreative(ItemStack itemStack) {
        return itemStack != null && itemStack.getOrCreateSubCompound("terminal").getBoolean("_creative");
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return "";
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        ItemStack copy = itemStack.copy();
        copy.getOrCreateSubCompound("terminal").setBoolean("_creative", true);
        subItems.add(copy);
    }
}
