package gregtech.api.items.metaitem.stats;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public interface IMouseEventHandler extends IItemComponent {

    /**
     * Handle a mouse event
     * 
     * @param event        the event
     * @param playerClient the player object on the client side
     * @param stack        the {@link ItemStack} the player is holding in their main hand
     */
    @SideOnly(Side.CLIENT)
    void handleMouseEvent(@NotNull MouseEvent event, @NotNull EntityPlayerSP playerClient, @NotNull ItemStack stack);
}
