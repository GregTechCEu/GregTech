package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.List;

public class TurbineRotorBehavior extends AbstractMaterialPartBehavior implements IItemMaxStackSizeProvider {

    private static final int TOOL_DURABILITY_MULTIPLIER = 100;

    public static TurbineRotorBehavior getInstanceFor(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof MetaItem)) {
            return null;
        }
        MetaItem<?> metaItem = (MetaItem<?>) itemStack.getItem();
        MetaValueItem valueItem = metaItem.getItem(itemStack);
        if (valueItem == null) {
            return null;
        }
        IItemDurabilityManager durabilityManager = valueItem.getDurabilityManager();
        if (!(durabilityManager instanceof TurbineRotorBehavior)) {
            return null;
        }
        return (TurbineRotorBehavior) durabilityManager;
    }

    @Override
    public int getPartMaxDurability(ItemStack itemStack) {
        Material material = getPartMaterial(itemStack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property != null ? property.toolDurability * TOOL_DURABILITY_MULTIPLIER : 0;
    }

    public double getRotorEfficiency(ItemStack itemStack) {
        Material primaryMaterial = getPartMaterial(itemStack);
        ToolProperty property = primaryMaterial.getProperty(PropertyKey.TOOL);
        return property == null ? 0.1 : Math.min(property.toolSpeed / 14.0, 1.0);
    }

    public void applyRotorDamage(ItemStack itemStack, int damageApplied) {
        int rotorDurability = getPartMaxDurability(itemStack);
        int resultDamage = getPartDamage(itemStack) + damageApplied;
        if (resultDamage >= rotorDurability) {
            itemStack.shrink(1);
        } else {
            setPartDamage(itemStack, resultDamage);
        }
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        super.addInformation(stack, lines);
        lines.add(I18n.format("metaitem.tool.tooltip.rotor.efficiency", (int) (getRotorEfficiency(stack) * 100)));
    }
}
