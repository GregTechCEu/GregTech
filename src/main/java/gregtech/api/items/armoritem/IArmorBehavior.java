package gregtech.api.items.armoritem;

import gregtech.api.util.GTUtility;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IArmorBehavior {

    /**
     * Called every tick that this behavior's armor item is equipped.
     *
     * @return If something was done this tick that needs durability loss or energy drain to be applied.
     */
    default boolean onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        return false;
    }

    /** Called when this behavior's armor item is unequipped. */
    default void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
    }

    /** Called when this behavior's armor item is equipped, as well as when the player logs into the world or is cloned. */
    default void onArmorEquip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
    }

    /** Add to this behavior's armor item tooltip. */
    default void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, @NotNull ITooltipFlag flag) {
    }

    /** Get the equipment slot for this behavior's armor item. Provided since the method call is somewhat obscure. */
    default EntityEquipmentSlot getEquipmentSlot(@NotNull ItemStack stack) {
        return EntityLiving.getSlotForItemStack(stack);
    }

    // todo try to remove/refactor this
    default NBTTagCompound getBehaviorTag(@NotNull ItemStack stack, @NotNull String key) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        NBTTagCompound subTag;
        if (tag.hasKey(key)) {
            subTag = tag.getCompoundTag(key);
        } else {
            subTag = new NBTTagCompound();
            tag.setTag(key, subTag);
        }
        return subTag;
    }
}
