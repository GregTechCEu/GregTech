package gregtech.api.items.effect;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

/**
 * Applies negative effects to players when holding items, such as filled Super Chests and Tanks
 */
public interface IHeldItemEffectManager {

    /**
     * Add a protective armor item to contribute to negating negative effects
     *
     * @param slot  the slot type the item can be stored in
     * @param stack the item to consider protective
     *
     * @throws IllegalArgumentException if {@code slot.getSlotType()} is {@link EntityEquipmentSlot.Type#HAND} or
     *                                  if {@code stack.isEmpty()} is {@code true}.
     */
    void addProtectiveItem(@NotNull EntityEquipmentSlot slot, @NotNull ItemStack stack);

    /**
     * @param player the player to apply negative held item effects to
     */
    void tryApplyEffects(@NotNull EntityPlayer player);
}
