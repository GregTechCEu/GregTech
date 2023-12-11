package gregtech.api.items.metaitem;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.stats.IFoodBehavior;
import gregtech.api.util.RandomPotionEffect;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Simple {@link gregtech.api.items.metaitem.stats.IFoodBehavior} implementation
 *
 * @see gregtech.api.items.metaitem.MetaItem
 */
public class FoodStats implements IFoodBehavior {

    public final int foodLevel;
    public final float saturation;
    public final boolean isDrink;
    public final boolean alwaysEdible;
    public final RandomPotionEffect[] potionEffects;

    @Nullable
    public ItemStack containerItem;

    public FoodStats(int foodLevel, float saturation, boolean isDrink, boolean alwaysEdible, ItemStack containerItem,
                     RandomPotionEffect... potionEffects) {
        this.foodLevel = foodLevel;
        this.saturation = saturation;
        this.isDrink = isDrink;
        this.alwaysEdible = alwaysEdible;
        if (containerItem != null) {
            this.containerItem = containerItem.copy();
        }
        this.potionEffects = potionEffects;
    }

    public FoodStats(int foodLevel, float saturation, boolean isDrink) {
        this(foodLevel, saturation, isDrink, false, null);
    }

    public FoodStats(int foodLevel, float saturation) {
        this(foodLevel, saturation, false);
    }

    @Override
    public int getFoodLevel(ItemStack itemStack, EntityPlayer player) {
        return foodLevel;
    }

    @Override
    public float getSaturation(ItemStack itemStack, EntityPlayer player) {
        return saturation;
    }

    @Override
    public boolean alwaysEdible(ItemStack itemStack, EntityPlayer player) {
        return alwaysEdible;
    }

    @Override
    public EnumAction getFoodAction(ItemStack itemStack) {
        return isDrink ? EnumAction.DRINK : EnumAction.EAT;
    }

    @Override
    public ItemStack onFoodEaten(ItemStack itemStack, EntityPlayer player) {
        if (!player.world.isRemote) {
            for (RandomPotionEffect potionEffect : potionEffects) {
                if (GTValues.RNG.nextDouble() * 100 > potionEffect.chance) {
                    player.addPotionEffect(new PotionEffect(potionEffect.effect));
                }
            }

            if (containerItem != null) {
                ItemStack containerItemCopy = containerItem.copy(); // Get the copy
                if (player == null || !player.capabilities.isCreativeMode) {
                    if (itemStack.isEmpty()) {
                        return containerItemCopy;
                    }

                    if (player != null) {
                        if (!player.inventory.addItemStackToInventory(containerItemCopy))
                            player.dropItem(containerItemCopy, false, false);
                    }
                }
            }
        }
        return itemStack;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (potionEffects.length > 0) {
            PotionEffect[] effects = new PotionEffect[potionEffects.length];
            for (int i = 0; i < potionEffects.length; i++) {
                effects[i] = potionEffects[i].effect;
            }
            // GTUtility.addPotionTooltip(Iterables.cycle(effects), lines); todo implement this
        }
    }
}
