package gregtech.api.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Indicates an unified item that can be damaged an has custom logic of handling it's damage
 */
public interface IToolItem {

    /**
     * Tries to apply given amount of damage to item
     * If simulated, actual damage won't be applied and durability won't be changed
     * <p>
     * DO NOT USE METHODS BELOW TO CHECK IF TOOL CAN RECEIVE SPECIFIED AMOUNT OF DAMAGE,
     * Use this method with simulate = true to check so, because it can be different for electric items!
     */
    default boolean damageItem(ItemStack stack, EntityLivingBase entity, int damage, boolean simulate) {
        return damageItem(stack, entity, damage, false, simulate) > 0;
    }

    /**
     * Tries to apply given amount of damage to item
     * If simulated, actual damage won't be applied and durability won't be changed
     * <p>
     * DO NOT USE METHODS BELOW TO CHECK IF TOOL CAN RECEIVE SPECIFIED AMOUNT OF DAMAGE,
     * Use this method with simulate = true to check so, because it can be different for electric items!
     *
     * @param stack        the item to damage
     * @param damage       the damage to apply
     * @param allowPartial normally you should pass false, when true an electric item
     *                     will discharge all its remaining charge if it cannot do the
     *                     full operation and tell you the equivalent damage
     * @param simulate     pass true to do a dry run of the process
     * @return the actual damage applied
     */
    int damageItem(ItemStack stack, EntityLivingBase entity, int damage, boolean allowPartial, boolean simulate);

    /**
     * @return amount of internal damage this item have
     * Item is not forced to modify this value in #damageItem, it's used only for rendering purpose
     */
    int getItemDamage(ItemStack stack);

    /**
     * @return amount of max internal damage this item can receive.
     * Only used for rendering purposes
     */
    int getMaxItemDamage(ItemStack stack);
}
