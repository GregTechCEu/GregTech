package gregtech.common.tools;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

public class ToolWrenchHV extends ToolWrenchLV {

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 8;
    }

    @Override
    public int getToolDamagePerDropConversion(ItemStack stack) {
        return 16;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 128;
    }

    @Override
    public int getToolDamagePerEntityAttack(ItemStack stack) {
        return 32;
    }

    @Override
    public int getBaseQuality(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 2.0F;
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        return 4.0F;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 4.0F;
    }

    @Override
    public ItemStack getBrokenStack(ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return MetaItems.POWER_UNIT_HV.getChargedStackWithOverride(electricItem);
    }
}
