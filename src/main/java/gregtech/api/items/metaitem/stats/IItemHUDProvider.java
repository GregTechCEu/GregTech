package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * Provides a drawable HUD for the item
 */
public interface IItemHUDProvider extends IItemComponent {

    /**
     * @return if the HUD needs to be drawn
     */
    @SideOnly(Side.CLIENT)
    default boolean shouldDrawHUD() {
        return true;
    }

    /**
     * Draws the HUD
     *
     * @param stack the ItemStack to retrieve information from
     */
    @SideOnly(Side.CLIENT)
    default void drawHUD(ItemStack stack) {/**/}

    /**
     * Checks and draws the hud for a provider
     *
     * @param provider the provider whose hud to draw
     * @param stack    the stack the provider should use
     */
    @SideOnly(Side.CLIENT)
    static void tryDrawHud(@NotNull IItemHUDProvider provider, @NotNull ItemStack stack) {
        if (provider.shouldDrawHUD()) provider.drawHUD(stack);
    }
}
