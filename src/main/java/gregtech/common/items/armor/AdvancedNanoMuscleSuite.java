package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.EnumKey;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class AdvancedNanoMuscleSuite extends NanoMuscleSuite implements IJetpack {
    //A replacement for checking the current world time, to get around the gamerule that stops it
    private long timer = 0L;
    private List<Pair<NonNullList<ItemStack>, List<Integer>>> inventoryIndexMap;


    public AdvancedNanoMuscleSuite(int energyPerUse, long capacity, int tier) {
        super(EntityEquipmentSlot.CHEST, energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @Nonnull ItemStack item) {
        IElectricItem cont = item.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (cont == null) {
            return;
        }

        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(item);
        boolean hoverMode = data.hasKey("hover") && data.getBoolean("hover");
        boolean flyEnabled = data.hasKey("flyMode") && data.getBoolean("flyMode");
        byte toggleTimer = data.hasKey("toggleTimer") ? data.getByte("toggleTimer") : 0;
        boolean canShare = data.hasKey("canShare") && data.getBoolean("canShare");

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

        if (toggleTimer == 0 && ArmorUtils.isKeyDown(player, EnumKey.SHARE_KEY)) {
            canShare = !canShare;
            toggleTimer = 5;
            data.setBoolean("canShare", canShare);
            if (!world.isRemote) {
                if (canShare)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.nms.share.enable"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.nms.share.disable"), true);
            }
        }

        performFlying(player, hoverMode, item);

        // Charging mechanics
        if (canShare && !world.isRemote) {
            // Check for new things to charge every 5 seconds
            if (timer % 100 == 0)
                inventoryIndexMap = ArmorUtils.getChargeableItem(player, cont.getTier());

            if (inventoryIndexMap != null && !inventoryIndexMap.isEmpty()) {
                // Charge all inventory slots
                for (int i = 0; i < inventoryIndexMap.size(); i++) {
                    Pair<NonNullList<ItemStack>, List<Integer>> inventoryMap = inventoryIndexMap.get(i);
                    Iterator<Integer> inventoryIterator = inventoryMap.getValue().iterator();
                    while (inventoryIterator.hasNext()) {
                        int slot = inventoryIterator.next();
                        IElectricItem chargable = inventoryMap.getKey().get(slot).getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

                        // Safety check the null, it should not actually happen. Also don't try and charge itself
                        if (chargable == null || chargable == cont) {
                            inventoryIterator.remove();
                            continue;
                        }

                        long attemptedChargeAmount = chargable.getTransferLimit() * 10;

                        // Accounts for tick differences when charging items
                        if (chargable.getCharge() < chargable.getMaxCharge() && cont.canUse(attemptedChargeAmount) && timer % 10 == 0) {
                            long delta = chargable.charge(attemptedChargeAmount, cont.getTier(), true, false);
                            if (delta > 0) {
                                cont.discharge(delta, cont.getTier(), true, false, false);
                            }
                            if (chargable.getCharge() == chargable.getMaxCharge()) {
                                inventoryIterator.remove();
                            }
                            player.inventoryContainer.detectAndSendChanges();
                        }
                    }

                    if (inventoryMap.getValue().isEmpty())
                        inventoryIndexMap.remove(inventoryMap);
                }
            }
        }

        if (toggleTimer > 0) toggleTimer--;

        data.setBoolean("canShare", canShare);
        // used to ensure emergency hover mode is not overridden
        if (data.hasKey("flyMode") && data.getBoolean("flyMode") == flyEnabled)
            data.setBoolean("flyMode", flyEnabled);
        if (data.hasKey("hover") && data.getBoolean("hover") == hoverMode)
            data.setBoolean("hover", hoverMode);
        data.setByte("toggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();

        timer++;
        if (timer == Long.MAX_VALUE)
            timer = 0;
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

        String status = I18n.format("metaarmor.hud.status.disabled");
        if (data.hasKey("hover")) {
            if (data.getBoolean("hover"))
                status = I18n.format("metaarmor.hud.status.enabled");
        }
        lines.add(I18n.format("metaarmor.hud.hover_mode", status));

        String flyMode = I18n.format("metaarmor.hud.status.disabled");
        if (data.hasKey("hover")) {
            if (data.getBoolean("hover"))
                flyMode = I18n.format("metaarmor.hud.status.enabled");
        }
        lines.add(I18n.format("metaarmor.hud.fly_mode", flyMode));
        super.addInfo(itemStack, lines);
    }

    @Override
    public ActionResult<ItemStack> onRightClick(World world, @Nonnull EntityPlayer player, EnumHand hand) {
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
    public boolean canFly(@Nonnull ItemStack stack) {
        NBTTagCompound data = stack.getTagCompound();
        if (data == null)
            return false;
        return data.hasKey("flyMode") && data.getBoolean("flyMode");
    }

    @Override
    public boolean canUseEnergy(@Nonnull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.canUse(amount);
    }

    @Override
    public void drainEnergy(@Nonnull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return;
        container.discharge(amount, tier, true, false, false);
    }

    @Override
    public boolean hasEnergy(@Nonnull ItemStack stack) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.getCharge() > 0;
    }

    private IElectricItem getIElectricItem(@Nonnull ItemStack stack) {
        return stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    @Override
    public double getSprintEnergyModifier() {
        return 4.0D;
    }

    @Override
    public double getSprintSpeedModifier() {
        return 1.8D;
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.4D;
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.005D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.14D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.8D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.19D;
    }

    @Override
    public EnumParticleTypes getParticle() {
        return null;
    }

    @Override
    public float getFallDamageReduction() {
        return 3.5f;
    }
}
