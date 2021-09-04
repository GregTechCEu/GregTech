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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedQuarkTechSuite extends QuarkTechSuite {
    private int cachedSlotId = -1;

    public AdvancedQuarkTechSuite(int energyPerUse, long capacity, int tier) {
        super(EntityEquipmentSlot.CHEST, energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack item) {
        IElectricItem cont = item.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(item);
        boolean hoverMode = data.hasKey("Hover") && data.getBoolean("Hover");
        boolean flyEnabled = data.hasKey("FlyMode") && data.getBoolean("FlyMode");
        byte toggleTimer = data.hasKey("ToggleTimer") ? data.getByte("ToggleTimer") : 0;
        boolean canShare = data.hasKey("CanShare") && data.getBoolean("CanShare");
        boolean result = false;
        float energyUsageMultiplier = 1.0F;

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

        // Additional features
        if (player.onGround) hoverMode = false;
        if (cont.getCharge() >= energyPerUse) player.extinguish();

        // Backpack mechanics
        if (canShare && !world.isRemote) {
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
        if (flyEnabled && cont.canUse(energyPerUse)) {
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
                    if (ArmorUtils.isKeyDown(player, EnumKey.BOOST) && !ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                        if (player.motionY <= 1.6D) player.motionY += 0.4D;
                        energyUsageMultiplier = 1.5F;
                    } else {
                        if (player.motionY <= 0.8D) player.motionY += 0.2D;
                    }

                    if (ArmorUtils.isKeyDown(player, EnumKey.FORWARD)) {
                        if (ArmorUtils.isKeyDown(player, EnumKey.BOOST)) {
                            player.moveRelative(0.0F, 0.0F, 1.0F, 0.20F);
                            energyUsageMultiplier = 1.5F;
                        } else {
                            player.moveRelative(0.0F, 0.0F, 1.0F, 0.1F);
                        }
                    }
                    player.fallDistance = 0.0F;
                    result = true;
                }
            }
        }

        // Fly discharge
        if (result) {
            cont.discharge(MathHelper.floor(energyPerUse * energyUsageMultiplier / 4), cont.getTier(), true, false, false);
            ArmorUtils.resetPlayerFloatingTime(player);
        }

        // Do not spam of server packets
        if (toggleTimer > 0) {
            toggleTimer--;
        }

        data.setBoolean("CanShare", canShare);
        data.setBoolean("FlyMode", flyEnabled);
        data.setBoolean("Hover", hoverMode);
        data.setByte("ToggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();
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
                this.HUD.newString(I18n.format("metaarmor.hud.gravi_engine", I18n.format(status)));
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
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        int damageLimit = Integer.MAX_VALUE;
        IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (energyPerUse > 0) {
            damageLimit = (int) Math.min(damageLimit, 25.0D * item.getCharge() / energyPerUse);
        }
        return new ArmorProperties(8, getDamageAbsorption() * getAbsorption(armor), damageLimit);
    }

    @Override
    public boolean handleUnblockableDamage(EntityLivingBase entity, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        return source != DamageSource.FALL && source != DamageSource.DROWN && source != DamageSource.STARVE;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/advanced_quark_tech_suite_1.png";
    }

    @Override
    public double getDamageAbsorption() {
        return 1.5D;
    }
}
