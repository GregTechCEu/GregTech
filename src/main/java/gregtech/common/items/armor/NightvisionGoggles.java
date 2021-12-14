package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import gregtech.common.ConfigHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class NightvisionGoggles extends ArmorLogicSuite {

    public NightvisionGoggles() {
        this(3600, 400000L * (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierNightVision - 2)), ConfigHolder.tools.voltageTierNightVision, EntityEquipmentSlot.HEAD);
    }

    public NightvisionGoggles(int energyPerUse, long capacity, int voltageTier, EntityEquipmentSlot slot) {
        super(energyPerUse, capacity, voltageTier, slot);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        IElectricItem item = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        NBTTagCompound nbtData = GTUtility.getOrCreateNbtCompound(itemStack);
        byte toggleTimer = nbtData.getByte("toggleTimer");
        boolean ret = false;
        if (!player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem().equals(itemStack.getItem())) {
            disableNightVision(world, player, false);
        }
        if (SLOT == EntityEquipmentSlot.HEAD) {
            boolean nightvision = nbtData.getBoolean("Nightvision");
            if (ArmorUtils.isKeyDown(player, EnumKey.MENU) && ArmorUtils.isKeyDown(player, EnumKey.MODE_SWITCH) && toggleTimer == 0) {
                toggleTimer = 10;
                if (!nightvision && item.getCharge() >= energyPerUse / 100) {
                    nightvision = true;
                    player.sendMessage(new TextComponentTranslation("metaarmor.message.nightvision.enabled"));
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

            if (nightvision && !world.isRemote && item.getCharge() >= (energyPerUse / 100)) {
                BlockPos pos = new BlockPos((int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
                int skylight = player.getEntityWorld().getLightFromNeighbors(pos);
                if (skylight > 8) {
                    player.removePotionEffect(MobEffects.NIGHT_VISION);
                    player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 999999, 0, true, true));
                } else {
                    player.removePotionEffect(MobEffects.BLINDNESS);
                    player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 999999, 0, true, true));
                }
                ret = true;
                item.discharge((energyPerUse / 100), this.tier, true, false, false);
            }

            if (!world.isRemote && toggleTimer > 0) {
                --toggleTimer;
                nbtData.setByte("toggleTimer", toggleTimer);
            }
        }
        if (ret) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    public void disableNightVision(World world, EntityPlayer player, boolean sendMsg) {
        if (!world.isRemote) {
            player.removePotionEffect(MobEffects.NIGHT_VISION);
            player.removePotionEffect(MobEffects.BLINDNESS);
            if (sendMsg) player.sendMessage(new TextComponentTranslation("metaarmor.message.nightvision.disabled"));
        }
    }

    @Override
    public double getDamageAbsorption() {
        return 0;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/nightvision_goggles.png";
    }

    @Override
    public void addInfo(ItemStack itemStack, List<String> lines) {
        super.addInfo(itemStack, lines);
        if (SLOT == EntityEquipmentSlot.HEAD) {
            NBTTagCompound nbtData = GTUtility.getOrCreateNbtCompound(itemStack);
            boolean nv = nbtData.getBoolean("Nightvision");
            if (nv) {
                lines.add(I18n.format("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(I18n.format("metaarmor.message.nightvision.disabled"));
            }
        }
    }
}
