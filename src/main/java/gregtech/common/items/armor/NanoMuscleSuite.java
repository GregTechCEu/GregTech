package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import gregtech.common.items.MetaItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;

import javax.annotation.Nonnull;

public class NanoMuscleSuite extends ArmorLogicSuite {

    public NanoMuscleSuite(EntityEquipmentSlot slot, int energyPerUse, long maxCapacity, int tier) {
        super(energyPerUse, maxCapacity, tier, slot);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @Nonnull ItemStack itemStack) {
        IElectricItem item = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        NBTTagCompound nbtData = GTUtility.getOrCreateNbtCompound(itemStack);
        byte toggleTimer = nbtData.getByte("toggleTimer");
        if (SLOT == EntityEquipmentSlot.HEAD) {
            boolean nightvision = nbtData.getBoolean("Nightvision");
            if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.MODE_SWITCH)) {
                toggleTimer = 10;
                if (!nightvision && item.getCharge() >= 4) {
                    nightvision = true;
                    if (!world.isRemote)
                        player.sendMessage(new TextComponentTranslation("metaarmor.nms.nightvision.enabled"));
                } else if (nightvision) {
                    nightvision = false;
                    disableNightVision(world, player, true);
                } else {
                    if (!world.isRemote) {
                        player.sendMessage(new TextComponentTranslation("metaarmor.message.nightvision.error"));
                    }
                }

                if (!world.isRemote) {
                    nbtData.setBoolean("Nightvision", nightvision);
                }
            }

            if (nightvision && !world.isRemote && item.getCharge() >= 4) {
                player.removePotionEffect(MobEffects.BLINDNESS);
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 999999, 0, true, false));
                item.discharge((4), this.tier, true, false, false);
            }

            if (!world.isRemote && toggleTimer > 0) {
                --toggleTimer;
                nbtData.setByte("toggleTimer", toggleTimer);
            }
        }
        player.inventoryContainer.detectAndSendChanges();
    }

    public void disableNightVision(@Nonnull World world, EntityPlayer player, boolean sendMsg) {
        if (!world.isRemote) {
            player.removePotionEffect(MobEffects.NIGHT_VISION);
            if (sendMsg) player.sendMessage(new TextComponentTranslation("metaarmor.nms.nightvision.disabled"));
        }
    }

    public boolean handleUnblockableDamage(EntityLivingBase entity, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        return source == DamageSource.FALL;
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        IElectricItem container = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        int damageLimit = Integer.MAX_VALUE;
        if (source == DamageSource.FALL && this.getEquipmentSlot(armor) == EntityEquipmentSlot.FEET) {
            if (energyPerUse > 0 && container != null) {
                damageLimit = (int) Math.min(damageLimit, 25.0 * container.getCharge() / energyPerUse);
            }
            return new ArmorProperties(10, (damage < 8.0) ? 1.0 : 0.875, damageLimit);
        }
        return super.getProperties(player, armor, source, damage, equipmentSlot);
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return SLOT;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack itemStack, DamageSource source, int damage, EntityEquipmentSlot equipmentSlot) {
        IElectricItem item = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item != null) {
            item.discharge((long) energyPerUse * damage, item.getTier(), true, false, false);
        }
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        ItemStack currentChest = Minecraft.getMinecraft().player.inventory.armorItemInSlot(EntityEquipmentSlot.CHEST.getIndex());
        ItemStack advancedChest = MetaItems.ADVANCED_NANO_MUSCLE_CHESTPLATE.getStackForm();
        String armorTexture = "nano_muscule_suite";
        if (advancedChest.isItemEqual(currentChest)) armorTexture = "advanced_nano_muscle_suite";
        return SLOT != EntityEquipmentSlot.LEGS ?
                String.format("gregtech:textures/armor/%s_1.png", armorTexture) :
                String.format("gregtech:textures/armor/%s_2.png", armorTexture);
    }

    @Override
    public double getDamageAbsorption() {
        return 0.9D;
    }
}
