package gregtech.api.items.metaitem.stats;

import gregtech.api.items.toolitem.behaviour.IToolBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

public interface IItemUseManager extends IItemComponent, IToolBehaviour {

    default boolean canStartUsing(ItemStack stack, EntityPlayer player) {
        return true;
    }

    default void onItemUseStart(ItemStack stack, EntityPlayer player) {
    }

    EnumAction getUseAction(ItemStack stack);

    int getMaxItemUseDuration(ItemStack stack);

    default void onItemUsingTick(ItemStack stack, EntityPlayer player, int count) {
    }

    default void onPlayerStoppedItemUsing(ItemStack stack, EntityPlayer player, int timeLeft) {
    }

    default ItemStack onItemUseFinish(ItemStack stack, EntityPlayer player) {
        return stack;
    }
}
