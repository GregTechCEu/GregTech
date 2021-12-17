package gregtech.common.items.armor;


import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
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

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedJetpack extends Jetpack {

    public AdvancedJetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @Nonnull ItemStack item) {
        IElectricItem cont = item.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if(cont == null) {
            return;
        }
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(item);
        boolean hoverMode = data.hasKey("hover") && data.getBoolean("hover");
        boolean flyEnabled = data.hasKey("flyMode") && data.getBoolean("flyMode");
        byte toggleTimer = data.hasKey("toggleTimer") ? data.getByte("toggleTimer") : 0;

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.HOVER_KEY)) {
            hoverMode = !hoverMode;
            toggleTimer = 5;
            data.setBoolean("hover", hoverMode);
            if (!world.isRemote) {
                if (hoverMode)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"), true);
            }
        }

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.FLY_KEY)) {
            flyEnabled = !flyEnabled;
            toggleTimer = 5;
            data.setBoolean("flyMode", flyEnabled);
            if (!world.isRemote) {
                if (flyEnabled)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.flight.enable"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.flight.disable"), true);
            }
        }

        performFlying(player, hoverMode, item);

        if (toggleTimer > 0) toggleTimer--;

        data.setBoolean("flyMode", flyEnabled);
        data.setBoolean("hover", hoverMode);
        data.setByte("toggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public boolean canFly(@Nonnull ItemStack stack) {
        NBTTagCompound data = stack.getTagCompound();
        if (data == null)
            return false;
        return data.hasKey("flyMode") && data.getBoolean("flyMode");
    }

    @Override
    public double getSprintEnergyModifier() {
        return 2.5D;
    }

    @Override
    public double getSprintSpeedModifier() {
        return 1.3D;
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.34D;
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.03D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.13D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.48D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.14D;
    }

    @Override
    public EnumParticleTypes getParticle() {
        return EnumParticleTypes.CLOUD;
    }

    @Override
    public float getFallDamageReduction() {
        return 2.0f;
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
            if (data.hasKey("flyMode")) {
                String status = data.getBoolean("flyMode") ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("metaarmor.hud.fly_mode", I18n.format(status)));
            }

            if (data.hasKey("hover")) {
                String status = data.getBoolean("hover") ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled";
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

    @Override
    public void addToolComponents(ArmorMetaItem.ArmorMetaValueItem mvi) {
        super.addToolComponents(mvi);
        mvi.addComponents(new Behaviour());
    }

    public static class Behaviour implements IItemBehaviour {

        public Behaviour() {
        }

        @Override
        public void addInformation(ItemStack itemStack, List<String> lines) {
            IItemBehaviour.super.addInformation(itemStack, lines);
            NBTTagCompound data = itemStack.getTagCompound();
            if (data != null) {
                if (data.hasKey("flyMode")) {
                    String status = data.getBoolean("flyMode") ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled";
                    lines.add(I18n.format("metaarmor.hud.fly_mode", I18n.format(status)));
                }
            }
        }
    }
}

