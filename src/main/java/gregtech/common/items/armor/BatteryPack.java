package gregtech.common.items.armor;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class BatteryPack extends ArmorLogicSuite {

    private int cachedSlotId = -1;

    public BatteryPack(int energyPerUse, int maxCapacity, int tier) {
        super(energyPerUse, maxCapacity, tier, EntityEquipmentSlot.CHEST);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        IElectricItem container = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
        boolean canShare = data.hasKey("CanShare") && data.getBoolean("CanShare");
        if (canShare && !world.isRemote) {
            // Trying to find item in inventory
            if (cachedSlotId < 0) {
                // Do not call this method often
                if (world.getWorldTime() % 40 == 0) {
                    cachedSlotId = ArmorUtils.getChargeableItem(player, container.getTier());
                }
            } else {
                ItemStack cachedItem = player.inventory.mainInventory.get(cachedSlotId);
                if (!ArmorUtils.isPossibleToCharge(cachedItem)) {
                    cachedSlotId = -1;
                }
            }

            // Do charge
            if (cachedSlotId >= 0) {
                IElectricItem chargeable = player.inventory.mainInventory.get(cachedSlotId).getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (chargeable == null) {
                    return;
                }
                if (container.canUse(chargeable.getTransferLimit() * 10) && world.getWorldTime() % 10 == 0) {
                    long delta = chargeable.charge(chargeable.getTransferLimit() * 10, chargeable.getTier(), true, false);
                    if (delta > 0) container.discharge(delta, container.getTier(), true, false, false);
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isNeedDrawHUD() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawHUD(ItemStack stack) {
        super.addCapacityHUD(stack);
        NBTTagCompound data = stack.getTagCompound();
        if (data != null) {
            if (data.hasKey("CanShare")) {
                String status = data.getBoolean("CanShare") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled");
                this.HUD.newString(I18n.format("mataarmor.hud.supply_mode", status));
            }
        }
        this.HUD.draw();
        this.HUD.reset();
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        return new ISpecialArmor.ArmorProperties(0, 0, 0);
    }

    @Override
    public void addInfo(ItemStack itemStack, List<String> lines) {
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
        String state = "";
        if (data.hasKey("CanShare")) {
            state = data.getBoolean("CanShare") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled");
        } else {
            state = I18n.format("metaarmor.hud.status.disabled");
        }
        lines.add(I18n.format("metaarmor.energy_share.tooltip", state));
        lines.add(I18n.format("metaarmor.energy_share.tooltip.guide"));
        super.addInfo(itemStack, lines);
    }

    @Override
    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() instanceof ArmorMetaItem<?> && player.isSneaking()) {
            NBTTagCompound data = GTUtility.getOrCreateNbtCompound(player.getHeldItem(hand));
            boolean canShareEnergy = data.hasKey("CanShare") && data.getBoolean("CanShare");

            canShareEnergy = !canShareEnergy;
            String locale = "metaarmor.energy_share." + (canShareEnergy ? "enable" : "disable");
            if (!world.isRemote) player.sendMessage(new TextComponentTranslation(locale));
            data.setBoolean("CanShare", canShareEnergy);
            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else {
            return super.onRightClick(world, player, hand);
        }
    }

    @Override
    public double getDamageAbsorption() {
        return 0;
    }

    @Override
    public int getArmorLayersAmount(ItemStack itemStack) {
        return 2;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return String.format("gregtech:textures/armor/battery_pack_%s.png", GTValues.VN[tier].toLowerCase());
    }
}
