package gregtech.common.items.behaviors;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

public class ToggleEnergyConsumerBehavior implements IItemBehaviour {

    private final int energyUsagePerTick;

    public ToggleEnergyConsumerBehavior(int energyUsagePerTick) {
        this.energyUsagePerTick = energyUsagePerTick;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            boolean isItemActive = isItemActive(itemStack);
            if (isItemActive) {
                setItemActive(itemStack, false);
            } else if (electricItem != null && drainActivationEnergy(electricItem, true)) {
                setItemActive(itemStack, true);
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, itemStack);
    }

    private boolean drainActivationEnergy(IElectricItem electricItem, boolean simulate) {
        return electricItem.discharge(energyUsagePerTick, electricItem.getTier(), true, false, simulate) >= energyUsagePerTick;
    }

    @Override
    public void onUpdate(ItemStack itemStack, Entity entity) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (isItemActive(itemStack) && electricItem != null) {
            boolean shouldRemainActive = drainActivationEnergy(electricItem, false);
            if (!shouldRemainActive) {
                setItemActive(itemStack, false);
            }
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behavior.toggle_energy_consumer.tooltip"));
    }

    public static boolean isItemActive(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound != null && tagCompound.getBoolean("Active");
    }

    public static void setItemActive(ItemStack itemStack, boolean isActive) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        tagCompound.setBoolean("Active", isActive);
    }
}
