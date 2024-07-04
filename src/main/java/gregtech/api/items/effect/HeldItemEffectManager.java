package gregtech.api.items.effect;

import gregtech.api.config.ConfigUtil;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HeldItemEffectManager implements IHeldItemEffectManager {

    private static final PotionEffect HUNGER = new PotionEffect(MobEffects.HUNGER, 20, 0, true, false);
    private static final PotionEffect SLOWNESS = new PotionEffect(MobEffects.SLOWNESS, 20, 1, true, false);
    private static final PotionEffect MINING_FATIGUE = new PotionEffect(MobEffects.MINING_FATIGUE, 20, 1, true, false);
    private static final PotionEffect WEAKNESS = new PotionEffect(MobEffects.WEAKNESS, 20, 0, true, false);

    private final Collection<ItemStack> protectiveBoots = new ArrayList<>();
    private final Collection<ItemStack> protectiveLegs = new ArrayList<>();
    private final Collection<ItemStack> protectiveChests = new ArrayList<>();
    private final Collection<ItemStack> protectiveHelmets = new ArrayList<>();

    @ApiStatus.Internal
    public @NotNull HeldItemEffectManager init() {
        for (String string : ConfigHolder.misc.protectiveArmor) {
            ItemStack stack = ConfigUtil.parseItemStack(string.trim(), GTLog.logger);
            if (stack.isEmpty()) continue;
            EntityEquipmentSlot slot = stack.getItem().getEquipmentSlot(stack);
            if (slot == null || slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR) {
                GTLog.logger.error("Item {} cannot be equipped in an armor slot", string);
                continue;
            }
            addProtectiveItem(slot, stack);
        }

        return this;
    }

    @Override
    public void addProtectiveItem(@NotNull EntityEquipmentSlot slot, @NotNull ItemStack stack) {
        Preconditions.checkArgument(slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR,
                "type must be ARMOR");
        Preconditions.checkArgument(!stack.isEmpty(), "stack cannot be empty");
        switch (slot) {
            case FEET -> protectiveBoots.add(stack);
            case LEGS -> protectiveLegs.add(stack);
            case CHEST -> protectiveChests.add(stack);
            case HEAD -> protectiveHelmets.add(stack);
        }
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void tryApplyEffects(@NotNull EntityPlayer player) {
        if (!shouldApplyEffects(player)) return;

        if (!player.isPotionActive(MobEffects.HUNGER)) {
            player.addPotionEffect(new PotionEffect(HUNGER));
        }
        if (!player.isPotionActive(MobEffects.SLOWNESS)) {
            player.addPotionEffect(new PotionEffect(SLOWNESS));
        }
        if (!player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            player.addPotionEffect(new PotionEffect(MINING_FATIGUE));
        }
        if (!player.isPotionActive(MobEffects.WEAKNESS)) {
            player.addPotionEffect(new PotionEffect(WEAKNESS));
        }
    }

    private boolean shouldApplyEffects(@NotNull EntityPlayer player) {
        if (player.capabilities.isCreativeMode) return false;

        List<ItemStack> armorInventory = player.inventory.armorInventory;
        if (armorInventory.size() < 4) return false;

        if (!isProtection(protectiveBoots, armorInventory.get(EntityEquipmentSlot.FEET.getIndex()))) {
            return true;
        }
        if (!isProtection(protectiveLegs, armorInventory.get(EntityEquipmentSlot.LEGS.getIndex()))) {
            return true;
        }
        if (!isProtection(protectiveChests, armorInventory.get(EntityEquipmentSlot.CHEST.getIndex()))) {
            return true;
        }

        return !isProtection(protectiveHelmets, armorInventory.get(EntityEquipmentSlot.HEAD.getIndex()));
    }

    private static boolean isProtection(@NotNull Iterable<@NotNull ItemStack> candidates, @NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (ItemStack s : candidates) {
            if (s.isItemEqual(stack)) {
                return true;
            }
        }
        return false;
    }
}
