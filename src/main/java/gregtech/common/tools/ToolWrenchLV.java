package gregtech.common.tools;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

public class ToolWrenchLV extends ToolWrench {

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        return 2.0F;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 1.0f;
    }

    @Override
    public ItemStack getBrokenStack(ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return MetaItems.POWER_UNIT_LV.getChargedStackWithOverride(electricItem);
    }
}
