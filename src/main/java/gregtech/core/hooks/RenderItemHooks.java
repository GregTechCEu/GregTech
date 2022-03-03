package gregtech.core.hooks;

import gregtech.api.items.toolitem.IGTTool;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class RenderItemHooks {

    public static void renderElectricBar(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        if (stack.getItem() instanceof IGTTool) {
            ((IGTTool) stack.getItem()).renderElectricBar(stack, xPosition, yPosition);
        }
    }

}
