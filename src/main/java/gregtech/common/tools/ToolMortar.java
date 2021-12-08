package gregtech.common.tools;

import net.minecraft.item.ItemStack;

public class ToolMortar extends ToolBase {

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 2;
    }

}
