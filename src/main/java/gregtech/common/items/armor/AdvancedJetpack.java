package gregtech.common.items.armor;


import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class AdvancedJetpack extends Jetpack {

    public AdvancedJetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack item) {
        IElectricItem cont = item.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if(cont == null) {
            return;
        }
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(item);
        boolean hoverMode = data.hasKey("Hover") && data.getBoolean("Hover");
        boolean flyEnabled = data.hasKey("FlyMode") && data.getBoolean("FlyMode");
        byte toggleTimer = data.hasKey("ToggleTimer") ? data.getByte("ToggleTimer") : 0;
        boolean result = false;

        // Mode toggle
        if (!world.isRemote) {
            if (ArmorUtils.isKeyDown(player, EnumKey.FLY_KEY) && toggleTimer == 0) {
                flyEnabled = !flyEnabled;
                toggleTimer = 10;
            }
        }

        if (ArmorUtils.isKeyDown(player, EnumKey.JUMP) && ArmorUtils.isKeyDown(player, EnumKey.MODE_SWITCH) && toggleTimer == 0) {
            hoverMode = !hoverMode;
            toggleTimer = 10;
            if (!world.isRemote) {
                String status = hoverMode ? "metaarmor.jetpack.hover.enable" : "metaarmor.jetpack.hover.disable";
                player.sendMessage(new TextComponentTranslation(status));
            }
        }

        if (player.onGround) hoverMode = false;

        // Fly mechanics
        if (flyEnabled && cont.canUse(energyPerUse) && !player.isInWater() && !player.isInLava()) {
            if (hoverMode) {
                if (!ArmorUtils.isKeyDown(player, EnumKey.JUMP) || !ArmorUtils.isKeyDown(player, EnumKey.SHIFT)) {
                    if (player.motionY > 0.1D) {
                        player.motionY -= 0.1D;
                    }

                    if (player.motionY < -0.1D) {
                        player.motionY += 0.1D;
                    }

                    if (player.motionY <= 0.1D && player.motionY >= -0.1D) {
                        player.motionY = 0.0D;
                    }

                    if (player.motionY > 0.1D || player.motionY < -0.1D) {
                        if (player.motionY < 0) {
                            player.motionY += 0.05D;
                        } else {
                            player.motionY -= 0.0025D;
                        }
                    } else {
                        player.motionY = 0.0D;
                    }
                    ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.6D);
                    ArmorUtils.playJetpackSound(player);
                }

                if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                    player.moveRelative(0.0F, 0.0F, 0.25F, 0.2F);
                }

                if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) {
                    player.motionY = 0.35D;
                }

                if (ArmorUtils.isKeyDown(player, EnumKey.SHIFT)) {
                    player.motionY = -0.35D;
                }

                if (ArmorUtils.isKeyDown(player, EnumKey.JUMP) && ArmorUtils.isKeyDown(player, EnumKey.SHIFT)) {
                    player.motionY = 0.0D;
                }

                player.fallDistance = 0.0F;
                result = true;
            } else {
                if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) {
                    if (player.motionY <= 0.8D) player.motionY += 0.2D;
                    if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                        player.moveRelative(0.0F, 0.0F, 0.85F, 0.1F);
                    }
                    ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.6D);
                    ArmorUtils.playJetpackSound(player);
                    player.fallDistance = 0.0F;
                    result = true;
                }
            }
        }

        // Fly discharge
        if (result) {
            cont.discharge(energyPerUse, cont.getTier(), true, false, false);
            ArmorUtils.resetPlayerFloatingTime(player);
        }

        // Do not spam of server packets
        if (toggleTimer > 0) {
            toggleTimer--;
        }

        data.setBoolean("FlyMode", flyEnabled);
        data.setBoolean("Hover", hoverMode);
        data.setByte("ToggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<String> lines) {
        super.addInfo(itemStack, lines);
    }

    @SideOnly(Side.CLIENT)
    public boolean isNeedDrawHUD() {
        return true;
    }

    @Override
    public void drawHUD(ItemStack item) {
        super.addCapacityHUD(item);
        IElectricItem cont = item.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (cont == null) return;
        if (!cont.canUse(energyPerUse)) return;
        NBTTagCompound data = item.getTagCompound();
        if (data != null) {
            if (data.hasKey("CanShare")) {
                String status = data.getBoolean("CanShare") ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("mataarmor.hud.supply_mode", I18n.format(status)));
            }

            if (data.hasKey("FlyMode")) {
                String status = data.getBoolean("FlyMode") ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("metaarmor.hud.fly_mode", I18n.format(status)));
            }

            if (data.hasKey("Hover")) {
                String status = data.getBoolean("Hover") ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("metaarmor.hud.hover_mode", I18n.format(status)));
            }
        }
        this.HUD.draw();
        this.HUD.reset();
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/advanced_jetpack.png";
    }
}

