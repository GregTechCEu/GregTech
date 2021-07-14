package gregtech.api.items.metaitem.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface IFoodBehavior extends IItemComponent {

    int getFoodLevel(ItemStack itemStack, @Nullable EntityPlayer player);

    float getSaturation(ItemStack itemStack, @Nullable EntityPlayer player);

    boolean alwaysEdible(ItemStack itemStack, @Nullable EntityPlayer player);

    EnumAction getFoodAction(ItemStack itemStack);

    default ItemStack onFoodEaten(ItemStack stack, EntityPlayer player) {
        return stack;
    }

    void addInformation(ItemStack itemStack, List<String> lines);

}
