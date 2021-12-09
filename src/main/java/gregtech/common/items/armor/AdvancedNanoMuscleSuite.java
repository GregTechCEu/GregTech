package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import gregtech.common.items.MetaItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedNanoMuscleSuite extends NanoMuscleSuite {
    private int cachedSlotId = -1;

    public AdvancedNanoMuscleSuite(int energyPerUse, long capacity, int tier) {
        super(EntityEquipmentSlot.CHEST, energyPerUse, capacity, tier);
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
        boolean canShare = data.hasKey("canShare") && data.getBoolean("canShare");

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.HOVER_KEY)) {
            hoverMode = !hoverMode;
            toggleTimer = 10;
            data.setBoolean("hover", hoverMode);
            if (!world.isRemote) {
                if (hoverMode)
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"));
                else
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"));
            }
        }

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.FLY_KEY)) {
            flyEnabled = !flyEnabled;
            toggleTimer = 10;
            data.setBoolean("flyMode", flyEnabled);
            if (!world.isRemote) {
                if (flyEnabled)
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.flight.enable"));
                else
                    player.sendMessage(new TextComponentTranslation("metaarmor.jetpack.flight.disable"));
            }
        }

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.SHARE_KEY)) {
            canShare = !canShare;
            toggleTimer = 10;
            data.setBoolean("canShare", canShare);
            if (!world.isRemote) {
                if (canShare)
                    player.sendMessage(new TextComponentTranslation("metaarmor.nms.share.enable"));
                else
                    player.sendMessage(new TextComponentTranslation("metaarmor.nms.share.disable"));
            }
        }

        // Backpack mechanics
        if (canShare) {
            // Trying to find item in inventory
            if (cachedSlotId < 0) {
                // Do not call this method often
                if (world.getWorldTime() % 40 == 0) {
                    cachedSlotId = ArmorUtils.getChargeableItem(player, cont.getTier());
                }
            } else {
                ItemStack cachedItem = player.inventory.mainInventory.get(cachedSlotId);
                if (!ArmorUtils.isPossibleToCharge(cachedItem)) {
                    cachedSlotId = -1;
                }
            }


            // Do neighbor armor charge
            //todo this doesn't work
            for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
                IElectricItem chargeable = player.inventory.armorInventory.get(i).getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (chargeable == null) continue;
                if (player.inventory.armorInventory.get(i).isItemEqual(MetaItems.ADVANCED_QUARK_TECH_SUITE_CHESTPLATE.getStackForm()))
                    continue;
                if ((chargeable.getCharge() + chargeable.getTransferLimit() * 10) <= chargeable.getMaxCharge() && cont.canUse(chargeable.getTransferLimit() * 10) && world.getWorldTime() % 10 == 0) {
                    long delta = chargeable.charge(chargeable.getTransferLimit() * 10, chargeable.getTier(), true, false);
                    if (delta > 0) cont.discharge(delta, cont.getTier(), true, false, false);
                    player.inventoryContainer.detectAndSendChanges();
                }
            }

            // Do charge
            if (cachedSlotId >= 0) {
                IElectricItem chargeable = player.inventory.mainInventory.get(cachedSlotId).getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (chargeable == null) {
                    return;
                }
                if (cont.canUse(chargeable.getTransferLimit() * 10) && world.getWorldTime() % 10 == 0) {
                    long delta = chargeable.charge(chargeable.getTransferLimit() * 10, chargeable.getTier(), true, false);
                    if (delta > 0) cont.discharge(delta, cont.getTier(), true, false, false);
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
        }

        // Fly mechanics
        if (!player.isInWater() && !player.isInLava() && cont.canUse(120)) {
            if (flyEnabled) {
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

                } else {
                    if (ArmorUtils.isKeyDown(player, EnumKey.JUMP)) {
                        if (player.motionY <= 0.8D) player.motionY += 0.2D;
                        if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                            player.moveRelative(0.0F, 0.0F, 0.85F, 0.1F);
                        }
                        ArmorUtils.spawnParticle(world, player, EnumParticleTypes.CLOUD, -0.6D);
                        ArmorUtils.playJetpackSound(player);
                    }
                }
            }
            player.fallDistance = 0.0F;
        }

        // Fly discharge
        if (!player.onGround && (hoverMode || ArmorUtils.isKeyDown(player, EnumKey.JUMP))) {
            cont.discharge(120, cont.getTier(), true, false, false);
        }

        if (world.getWorldTime() % 40 == 0 && !player.onGround) {
            ArmorUtils.resetPlayerFloatingTime(player);
        }

        if (toggleTimer > 0) toggleTimer--;

        data.setBoolean("canShare", canShare);
        data.setBoolean("flyMode", flyEnabled);
        data.setBoolean("hover", hoverMode);
        data.setByte("toggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<String> lines) {
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
        String state;
        if (data.hasKey("canShare")) {
            state = data.getBoolean("canShare") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled");
        } else {
            state = I18n.format("metaarmor.hud.status.disabled");
        }
        lines.add(I18n.format("metaarmor.energy_share.tooltip", state));
        lines.add(I18n.format("metaarmor.energy_share.tooltip.guide"));
        super.addInfo(itemStack, lines);
    }

    @Override
    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack armor = player.getHeldItem(hand);

        if (armor.getItem() instanceof ArmorMetaItem && player.isSneaking()) {
            NBTTagCompound data = GTUtility.getOrCreateNbtCompound(armor);
            boolean canShareEnergy = data.hasKey("canShare") && data.getBoolean("canShare");

            canShareEnergy = !canShareEnergy;
            String locale = "metaarmor.energy_share." + (canShareEnergy ? "enable" : "disable");
            if (!world.isRemote) player.sendMessage(new TextComponentTranslation(locale));
            data.setBoolean("canShare", canShareEnergy);
            return ActionResult.newResult(EnumActionResult.SUCCESS, armor);
        }

        return super.onRightClick(world, player, hand);
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
            if (data.hasKey("canShare")) {
                String status = data.getBoolean("canShare") ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("mataarmor.hud.supply_mode", I18n.format(status)));
            }

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
        return "gregtech:textures/armor/advanced_nano_muscle_suite_1.png";
    }

    @Override
    public double getDamageAbsorption() {
        return 1.0D;
    }

}
