package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
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
import java.util.List;

public class Jetpack extends ArmorLogicSuite implements IJetpack {

    public Jetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, EntityEquipmentSlot.CHEST);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @Nonnull ItemStack stack) {
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

        performFlying(player, hover, stack);

        if (toggleTimer > 0) toggleTimer--;

        data.setByte("toggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();
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
        container.discharge(amount, 2, true, false, false);
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
                if (data.hasKey("hover")) {
                    String status = (data.getBoolean("hover") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled"));
                    lines.add(I18n.format("metaarmor.hud.hover_mode", status));
                }
            }
        }
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.18D;
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.1D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.12D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.3D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.08D;
    }

    @Override
    public EnumParticleTypes getParticle() {
        return EnumParticleTypes.SMOKE_NORMAL;
    }
}
