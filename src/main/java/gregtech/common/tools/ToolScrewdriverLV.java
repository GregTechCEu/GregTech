package gregtech.common.tools;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

public class ToolScrewdriverLV extends ToolScrewdriver {

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 2;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 2.0f;
    }

    @Override
    public ItemStack getBrokenStack(ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return MetaItems.POWER_UNIT_LV.getChargedStackWithOverride(electricItem);
    }
}
