package gregtech.common.items.armor;

import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;


/**
 * Logic from SimplyJetpacks2: https://github.com/Tomson124/SimplyJetpacks2/blob/1.12/src/main/java/tonius/simplyjetpacks/item/ItemJetpack.java
 */
public interface IJetpack {

    default double getSprintEnergyModifier() {
        return 1.0D;
    }

    default double getSprintSpeedModifier() {
        return 1.0D;
    }

    default double getVerticalHoverSpeed() {
        return 0.18D;
    }

    default double getVerticalHoverSlowSpeed() {
        return 0.14D;
    }

    default double getVerticalAcceleration() {
        return 0.1D;
    }

    default double getVerticalSpeed() {
        return 0.22D;
    }

    default double getSidewaysSpeed() {
        return 0.0D;
    }

    default EnumParticleTypes getParticle() {
        return EnumParticleTypes.SMOKE_LARGE;
    }

    default boolean canFly(ItemStack stack) {
        return true;
    }

    default float getFallDamageReduction() {
        return 0.0f;
    }

    default boolean hasEmergencyHover() {
        return false;
    }

    int getEnergyPerUse();

    boolean canUseEnergy(ItemStack stack, int amount);

    void drainEnergy(ItemStack stack, int amount);

    boolean hasEnergy(ItemStack stack);

    default void performFlying(@Nonnull EntityPlayer player, boolean hover, ItemStack stack) {
        double currentAccel = getVerticalAcceleration() * (player.motionY < 0.3D ? 2.5D : 1.0D);
        double currentSpeedVertical = getVerticalSpeed() * (player.isInWater() ? 0.4D : 1.0D);
        boolean flyKeyDown = ArmorUtils.isKeyDown(player, EnumKey.JUMP);
        boolean descendKeyDown = ArmorUtils.isKeyDown(player, EnumKey.CROUCH);

        if (canFly(stack) && !player.isInWater() && !player.isInLava() && canUseEnergy(stack, getEnergyPerUse())) {
            if (flyKeyDown || hover && !player.onGround) {
                drainEnergy(stack, (int) (player.isSprinting() ? Math.round(getEnergyPerUse() * getSprintEnergyModifier()) : getEnergyPerUse()));

                if (hasEnergy(stack)) {
                    if (flyKeyDown) {
                        if (!hover) {
                            player.motionY = Math.min(player.motionY + currentAccel, currentSpeedVertical);
                        } else {
                            if (descendKeyDown)
                                player.motionY = Math.min(player.motionY + currentAccel, -getVerticalHoverSlowSpeed());
                            else player.motionY = Math.min(player.motionY + currentAccel, getVerticalHoverSpeed());
                        }
                    } else {
                        player.motionY = Math.min(player.motionY + currentAccel, -getVerticalHoverSlowSpeed());
                    }
                    float speedSideways = (float) (player.isSneaking() ? getSidewaysSpeed() * 0.5f : getSidewaysSpeed());
                    float speedForward = (float) (player.isSprinting() ? speedSideways * getSprintSpeedModifier() : speedSideways);

                    if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD))
                        player.moveRelative(0, 0, speedForward, speedForward);
                    if (ArmorUtils.isKeyDown(player, EnumKey.BACKWARD))
                        player.moveRelative(0, 0, -speedSideways, speedSideways * 0.8f);
                    if (ArmorUtils.isKeyDown(player, EnumKey.LEFT))
                        player.moveRelative(speedSideways, 0, 0, speedSideways);
                    if (ArmorUtils.isKeyDown(player, EnumKey.RIGHT))
                        player.moveRelative(-speedSideways, 0, 0, speedSideways);
                    if (!player.getEntityWorld().isRemote) {
                        player.fallDistance = 0;
                    }
                }
                ArmorUtils.spawnParticle(player.getEntityWorld(), player, getParticle(), -0.6D);
            }
        }
        if (!player.world.isRemote && hasEmergencyHover()) {
            if (hasEnergy(stack) && (!hover || !canFly(stack))) {
                if (player.posY < -5) {
                    this.performEmergencyHover(stack, player);
                } else {
                    if (!player.isCreative() && player.fallDistance - 1.2f >= player.getHealth()) {
                        for (int j = 0; j <= 16; j++) {
                            int x = Math.round((float) player.posX - 0.5f);
                            int y = Math.round((float) player.posY) - j;
                            int z = Math.round((float) player.posZ - 0.5f);
                            if (!player.world.isAirBlock(new BlockPos(x, y, z))) {
                                performEmergencyHover(stack, player);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    //todo this doesn't work
    default void performEmergencyHover(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
        NBTTagCompound data =  GTUtility.getOrCreateNbtCompound(stack);

        data.setBoolean("flyMode", true);
        data.setBoolean("hover", true);

        player.inventoryContainer.detectAndSendChanges();
        player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.emergency_hover_mode")
                .setStyle(new Style().setColor(TextFormatting.RED)), true);
    }
}
