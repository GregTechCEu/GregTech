package gregtech.api.items.metaitem.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IFoodBehavior extends IItemComponent {

    int getFoodLevel(@NotNull ItemStack itemStack, @Nullable EntityPlayer player);

    float getSaturation(@NotNull ItemStack itemStack, @Nullable EntityPlayer player);

    boolean alwaysEdible(@NotNull ItemStack itemStack, @Nullable EntityPlayer player);

    EnumAction getFoodAction(@NotNull ItemStack itemStack);

    default ItemStack onFoodEaten(@NotNull ItemStack stack, @NotNull EntityPlayer player) {
        return stack;
    }

    void addInformation(@NotNull ItemStack itemStack, List<String> lines);
}
