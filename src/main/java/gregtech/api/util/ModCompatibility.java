package gregtech.api.util;

import gregtech.client.utils.ItemRenderCompat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;

@SideOnly(Side.CLIENT)
public class ModCompatibility {

    /**
     * @deprecated Use {@link ItemRenderCompat#getRepresentedStack(ItemStack)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    @Deprecated
    public static ItemStack getRealItemStack(ItemStack itemStack) {
        return ItemRenderCompat.getRepresentedStack(itemStack);
    }
}
