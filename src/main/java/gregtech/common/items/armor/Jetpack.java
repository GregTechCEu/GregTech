package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class Jetpack extends ArmorLogicSuite {

    public Jetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, EntityEquipmentSlot.CHEST);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @Nonnull ItemStack stack) {
        IElectricItem container = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (container == null)
            return;

        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(stack);
        byte toggleTimer = 0;
        boolean hover = false;
        if (data.hasKey("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
        if (data.hasKey("hover")) hover = data.getBoolean("hover");

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.HOVER_KEY)) {
            hover = !hover;
            toggleTimer = 10;
            data.setBoolean("hover", hover);
            if (!world.isRemote) {
                if (hover)
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"));
                else
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"));
            }
        }

        if (!player.isInWater() && !player.isInLava() && container.canUse(energyPerUse)) {
            if (!hover) {
                if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) { //todo prevent holding space + e from flying upwards
                    if (player.motionY < 0.6D) player.motionY += 0.2D;
                    if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                        player.moveRelative(0.0F, 0.0F, 1.0F, 0.1F);
                    }
                    ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.6D);
                    ArmorUtils.playJetpackSound(player);
                }
            } else {
                if (!player.onGround) {
                    ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.3D);
                    ArmorUtils.playJetpackSound(player);
                }

                if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD) && player.motionX < 0.5D && player.motionZ < 0.5D) {
                    player.moveRelative(0.0F, 0.0F, 1.0F, 0.025F);
                }

                if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) { //todo prevent holding space + e from flying upwards
                    if (player.motionY < 0.5D) {
                        player.motionY += 0.125D;
                    }
                } else if (ArmorUtils.isKeyDown(player, EnumKey.SHIFT)) {
                    if (player.motionY < -0.5D) player.motionY += 0.1D;
                } else if (!ArmorUtils.isKeyDown(player, EnumKey.JUMP) && !ArmorUtils.isKeyDown(player, EnumKey.SHIFT) && !player.onGround) {
                    if (player.motionY < 0 && player.motionY >= -0.03D) player.motionY = -0.025D;
                    if (player.motionY < -0.025D) {
                        if (player.motionY + 0.2D > -0.025D) {
                            player.motionY = -0.025D;
                        } else {
                            player.motionY += 0.2D;
                        }
                    }
                }
                player.fallDistance = 0.0F;
            }

            if (!player.onGround && (hover || ArmorUtils.isKeyDown(player, EnumKey.JUMP))) {
                container.discharge(energyPerUse, container.getTier(), false, false, false);
            }

            if (world.getWorldTime() % 40 == 0 && !player.onGround) {
                ArmorUtils.resetPlayerFloatingTime(player);
            }

            if (toggleTimer > 0) toggleTimer--;

            data.setByte("toggleTimer", toggleTimer);
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/jetpack.png";
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        return new ArmorProperties(0, 0, 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isNeedDrawHUD() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawHUD(ItemStack item) {
        super.addCapacityHUD(item);
        NBTTagCompound data = item.getTagCompound();
        if (data != null) {
            if (data.hasKey("hover")) {
                String status = (data.getBoolean("hover") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled"));
                String result = I18n.format("metaarmor.hud.hover_mode", status);
                this.HUD.newString(result);
            }
        }
        this.HUD.draw();
        this.HUD.reset();
    }
}
