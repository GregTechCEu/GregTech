package gregtech.common.items.armor;

import gregtech.api.items.armoritem.IArmorBehavior;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

// All MobEffects warn that they "might be null" for some reason, so suppress it
@SuppressWarnings("ConstantConditions")
public class NightvisionBehavior implements IArmorBehavior {

    public static final NightvisionBehavior INSTANCE = new NightvisionBehavior();

    private static final String NBT_NIGHTVISION = "NightVision";

    protected NightvisionBehavior() {/**/}

    @Override
    public Set<KeyBind> getListenedKeys() {
        return Collections.singleton(KeyBind.ARMOR_MODE_SWITCH);
    }

    @Override
    public void onKeyPressed(@NotNull ItemStack stack, @NotNull EntityPlayer player, KeyBind keyPressed) {
        if (keyPressed == KeyBind.ARMOR_MODE_SWITCH) {
            NBTTagCompound tag = getBehaviorTag(stack, NBT_NIGHTVISION);
            boolean wasEnabled = tag.getBoolean("Enabled");
            tag.setBoolean("Enabled", !wasEnabled);
            if (wasEnabled) {
                player.removePotionEffect(MobEffects.NIGHT_VISION);
                player.sendStatusMessage(new TextComponentTranslation("metaarmor.message.nightvision.disabled"), true);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("metaarmor.message.nightvision.enabled"), true);
            }
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    @Override
    public boolean onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        if (world.isRemote) return false;
        NBTTagCompound tag = getBehaviorTag(stack, NBT_NIGHTVISION);
        boolean enabled = tag.getBoolean("Enabled");
        if (enabled) {
            player.removePotionEffect(MobEffects.BLINDNESS);
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 999999, 0, true, false));
        }
        return enabled;
    }

    @Override
    public void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        player.removePotionEffect(MobEffects.NIGHT_VISION);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, @NotNull ITooltipFlag flag) {
        NBTTagCompound tag = getBehaviorTag(stack, NBT_NIGHTVISION);
        if (tag.getBoolean("Enabled")) {
            tooltip.add(I18n.format("metaarmor.message.nightvision.enabled"));
        } else {
            tooltip.add(I18n.format("metaarmor.message.nightvision.disabled"));
        }
    }
}
