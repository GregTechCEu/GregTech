package gregtech.api.items.metaitem;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public class ElectricStats implements IItemComponent, IItemCapabilityProvider, IItemMaxStackSizeProvider, IItemBehaviour {

    public static final ElectricStats EMPTY = new ElectricStats(0, 0, false, false);

    public final long maxCharge;
    public final int tier;

    public final boolean chargeable;
    public final boolean dischargeable;

    public ElectricStats(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        this.maxCharge = maxCharge;
        this.tier = (int) tier;
        this.chargeable = chargeable;
        this.dischargeable = dischargeable;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if(electricItem != null && electricItem.canProvideChargeExternally() && player.isSneaking()) {
            if(!world.isRemote) {
                boolean isInDischargeMode = isInDischargeMode(itemStack);
                String locale = "metaitem.electric.discharge_mode." + (isInDischargeMode ? "disabled" : "enabled");
                player.sendStatusMessage(new TextComponentTranslation(locale), true);
                setInDischargeMode(itemStack, !isInDischargeMode);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
        }
        return ActionResult.newResult(EnumActionResult.PASS, itemStack);
    }

    @Override
    public void onUpdate(ItemStack itemStack, Entity entity) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if(!entity.world.isRemote && entity instanceof EntityPlayer && electricItem != null &&
            electricItem.canProvideChargeExternally() &&
            isInDischargeMode(itemStack) && electricItem.getCharge() > 0L) {

            EntityPlayer entityPlayer = (EntityPlayer) entity;
            InventoryPlayer inventoryPlayer = entityPlayer.inventory;
            long transferLimit = electricItem.getTransferLimit();

            for(int i = 0; i < inventoryPlayer.getSizeInventory(); i++) {
                ItemStack itemInSlot = inventoryPlayer.getStackInSlot(i);
                IElectricItem slotElectricItem = itemInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if(slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {

                    long chargedAmount = chargeElectricItem(transferLimit, electricItem, slotElectricItem);
                    if(chargedAmount > 0L) {
                        transferLimit -= chargedAmount;
                        if(transferLimit == 0L) break;
                    }
                }
            }
        }
    }

    private static long chargeElectricItem(long maxDischargeAmount, IElectricItem source, IElectricItem target) {
        long maxDischarged = source.discharge(maxDischargeAmount, source.getTier(), false, false, true);
        long maxReceived = target.charge(maxDischarged, source.getTier(), false, true);
        if(maxReceived > 0L) {
            long resultDischarged = source.discharge(maxReceived, source.getTier(), false, true, false);
            target.charge(resultDischarged, source.getTier(), false, false);
            return resultDischarged;
        }
        return 0L;
    }

    private static void setInDischargeMode(ItemStack itemStack, boolean isDischargeMode) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if(isDischargeMode) {
            if(tagCompound == null) {
                tagCompound = new NBTTagCompound();
                itemStack.setTagCompound(tagCompound);
            }
            tagCompound.setBoolean("DischargeMode", true);
        } else {
            if(tagCompound != null) {
                tagCompound.removeTag("DischargeMode");
                if(tagCompound.isEmpty()) {
                    itemStack.setTagCompound(null);
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if(electricItem != null && electricItem.canProvideChargeExternally()) {
            lines.add(I18n.format("metaitem.electric.discharge_mode.tooltip"));
        }
    }

    private static boolean isInDischargeMode(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound != null && tagCompound.getBoolean("DischargeMode");
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        ElectricItem electricItem = (ElectricItem) itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || electricItem.getCharge() == 0) {
            return defaultValue;
        }
        return 1;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new ElectricItem(itemStack, maxCharge, tier, chargeable, dischargeable);
    }

    public static ElectricStats createElectricItem(long maxCharge, long tier) {
        return new ElectricStats(maxCharge, tier, true, false);
    }

    public static ElectricStats createRechargeableBattery(long maxCharge, int tier) {
        return new ElectricStats(maxCharge, tier, true, true);
    }

    public static ElectricStats createBattery(long maxCharge, int tier, boolean rechargeable) {
        return new ElectricStats(maxCharge, tier, rechargeable, true);
    }
}
