package gregtech.api.items.metaitem;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.items.metaitem.stats.*;
import gregtech.common.ConfigHolder;
import gregtech.integration.baubles.BaublesModule;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Loader;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ElectricStats implements IItemComponent, IItemCapabilityProvider, IItemMaxStackSizeProvider,
                           IItemBehaviour, ISubItemHandler {

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
        if (electricItem != null && electricItem.canProvideChargeExternally() && player.isSneaking()) {
            if (!world.isRemote) {
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
        if (!entity.world.isRemote && entity instanceof EntityPlayer entityPlayer && electricItem != null &&
                electricItem.canProvideChargeExternally() &&
                isInDischargeMode(itemStack) && electricItem.getCharge() > 0L) {

            IInventory inventoryPlayer = entityPlayer.inventory;
            long transferLimit = electricItem.getTransferLimit();

            if (Loader.isModLoaded(GTValues.MODID_BAUBLES)) {
                inventoryPlayer = BaublesModule.getBaublesWrappedInventory(entityPlayer);
            }

            for (int i = 0; i < inventoryPlayer.getSizeInventory(); i++) {
                ItemStack itemInSlot = inventoryPlayer.getStackInSlot(i);
                IElectricItem slotElectricItem = itemInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM,
                        null);
                IEnergyStorage feEnergyItem = itemInSlot.getCapability(CapabilityEnergy.ENERGY, null);
                if (slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {

                    long chargedAmount = chargeElectricItem(transferLimit, electricItem, slotElectricItem);
                    if (chargedAmount > 0L) {
                        transferLimit -= chargedAmount;
                        if (transferLimit == 0L) break;
                    }
                } else if (ConfigHolder.compat.energy.nativeEUToFE && feEnergyItem != null) {
                    if (feEnergyItem.getEnergyStored() < feEnergyItem.getMaxEnergyStored()) {
                        int energyMissing = feEnergyItem.getMaxEnergyStored() - feEnergyItem.getEnergyStored();
                        long euToCharge = FeCompat.toEu(energyMissing, ConfigHolder.compat.energy.feToEuRatio);
                        long energyToTransfer = Math.min(euToCharge, transferLimit);
                        long maxDischargeAmount = Math.min(energyToTransfer,
                                electricItem.discharge(energyToTransfer, electricItem.getTier(), false, true, true));
                        FeCompat.insertEu(feEnergyItem, maxDischargeAmount);
                        electricItem.discharge(maxDischargeAmount, electricItem.getTier(), false, true, false);
                    }
                }
            }
        }
    }

    private static long chargeElectricItem(long maxDischargeAmount, IElectricItem source, IElectricItem target) {
        long maxDischarged = source.discharge(maxDischargeAmount, source.getTier(), false, false, true);
        long maxReceived = target.charge(maxDischarged, source.getTier(), false, true);
        if (maxReceived > 0L) {
            long resultDischarged = source.discharge(maxReceived, source.getTier(), false, true, false);
            target.charge(resultDischarged, source.getTier(), false, false);
            return resultDischarged;
        }
        return 0L;
    }

    private static void setInDischargeMode(ItemStack itemStack, boolean isDischargeMode) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (isDischargeMode) {
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                itemStack.setTagCompound(tagCompound);
            }
            tagCompound.setBoolean("DischargeMode", true);
        } else {
            if (tagCompound != null) {
                tagCompound.removeTag("DischargeMode");
                if (tagCompound.isEmpty()) {
                    itemStack.setTagCompound(null);
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null && electricItem.canProvideChargeExternally()) {
            addTotalChargeTooltip(lines, electricItem.getMaxCharge(), electricItem.getTier());
            if (isInDischargeMode(itemStack)) {
                lines.add(I18n.format("metaitem.electric.discharge_mode.enabled"));
            } else {
                lines.add(I18n.format("metaitem.electric.discharge_mode.disabled"));
            }
            lines.add(I18n.format("metaitem.electric.discharge_mode.tooltip"));
        }
    }

    private static void addTotalChargeTooltip(List<String> tooltip, long maxCharge, int tier) {
        Instant start = Instant.now();
        Instant end = Instant.now().plusSeconds((long) ((maxCharge * 1.0) / GTValues.V[tier] / 20));
        Duration duration = Duration.between(start, end);

        long chargeTime;
        String unit;
        if (duration.getSeconds() <= 180) {
            chargeTime = duration.getSeconds();
            unit = I18n.format("metaitem.battery.charge_unit.second");
        } else if (duration.toMinutes() <= 180) {
            chargeTime = duration.toMinutes();
            unit = I18n.format("metaitem.battery.charge_unit.minute");
        } else {
            chargeTime = duration.toHours();
            unit = I18n.format("metaitem.battery.charge_unit.hour");
        }
        tooltip.add(I18n.format("metaitem.battery.charge_time", chargeTime, unit, GTValues.VNF[tier]));
    }

    private static boolean isInDischargeMode(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound != null && tagCompound.getBoolean("DischargeMode");
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        ElectricItem electricItem = (ElectricItem) itemStack
                .getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || electricItem.getCharge() == 0) {
            return defaultValue;
        }
        return 1;
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return "";
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        ItemStack copy = itemStack.copy();
        IElectricItem electricItem = copy.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
            subItems.add(copy);
        } else {
            subItems.add(itemStack);
        }
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
